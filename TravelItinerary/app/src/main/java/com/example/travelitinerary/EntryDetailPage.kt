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
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EntryDetailPage(navController: NavController, entryId: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val entryDocument = remember { mutableStateOf<Map<String, Any>?>(null) }

    // Variables for entry details
    var entryName by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf("") }
    var destinations by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    // Fetch entry details from Firestore based on entryId
    LaunchedEffect(entryId) {
        user?.let {
            firestore.collection("users")
                .document(it.uid)
                .collection("entries")
                .document(entryId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val entry = document.data
                        entryName = entry?.get("name") as? String ?: "No name"
                        entryDate = entry?.get("date") as? String ?: "Unknown"
                        destinations = (entry?.get("destinations") as? List<*>)?.joinToString(", ") ?: "No destinations"
                        rating = entry?.get("rating") as? Int ?: 0
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EntryDetailPage", "Failed to fetch entry: ${e.message}")
                }
        }
    }

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
        Text("Destinations: $destinations", style = MaterialTheme.typography.bodyMedium)
        Text("Rating: $rating", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Main Page")
        }
    }
}

