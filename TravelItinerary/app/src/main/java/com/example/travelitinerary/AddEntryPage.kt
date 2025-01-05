package com.example.travelitinerary

import android.media.Image
import android.os.Bundle

import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import com.google.firebase.firestore.FieldValue
//import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import java.util.Calendar


@Composable
fun AddEntryPage(navController: NavController, entryId: String? = null) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Date()) }
    var currentDestinationInput by remember { mutableStateOf("") }
    var selectedDestinations by remember { mutableStateOf(mutableListOf<String>()) }
    var destinationSuggestions by remember { mutableStateOf(emptyList<Destination>()) }
    var rating by remember { mutableStateOf("") }
    var itinerary by remember { mutableStateOf("") }
    var expense by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Fetch entry details if entryId is provided
    if (entryId != null) {
        LaunchedEffect(entryId) {
            currentUser?.let {
                firestore.collection("users")
                    .document(it.uid)
                    .collection("entries")
                    .document(entryId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val entry = document.data
                            name = entry?.get("name") as? String ?: ""
                            date = (entry?.get("date") as? Timestamp)?.toDate() ?: Date()
                            rating = entry?.get("rating")?.toString() ?: ""

                            // Fetch destinations from the subcollection
                            firestore.collection("users")
                                .document(it.uid)
                                .collection("entries")
                                .document(entryId)
                                .collection("destinations")
                                .get()
                                .addOnSuccessListener { destinationDocs ->
                                    selectedDestinations = destinationDocs.documents.map { it.getString("name") ?: "" }.toMutableList()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AddEntryPage", "Failed to fetch destinations: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e -> Log.e("AddEntryPage", "Failed to fetch entry: ${e.message}") }
            }
        }
    }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Select date", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val calendar = Calendar.getInstance().apply { time = date }
                    android.app.DatePickerDialog(
                        navController.context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            date = selectedCalendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date),
                    style = TextStyle(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Destination Input and Suggestions
        TextField(
            value = currentDestinationInput,
            onValueChange = { input ->
                currentDestinationInput = input
                if (input.length >= 2) {
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

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(destinationSuggestions) { city ->
                Text(
                    text = city.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!selectedDestinations.contains(city.name)) {
                                selectedDestinations.add(city.name)
                                currentDestinationInput = ""
                                destinationSuggestions = emptyList()
                            }
                        }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display selected destinations with delete option
        Column(modifier = Modifier.fillMaxWidth()) {
            selectedDestinations.forEach { destination ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = destination, style = MaterialTheme.typography.bodyMedium)

                    IconButton(onClick = {
                        selectedDestinations = selectedDestinations.filterNot { it == destination }.toMutableList()
                        // Delete the destination from Firestore
                        entryId?.let { id ->
                            currentUser?.let { user ->
                                val destinationsRef = firestore.collection("users")
                                    .document(user.uid)
                                    .collection("entries")
                                    .document(id)
                                    .collection("destinations")

                                // Find the destination document by name and delete it
                                destinationsRef.whereEqualTo("name", destination)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        val destinationDoc = querySnapshot.documents.firstOrNull()
                                        destinationDoc?.let {
                                            // If document is found, delete it
                                            destinationsRef.document(it.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Log.d("Firestore", "Destination deleted successfully: $destination")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Firestore", "Failed to delete destination: ${e.message}")
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error finding destination: ${e.message}")
                                    }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Destination",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (name.isBlank() || selectedDestinations.isEmpty() || rating.isBlank()) {
                Toast.makeText(navController.context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            } else {
                val ratingInt = rating.toIntOrNull()
                if (ratingInt == null || ratingInt !in 1..5) {
                    Toast.makeText(navController.context, "Please enter a rating between 1 and 5", Toast.LENGTH_SHORT).show()
                } else {
                    val entry = hashMapOf(
                        "name" to name,
                        "date" to Timestamp(date),
                        "rating" to ratingInt,
                    )

                    currentUser?.let { user ->
                        val userEntriesRef = firestore.collection("users").document(user.uid).collection("entries")
                        val entryTask = if (entryId != null) {
                            userEntriesRef.document(entryId).update(entry as Map<String, Any>)
                        } else {
                            userEntriesRef.add(entry)
                        }

                        entryTask.addOnSuccessListener { documentReference ->
                            // Destination subcollection reference
                            val destinationsRef = if (entryId != null) {
                                userEntriesRef.document(entryId).collection("destinations")
                            } else {
                                (documentReference as DocumentReference).collection("destinations")
                            }

                            // Check for existing destinations and only add new ones
                            selectedDestinations.forEach { destination ->
                                // Query if the destination already exists
                                destinationsRef.whereEqualTo("name", destination)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        if (querySnapshot.isEmpty) {
                                            // If the destination doesn't exist, add it
                                            destinationsRef.add(mapOf("name" to destination))
                                                .addOnSuccessListener {
                                                    Log.d("Firestore", "Destination added successfully: $destination")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Firestore", "Failed to add destination: ${e.message}")
                                                }
                                        } else {
                                            Log.d("Firestore", "Destination already exists: $destination")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error checking destination existence: ${e.message}")
                                    }
                            }

                            Toast.makeText(navController.context, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate("main-page")
                        }.addOnFailureListener { e ->
                            Toast.makeText(navController.context, "Failed to save entry: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }) {
            Text("Save Entry")
        }




        // Back Button
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

data class Destination(val name: String)

fun fetchCitiesFromNominatim(query: String, onResult: (List<Destination>) -> Unit) {
    val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1&accept-language=en"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = URL(url).readText()
            val jsonArray = JSONArray(result)
            val cities = mutableListOf<Destination>()

            for (i in 0 until jsonArray.length()) {
                val city = jsonArray.getJSONObject(i).optJSONObject("address")?.optString("city")
                val town = jsonArray.getJSONObject(i).optJSONObject("address")?.optString("town")
                if (city != null && city.isNotEmpty()) cities.add(Destination(city))
                else if (town != null && town.isNotEmpty()) cities.add(Destination(town))
            }

            withContext(Dispatchers.Main) {
                onResult(cities.distinctBy { it.name }) // Distinct by name to avoid duplicates
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(emptyList()) // In case of error, return an empty list
            }
        }
    }
}


@Composable
fun DateField(
    date: Date?,
    onDateChange: (Date) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val formattedDate = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "Select a date"

    // Date Field
    OutlinedTextField(
        value = formattedDate,
        onValueChange = { /* Ignore manual input */ },
        label = { Text("Date") },
        readOnly = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done  // Disable keyboard action
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Hide the keyboard
                keyboardController?.hide()

                // Trigger the DatePickerDialog
                val calendar = Calendar.getInstance().apply { time = date ?: Date() }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }
                        onDateChange(selectedCalendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    )
}