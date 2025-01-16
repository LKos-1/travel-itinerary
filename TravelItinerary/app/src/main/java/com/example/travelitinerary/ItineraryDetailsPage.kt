package com.example.travelitinerary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ItineraryDetailsPage(navController: NavController, city: String, entryId: String) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var description by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf(listOf<Pair<String, Double>>()) }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch and display the destination details
    LaunchedEffect(key1 = entryId, key2 = city) {
        if (currentUserId != null) {
            Log.d("Firestore", "currentUserId: $currentUserId")
            Log.d("Firestore", "entryId: $entryId")
            db.collection("users")
                .document(currentUserId)
                .collection("entries")
                .document(entryId)
                .collection("destinations")
                .whereEqualTo("name", city)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val itinerary = document.get("itinerary") as? Map<String, Any>
                        if (itinerary != null) {
                            description = itinerary["description"] as? String ?: ""
                            expenses = (itinerary["expenses"] as? List<Map<String, Any>>)?.mapNotNull {
                                val desc = it["description"] as? String
                                val amount = it["amount"] as? Double
                                if (desc != null && amount != null) Pair(desc, amount) else null
                            } ?: emptyList()
                        } else {
                            errorMessage = "Itinerary not found"
                        }
                    } else {
                        errorMessage = "Destination not found"
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Error accessing destination: ${exception.message}"
                }
        } else {
            errorMessage = "User not authenticated"
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .background(Color(0xFFF5F5F5)) // Light gray background
    ) {
        Text(
            "Itinerary for $city",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Description Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Description:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Expense List Section
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(expense.first, style = MaterialTheme.typography.bodySmall)
                        Text("$${expense.second}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Error message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Edit Itinerary Button
        Button(
            onClick = { navController.navigate("edit-itinerary/$city") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Text("Edit Itinerary", color = Color.White)
        }

        // Delete Itinerary Button
        Button(
            onClick = {
                if (currentUserId != null) {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("entries")
                        .document(entryId)
                        .collection("destinations")
                        .whereEqualTo("name", city)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val document = querySnapshot.documents[0]
                                document.reference.update("itinerary", FieldValue.delete())
                                    .addOnSuccessListener {
                                        errorMessage = "Itinerary deleted successfully"
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { exception ->
                                        errorMessage = "Failed to delete itinerary: ${exception.message}"
                                    }
                            } else {
                                errorMessage = "Destination not found"
                            }
                        }
                        .addOnFailureListener { exception ->
                            errorMessage = "Error accessing destination: ${exception.message}"
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Text("Delete Itinerary", color = Color.White)
        }

        // Back Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}











