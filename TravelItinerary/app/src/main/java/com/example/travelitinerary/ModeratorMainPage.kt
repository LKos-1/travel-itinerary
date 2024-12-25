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
fun ModeratorMainPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter user email") }
        )

        Button(onClick = {
            // Fetch user document based on email
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val userId = document.id
                        // Update the user's role to moderator
                        assignRole(userId, "moderator", {
                            Log.d("ModeratorMainPage", "Role updated successfully")
                        }, { error ->
                            Log.e("ModeratorMainPage", "Error updating role: $error")
                        })
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ModeratorMainPage", "Error fetching user: ${e.message}")
                }
        }) {
            Text("Promote to Moderator")
        }

        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Main Page")
        }
    }
}

fun assignRole(userId: String, newRole: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("users").document(userId)
        .update("role", newRole)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Failed to update role")
        }
}

@Preview(showBackground = true)
@Composable
fun ModeratorMainPagePreview() {
    val navController = rememberNavController()
    ModeratorMainPage(navController = navController)
}