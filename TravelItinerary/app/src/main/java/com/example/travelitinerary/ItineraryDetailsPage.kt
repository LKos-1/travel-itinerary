package com.example.travelitinerary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
fun ItineraryDetailsPage(navController: NavController, city: String) {
    val entryId = navController.previousBackStackEntry
        ?.arguments?.getString("entryId") ?: ""
    val db = FirebaseFirestore.getInstance()
    val selectedCities = navController.previousBackStackEntry
        ?.arguments?.getString("selectedCities")
        ?.split(",")?.toMutableList() ?: mutableListOf()
    var description by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf(listOf<Pair<String, Double>>()) }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Itinerary for $city", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Description Field
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Expense Fields
        TextField(
            value = expenseAmount,
            onValueChange = { expenseAmount = it },
            label = { Text("Expense Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        TextField(
            value = expenseDescription,
            onValueChange = { expenseDescription = it },
            label = { Text("Expense Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Add Expense Button
        Button(
            onClick = {
                if (expenseAmount.isNotBlank() && expenseDescription.isNotBlank()) {
                    val amount = expenseAmount.toDoubleOrNull()
                    if (amount != null) {
                        expenses = expenses + Pair(expenseDescription, amount)
                        expenseAmount = ""
                        expenseDescription = ""
                        errorMessage = "" // Reset error message
                    } else {
                        errorMessage = "Invalid expense amount"
                    }
                } else {
                    errorMessage = "Please fill in both fields"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Add Expense")
        }

        // Display added expenses
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(expenses) { expense ->
                Text("${expense.first}: $${expense.second}")
            }
        }

        // Save Itinerary Button
        Button(
            onClick = {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    // Access the destination document inside the entry document
                    db.collection("users")
                        .document(currentUserId)
                        .collection("entries")
                        .document(entryId)
                        .collection("destinations")
                        .whereEqualTo("name", city) // Query for the city by its name
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Get the first matching document (assuming city name is unique)
                                val document = querySnapshot.documents[0]

                                // Update the existing destination document with the itinerary field
                                document.reference
                                    .update(
                                        "itinerary", mapOf(
                                            "description" to description,
                                            "expenses" to expenses.map { mapOf("description" to it.first, "amount" to it.second) }
                                        )
                                    )
                                    .addOnSuccessListener {
                                        navController.navigate("main-page") // Navigate to the main page
                                    }
                                    .addOnFailureListener { exception ->
                                        errorMessage = "Failed to save itinerary: ${exception.message}"
                                    }
                            } else {
                                errorMessage = "Destination document with city name not found"
                            }
                        }
                        .addOnFailureListener { exception ->
                            errorMessage = "Error accessing destination: ${exception.message}"
                        }
                } else {
                    errorMessage = "User not authenticated"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save Itinerary")
        }
    }
}





