package com.example.travelitinerary
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res -> onSignInResult(res) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "login") {
                composable("login") { LoginForm(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("main-page") { MainPage(navController) }
                composable("moderator-main-page/{email}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ModeratorMainPage(navController = navController, currentModeratorEmail = email)
                }
                composable("blocked-page/{email}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    NavigateToBlockedPage(email = email, navController = navController)
                }
                composable("profile") { ProfilePage(navController) }
                composable("edit-profile") { EditProfilePage(navController) }
                composable("add-entry") { AddEntryPage(navController) }
                composable("add-entry/{entryId}") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                    AddEntryPage(navController = navController, entryId = entryId)
                }
                composable("entry-detail/{entryId}") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                    if (entryId.isNotBlank()) {
                        EntryDetailPage(navController = navController, entryId = entryId)
                    } else {
                        Text("Invalid entry ID", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                composable("add-itinerary/{entryId}/{selectedCities}") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                    //val entryId = navController.currentBackStackEntry?.arguments?.getString("entryId") ?: ""
                    val selectedCitiesString = backStackEntry.arguments?.getString("selectedCities") ?: ""
                    val selectedCities = selectedCitiesString.split(",").map { it.trim() }.toMutableList() // Convert the string back to a list
                    ItineraryOverviewPage(navController = navController, entryId = entryId, selectedCities = selectedCities)
                }
                composable("itinerary-view/{city}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    if (city.isNotBlank()) {
                        ItineraryViewPage(navController = navController, city = city)
                    } else {
                        Text("Invalid city", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                composable("itinerary-details/{city}/{entryId}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: ""

                    if (city.isNotBlank() && entryId.isNotBlank()) {
                        ItineraryDetailsPage(navController = navController, city = city, entryId = entryId)
                    } else {
                        Text("Invalid city or entryId", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                composable("edit-itinerary/{city}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    if (city.isNotBlank()) {
                        EditItineraryPage(navController = navController, city = city)
                    } else {
                        Text("Invalid city", style = MaterialTheme.typography.bodyLarge)
                    }
                }

            }
        }
    }

    fun launchFirebaseLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Get Firestore instance
                val firestore = FirebaseFirestore.getInstance()
                // Reference to the user's Firestore document
                val userDocRef = firestore.collection("users").document(user.uid)

                // Fetch the user's role from Firestore
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val role = document.getString("role") ?: "user" // Default to "user" if role is missing
                            setContent {
                                val navController = rememberNavController()
                                when (role) {
                                    "moderator" -> navController.navigate("moderator-main-page/${user.email}")
                                    "user" -> navController.navigate("main-page")
                                    "blocked" -> navController.navigate("blocked-page/${user.email}")
                                    else -> {
                                        // Handle unknown role (e.g., navigate to a generic error page or log out)
                                        Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            // Document doesn't exist or user role not found
                            Toast.makeText(this, "User data not found. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            if (response == null) {
                Toast.makeText(this, "Sign-in canceled", Toast.LENGTH_SHORT).show()
            } else {
                val errorCode = response.error?.message ?: "Unknown error"
                Toast.makeText(this, "Sign-in failed: $errorCode", Toast.LENGTH_LONG).show()
            }
        }
    }

}

@Composable
fun LoginForm(navController: NavController) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
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

        Button(onClick = {
            if (email.text.isNotEmpty() && password.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.text, password.text)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                val firestore = FirebaseFirestore.getInstance()
                                val userDocRef = firestore.collection("users").document(user.uid)

                                // Fetch the user's role from Firestore
                                userDocRef.get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val role = document.getString("role") ?: "user" // Default to "user" if role is missing
                                            when (role) {
                                                "moderator" -> {
                                                    navController.navigate("moderator-main-page/${user.email}") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                                "user" -> {
                                                    navController.navigate("main-page") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                                "blocked" -> {
                                                    navController.navigate("blocked-page/${user.email}") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                            }
                                        } else {
                                            errorMessage = "User data not found. Please contact support."
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to fetch user data: ${e.message}"
                                    }
                            }
                        } else {
                            errorMessage = task.exception?.localizedMessage ?: "Login failed"
                        }
                    }
            } else {
                errorMessage = "Please fill in all fields"
            }
        }) {
            Text("Log In")
        }


        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Not a user? Register now")
        }
    }
}
@Composable
fun NavigateToBlockedPage(email: String, navController: NavController) {
    var blockedBy by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch the blockedBy data
    LaunchedEffect(email) {
        fetchBlockedBy(email, { fetchedBlockedBy ->
            blockedBy = fetchedBlockedBy
        }, { error ->
            errorMessage = error
        })
    }

    // Display error message if there is any, otherwise pass blockedBy to BlockedUserPage
    if (errorMessage != null) {
        Text("Error: $errorMessage", color = Color.Red)
    } else {
        // Pass the blockedBy value to BlockedUserPage
        BlockedUserPage(blockedBy = blockedBy, navController = navController)
    }
}
fun fetchBlockedBy(email: String, onSuccess: (String?) -> Unit, onFailure: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.documents.first()
                val blockedBy = document.getString("blockedBy")
                onSuccess(blockedBy)
            } else {
                onSuccess(null) // User not found
            }
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Failed to fetch blockedBy")
        }
}
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val navController = rememberNavController()
    LoginForm(navController)
}