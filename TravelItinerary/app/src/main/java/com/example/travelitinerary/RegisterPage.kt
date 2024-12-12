package com.example.travelitinerary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create an Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                    errorMessage = "Please enter a valid email"
                    return@Button
                }

                if (password.text.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                isLoading = true
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()

                auth.createUserWithEmailAndPassword(email.text, password.text)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Create a Firestore document for the user
                                val userDoc = mapOf(
                                    "email" to user.email,
                                    "displayName" to user.displayName, // Optional, can be null
                                    "createdAt" to System.currentTimeMillis(),
                                    "entries" to emptyList<String>() // Placeholder for future entries
                                )

                                firestore.collection("users").document(user.uid).set(userDoc)
                                    .addOnSuccessListener {
                                        errorMessage = "Registration successful!"
                                        navController.navigate("login")
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to create user document: ${e.message}"
                                    }
                            } else {
                                errorMessage = "User creation failed. Please try again."
                            }
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "An error occurred"
                        }
                    }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Register")
            }
        }

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = if (errorMessage.contains("successful")) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login now")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    TravelItineraryTheme {
        val navController = rememberNavController()
        RegisterScreen(navController = navController)
    }
}
