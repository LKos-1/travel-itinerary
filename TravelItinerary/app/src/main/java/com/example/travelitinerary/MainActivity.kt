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
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Setup NavHost
            val navController = rememberNavController()
            NavHost(navController, startDestination = "login") {
                composable("login") { LoginForm(navController) }
                composable("register") { RegisterForm(navController) }
            }
        }
    }
}

@Composable
fun LoginForm(navController: NavController) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Travel Itinerary!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp) // Space between this and the next content
        )

        Text(text = "Login Form")

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Password input field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = {
                if (username.text.isEmpty() || password.text.isEmpty()) {
                    // Display error message
                    errorMessage = "Please fill in both fields"
                } else {
                    errorMessage = "Login successful!"
                    navController.navigate("main-page")
                }
            }
        ) {
            Text("Log In")
        }

        // Show error or success message
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = if (errorMessage == "Login successful!") Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to register page
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Not a user? Register now")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TravelItineraryTheme {
        val navController = rememberNavController()
        LoginForm(navController = navController)
    }
}