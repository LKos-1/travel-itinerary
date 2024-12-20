package com.example.travelitinerary

import android.media.Image
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

@Composable
fun AddEntryPage(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedDestinations by remember { mutableStateOf(mutableListOf<String>()) }
    var currentDestinationInput by remember { mutableStateOf("") }
    var destinationSuggestions by remember { mutableStateOf(emptyList<String>()) }
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

        // Entry Name Field
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Entry Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Field
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Destinations Input with Fetching
        TextField(
            value = currentDestinationInput,
            onValueChange = { input ->
                currentDestinationInput = input
                if (input.length >= 2) { // Fetch suggestions when input has 2+ characters
                    fetchCitiesFromNominatim(input) { result ->
                        destinationSuggestions = result
                    }
                } else {
                    destinationSuggestions = emptyList()
                }
            },
            label = { Text("Search Destination") },
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn {
            items(destinationSuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!selectedDestinations.contains(suggestion)) {
                                selectedDestinations.add(suggestion)
                                currentDestinationInput = ""
                                destinationSuggestions = emptyList()
                            }
                        }
                        .padding(8.dp)
                )
            }
        }
        selectedDestinations.forEach { destination ->
            Text(text = destination, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rating Field
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(onClick = {
            val entry = hashMapOf(
                "name" to name,
                "date" to date,
                "destinations" to selectedDestinations,
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
fun fetchCitiesFromNominatim(query: String, onResult: (List<String>) -> Unit) {
    val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = URL(url).readText()
            val jsonArray = JSONArray(result)
            val cities = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val city = jsonArray.getJSONObject(i).optJSONObject("address")?.optString("city")
                val town = jsonArray.getJSONObject(i).optJSONObject("address")?.optString("town")
                if (city != null) cities.add(city)
                else if (town != null) cities.add(town)
            }

            withContext(Dispatchers.Main) {
                onResult(cities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(emptyList())
            }
        }
    }
}
@Composable
fun Chip(
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        label()
    }
}
