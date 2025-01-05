package com.example.travelitinerary

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EntryDetailPage(navController: NavController, entryId: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Variables for entry details
    var entryName by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf("") }
    var destinations by remember { mutableStateOf<List<String>>(emptyList()) }
    var rating by remember { mutableStateOf(0) }
    var itinerary by remember { mutableStateOf("") }  // New field for itinerary
    var expense by remember { mutableStateOf(0.0) }  // New field for expense
    var isLoading by remember { mutableStateOf(true) } // Loading state
    var errorMessage by remember { mutableStateOf<String?>(null) } // Error state

    // Function to format the date
    fun formatDate(timestamp: Timestamp?): String {
        return timestamp?.let {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            simpleDateFormat.format(it.toDate())
        } ?: "Unknown"
    }

    // Fetch entry details from Firestore based on entryId
    // Fetch entry details from Firestore based on entryId
    LaunchedEffect(entryId) {
        user?.let {
            // Fetch entry details from the "entries" collection
            firestore.collection("users")
                .document(it.uid)
                .collection("entries")
                .document(entryId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val entry = document.data
                        entryName = entry?.get("name") as? String ?: "No name"
                        val timestamp = entry?.get("date") as? Timestamp
                        entryDate = formatDate(timestamp)
                        rating = (entry?.get("rating") as? Number)?.toInt() ?: 0
                        itinerary = entry?.get("itinerary") as? String ?: ""  // Fetch itinerary
                        expense = (entry?.get("expense") as? Number)?.toDouble() ?: 0.0  // Fetch expense
                    }

                    // Fetch related destinations from the subcollection
                    firestore.collection("users")
                        .document(it.uid)
                        .collection("entries")
                        .document(entryId)
                        .collection("destinations")
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val destinationList = querySnapshot.documents.mapNotNull { doc ->
                                doc.getString("name")
                            }
                            destinations = destinationList
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Failed to fetch destinations: ${e.message}"
                            isLoading = false
                        }
                }
                .addOnFailureListener { e ->
                    errorMessage = "Failed to fetch entry: ${e.message}"
                    isLoading = false
                }
        }
    }


    // Show loading state or error message if necessary
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else if (errorMessage != null) {
        Text("Error: $errorMessage", color = Color.Red)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Entry Details", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Name: $entryName", style = MaterialTheme.typography.bodyMedium)
            Text("Date: $entryDate", style = MaterialTheme.typography.bodyMedium)
            Text("Destinations: ${destinations.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
            Text("Rating: $rating", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val citiesString = destinations.joinToString(",")
                    navController.navigate("add-itinerary/$entryId/$citiesString") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Add or view Itinerary")
            }

            // Edit Button
            Button(
                onClick = {
                    navController.navigate("add-entry/$entryId")
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Edit Entry")
            }

            // Delete Button
            Button(
                onClick = {
                    user?.let {
                        firestore.collection("users")
                            .document(it.uid)
                            .collection("entries")
                            .document(entryId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(navController.context, "Entry deleted successfully!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Go back after deletion
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(navController.context, "Failed to delete entry: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Delete Entry")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Main Page")
            }
        }
    }
}

