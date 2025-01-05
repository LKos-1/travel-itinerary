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
fun ItineraryViewPage(
    navController: NavController,
    city: String,
    entryId: String? = null
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var itinerary by remember { mutableStateOf<Map<String, Any>?>(null) }
    var description by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf(listOf<Pair<String, Double>>()) }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Fetch itinerary data from Firestore
    LaunchedEffect(entryId) {
        if (entryId != null && currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("entries")
                .document(entryId)
                .collection("destinations")
                .document(city)  // Use the document ID for the city
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        itinerary = document["itinerary"] as? Map<String, Any>
                        if (itinerary != null) {
                            description = itinerary?.get("description") as? String ?: ""
                            val storedExpenses = itinerary?.get("expenses") as? List<Map<String, Any>> ?: emptyList()
                            expenses = storedExpenses.map { Pair(it["description"] as? String ?: "", it["amount"] as? Double ?: 0.0) }
                        } else {
                            errorMessage = "No itinerary found for this city."
                        }
                    } else {
                        errorMessage = "No destination document found."
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Failed to load itinerary: ${exception.message}"
                }
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Itinerary for $city", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Display error message if any
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Show itinerary details if available
        itinerary?.let {
            // Display description with editable TextField
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Display expenses with editable fields
            Text("Expenses:")
            expenses.forEachIndexed { index, expense ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = expense.first,
                        onValueChange = { newDescription ->
                            expenses = expenses.toMutableList().apply { this[index] = this[index].copy(first = newDescription) }
                        },
                        label = { Text("Expense Description") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    TextField(
                        value = expense.second.toString(),
                        onValueChange = { newAmount ->
                            expenses = expenses.toMutableList().apply {
                                val amount = newAmount.toDoubleOrNull() ?: expense.second
                                this[index] = this[index].copy(second = amount)
                            }
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add expense button
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = expenseDescription,
                    onValueChange = { expenseDescription = it },
                    label = { Text("New Expense Description") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                TextField(
                    value = expenseAmount,
                    onValueChange = { expenseAmount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (expenseAmount.isNotBlank() && expenseDescription.isNotBlank()) {
                        val amount = expenseAmount.toDoubleOrNull()
                        if (amount != null) {
                            expenses = expenses + Pair(expenseDescription, amount)
                            expenseDescription = ""
                            expenseAmount = ""
                            errorMessage = "" // Reset error message
                        } else {
                            errorMessage = "Invalid expense amount"
                        }
                    } else {
                        errorMessage = "Please fill in both fields"
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Add Expense")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Save itinerary changes
            Button(
                onClick = {
                    if (currentUserId != null && entryId != null) {
                        db.collection("users")
                            .document(currentUserId)
                            .collection("entries")
                            .document(entryId)
                            .collection("destinations")
                            .document(city)
                            .update(
                                "itinerary", mapOf(
                                    "description" to description,
                                    "expenses" to expenses.map { mapOf("description" to it.first, "amount" to it.second) }
                                )
                            )
                            .addOnSuccessListener {
                                navController.navigate("main-page") // Navigate back after saving
                            }
                            .addOnFailureListener { exception ->
                                errorMessage = "Failed to save itinerary: ${exception.message}"
                            }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save Changes")
            }

            // Delete itinerary button
            Button(
                onClick = {
                    if (currentUserId != null && entryId != null) {
                        db.collection("users")
                            .document(currentUserId)
                            .collection("entries")
                            .document(entryId)
                            .collection("destinations")
                            .document(city)
                            .update("itinerary", null) // Remove itinerary field
                            .addOnSuccessListener {
                                navController.navigate("main-page") // Navigate back after deleting
                            }
                            .addOnFailureListener { exception ->
                                errorMessage = "Failed to delete itinerary: ${exception.message}"
                            }
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete Itinerary", color = Color.White)
            }
        }
    }
}






