package com.example.travelitinerary

import android.media.Image
import android.os.Bundle
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


@Composable
fun EditProfilePage(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Load the current user's details if available
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            name = currentUser.displayName ?: ""
            email = currentUser.email ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Text Field for Name
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Text Field for Email
        /*TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))*/

        // Save Changes Button
        Button(
            onClick = {
                if (currentUser != null) {
                    // Save the updated profile information to Firebase
                    val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    // Update the user's name
                    currentUser.updateProfile(userProfileChangeRequest)
                        .addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Successfully updated name
                                println("Name updated")
                            } else {
                                println("Name update failed: ${profileTask.exception?.message}")
                            }
                        }

                    if (email != currentUser.email) {
                        currentUser.verifyBeforeUpdateEmail(email)
                            .addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    println("Verification email sent to $email. Please verify the new email.")

                                } else {
                                    println("Failed to send verification email: ${verifyTask.exception?.message}")
                                }
                            }
                    }

                    // pdate the user’s profile picture
                    navController.popBackStack()
                }
            }
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back Button to Profile Page
        Button(
            onClick = {
                navController.popBackStack() // Navigate back to Profile Page
            }
        ) {
            Text("Cancel")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePagePreview() {
    val navController = rememberNavController()
    EditProfilePage(navController)
}
