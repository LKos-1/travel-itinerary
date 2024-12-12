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
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.travelitinerary.ui.theme.TravelItineraryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddEntryPage(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var destinations by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add New Entry", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Entry Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = destinations,
            onValueChange = { destinations = it },
            label = { Text("Destinations (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val entry = hashMapOf(
                "name" to name,
                "date" to date,
                "destinations" to destinations.split(",").map { it.trim() },
                "rating" to (rating.toIntOrNull() ?: 0)
            )

            currentUser?.let { user ->
                firestore.collection("users")
                    .document(user.uid)
                    .collection("entries")
                    .add(entry)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context, "Entry added successfully!", Toast.LENGTH_SHORT).show()
                        navController.navigate("main-page")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(navController.context, "Failed to add entry: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }) {
            Text("Save Entry")
        }
    }
}
