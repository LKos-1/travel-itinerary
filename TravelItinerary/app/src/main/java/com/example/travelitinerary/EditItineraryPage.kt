package com.example.travelitinerary

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditItineraryPage(
    navController: NavController,
    city: String
) {
    val entryId = navController.previousBackStackEntry
        ?.arguments?.getString("entryId") ?: ""
    val db = FirebaseFirestore.getInstance()
    val selectedCities = navController.previousBackStackEntry
        ?.arguments?.getString("selectedCities")
        ?.split(",")?.toMutableList() ?: mutableListOf()

    // State variables for description, expenses, and error messages
    var description by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf(listOf<Pair<String, Double>>()) }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch the current itinerary information from Firestore to pre-fill the fields
    LaunchedEffect(city) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
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
                    Log.d("Firestore", "QuerySnapshot size: ${querySnapshot.size()}")
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        Log.d("Firestore", "Document data: ${document.data}")
                        val itinerary = document.get("itinerary") as? Map<*, *>
                        description = itinerary?.get("description") as? String ?: ""
                        expenses = (itinerary?.get("expenses") as? List<Map<String, Any>>)?.map {
                            Pair(it["description"] as? String ?: "", it["amount"] as? Double ?: 0.0)
                        } ?: emptyList()
                    } else {
                        errorMessage = "Destination not found"
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Error fetching itinerary: ${exception.message}"
                }

        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Edit Itinerary for $city", style = MaterialTheme.typography.headlineMedium)

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
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    // Display the expense description and amount
                    Text(
                        text = "${expense.first}: $${expense.second}",
                        modifier = Modifier.weight(1f)
                    )

                    // Trash icon to delete the expense
                    IconButton(
                        onClick = {
                            // Remove the expense from the list when the icon is clicked
                            expenses = expenses.filterNot { it == expense }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Expense",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                        .whereEqualTo("name", city)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
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

        Button(onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Back")
        }
    }
}
