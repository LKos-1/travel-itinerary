package com.example.travelitinerary

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp

@Composable
fun ItineraryOverviewPage(
    navController: NavController,
    entryId: String,
    selectedCities: MutableList<String> // Use the parameter instead of re-declaring it
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val loading = remember { mutableStateOf(true) } // Track loading state
    Log.d("Itinerary", "User ID: $userId, Entry ID: $entryId")

    // Fetching the selected cities from Firestore
    LaunchedEffect(entryId) {
        // Start by clearing the list before fetching
        selectedCities.clear()

        // Firestore query to fetch cities
        db.collection("users")
            .document(userId ?: "")
            .collection("entries")
            .document(entryId)
            .collection("destinations")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("Itinerary", "Fetched ${querySnapshot.size()} destinations.")
                if (!querySnapshot.isEmpty) {
                    // Iterate over the documents and add city names to the list
                    for (document in querySnapshot.documents) {
                        val cityName = document.getString("name") // Get the city name field
                        if (cityName != null) {
                            selectedCities.add(cityName)
                            Log.d("Itinerary", "Fetched city: $cityName") // Log fetched city name
                        } else {
                            Log.e("Itinerary", "City name is missing in document ${document.id}")
                        }
                    }
                    loading.value = false // Set loading to false once data is fetched
                } else {
                    Log.e("Itinerary", "No destinations found for this entry.")
                    loading.value = false // No destinations, so loading is done
                }
            }
            .addOnFailureListener { e ->
                Log.e("Itinerary", "Failed to fetch destinations: ${e.message}")
                loading.value = false // Set loading to false in case of failure
            }
    }

    // Show loading spinner while fetching data
    if (loading.value) {
        CircularProgressIndicator() // Show a progress indicator while data is loading
        return // Return early while loading
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select a City", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp)) // Add some space above the cities list

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between items
        ) {
            items(selectedCities) { city -> // Loop through selectedCities list to display cities
                // Card with a border and padding
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d("Itinerary", "Querying destinations for city: $city")

                            db.collection("users")
                                .document(userId ?: "")
                                .collection("entries")
                                .document(entryId)
                                .collection("destinations")
                                .whereEqualTo("name", city) // Query by the city name field
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    Log.d("Itinerary", "Query result size: ${querySnapshot.size()}")
                                    if (!querySnapshot.isEmpty) {
                                        val destinationDoc = querySnapshot.documents[0] // Get the first document
                                        val destinationId = destinationDoc.id // Get the unique document ID (destinationId)
                                        if (destinationDoc.contains("itinerary")) {
                                            navController.navigate("itinerary-view/$city")
                                        } else {
                                            // If no itinerary found, navigate to itinerary details page
                                            navController.navigate("itinerary-details/$city")
                                        }
                                    } else {
                                        Log.e("Itinerary", "No destination found with city name: $city")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Itinerary", "Failed to fetch destination: ${e.message}")
                                }
                        }
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp), // Rounded corners
                    border = BorderStroke(2.dp, Color.LightGray) // Border color
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD3B7E0)) // Light purple background
                            .padding(16.dp), // Padding inside the card
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = city,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black // Text color
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom

        // Back Button at the bottom of the screen
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add padding from the bottom
        ) {
            Text("Back")
        }
    }
}








