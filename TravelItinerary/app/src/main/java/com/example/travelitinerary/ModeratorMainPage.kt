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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ModeratorMainPage(navController: NavController, currentModeratorEmail: String) {
    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Moderator Panel",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp) // Add spacing after title
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter user email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Block User Button
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    isLoading = true
                    statusMessage = ""
                    // Fetch user document based on email
                    firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents.first()
                                val userId = document.id
                                val role = document.getString("role") ?: "user"
                                val blockedBy = document.getString("blockedBy")

                                // Check if the user is a moderator
                                if (role == "moderator") {
                                    statusMessage = "Cannot block another moderator."
                                } else if (currentModeratorEmail == email) {
                                    statusMessage = "You cannot block yourself."
                                } else if (role == "blocked") {
                                    statusMessage = "This user is already blocked."
                                } else {
                                    assignRole(
                                        userId,
                                        "blocked",
                                        blockedBy = currentModeratorEmail,
                                        onSuccess = {
                                            statusMessage = "User has been blocked successfully."
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Error blocking user: $error"
                                        }
                                    )
                                }
                            } else {
                                statusMessage = "No user found with the provided email."
                            }
                        }
                        .addOnFailureListener { e ->
                            statusMessage = "Error fetching user: ${e.message}"
                        }
                        .addOnCompleteListener {
                            isLoading = false
                        }
                } else {
                    statusMessage = "Please enter an email."
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Block User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Unblock User Button
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    isLoading = true
                    statusMessage = ""
                    // Fetch user document based on email
                    firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents.first()
                                val userId = document.id
                                val role = document.getString("role") ?: "user"

                                if (role == "moderator") {
                                    statusMessage = "Cannot unblock a moderator."
                                } else if (currentModeratorEmail == email) {
                                    statusMessage = "You cannot unblock yourself."
                                } else if (role == "blocked") {
                                    // Clear the "blockedBy" field when unblocking
                                    assignRole(
                                        userId,
                                        "user",
                                        blockedBy = null, // Clear blockedBy when unblocking
                                        onSuccess = {
                                            statusMessage = "User has been unblocked successfully."
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Error unblocking user: $error"
                                        }
                                    )
                                } else {
                                    statusMessage = "User is not blocked."
                                }
                            } else {
                                statusMessage = "No user found with the provided email."
                            }
                        }
                        .addOnFailureListener { e ->
                            statusMessage = "Error fetching user: ${e.message}"
                        }
                        .addOnCompleteListener {
                            isLoading = false
                        }
                } else {
                    statusMessage = "Please enter an email."
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unblock User")
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Promote User Button
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    isLoading = true
                    statusMessage = ""
                    // Fetch user document based on email
                    firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents.first()
                                val userId = document.id
                                val role = document.getString("role") ?: "user"
                                val blockedBy = document.getString("blockedBy")

                                // Check if the user is already a moderator
                                if (role == "moderator") {
                                    statusMessage = "This user is already a moderator."
                                } else if (role == "blocked") {
                                    statusMessage = "This user is blocked and cannot be promoted."
                                } else if (currentModeratorEmail == email) {
                                    statusMessage = "You cannot promote yourself."
                                } else {
                                    assignRole(
                                        userId,
                                        "moderator",
                                        blockedBy = null, // Clear blockedBy when promoting
                                        onSuccess = {
                                            statusMessage = "User has been promoted to moderator successfully."
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Error promoting user: $error"
                                        }
                                    )
                                }
                            } else {
                                statusMessage = "No user found with the provided email."
                            }
                        }
                        .addOnFailureListener { e ->
                            statusMessage = "Error fetching user: ${e.message}"
                        }
                        .addOnCompleteListener {
                            isLoading = false
                        }
                } else {
                    statusMessage = "Please enter an email."
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Promote to Moderator")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Message
        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                color = if (statusMessage.contains("successfully")) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("main-page") { inclusive = true }
                }
            },
        ) {
            Text("Sign Out")
        }
    }
}

fun assignRole(userId: String, newRole: String, blockedBy: String? = null, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val updates = mutableMapOf<String, Any>("role" to newRole)

    if (newRole == "blocked" && blockedBy != null) {
        updates["blockedBy"] = blockedBy
    } else if (newRole == "user") {
        // Remove the blockedBy field completely from Firestore when unblocking
        updates["blockedBy"] = FieldValue.delete()
    }

    firestore.collection("users").document(userId)
        .update(updates)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Failed to update role")
        }
}


