package com.example.travelitinerary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainPage(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "User" // Get the user's name or default to "User"
    val entries = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Fetch entries from Firestore
    LaunchedEffect(user?.uid) {
        user?.let {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .document(it.uid)
                .collection("entries")
                .get()
                .addOnSuccessListener { snapshot ->
                    entries.value = snapshot.documents.map { doc -> doc.data ?: emptyMap() }
                }
                .addOnFailureListener { e ->
                    Log.e("MainPage", "Failed to fetch entries: ${e.message}")
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Entries",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Entries section
        if (entries.value.isEmpty()) {
            Text("No entries created yet", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(entries.value) { entry ->
                    val name = entry["name"] as? String ?: "Untitled"
                    val date = entry["date"] as? String ?: "Unknown Date"
                    EntryItem(entryName = name, entryDate = date)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Add New Entry Button
        Button(
            onClick = { navController.navigate("add-entry") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Add a new entry")
        }
    }
}

@Composable
fun EntryItem(entryName: String, entryDate: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entryName, style = MaterialTheme.typography.bodyMedium)
            Text(text = entryDate, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    val navController = rememberNavController()
    MainPage(navController = navController)
}