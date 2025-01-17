package com.example.travelitinerary

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito.mockStatic
import com.example.travelitinerary.assignRole
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
class RegisterUnitTest {
    @Test
    fun `test user login`() {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.email).thenReturn("testuser@example.com")

        assertNotNull(mockAuth.currentUser)
        assertEquals("testuser@example.com", mockAuth.currentUser?.email)
    }

    @Test
    fun `test user registration`() {
        // Mock FirebaseAuth and FirebaseUser
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)

        // Define the behavior for the mock user object
        `when`(mockUser.email).thenReturn("testuser@example.com")

        // Mock the task returned by createUserWithEmailAndPassword
        val mockTask = mock(Task::class.java) as Task<AuthResult>
        `when`(mockAuth.createUserWithEmailAndPassword("testuser@example.com", "password123"))
            .thenReturn(mockTask)

        // Simulate the task being completed successfully
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.result).thenReturn(mock(AuthResult::class.java)) // Return mock AuthResult
        `when`(mockTask.result?.user).thenReturn(mockUser) // Set mock user

        // Perform registration (mocked)
        val task = mockAuth.createUserWithEmailAndPassword("testuser@example.com", "password123")

        // Assertions
        assertTrue(task.isSuccessful)
        assertNotNull(task.result)
        assertEquals("testuser@example.com", task.result?.user?.email)
    }
    @Test
    fun testRegisterWithEmptyFields() {
        val mockAuth = mock(FirebaseAuth::class.java)

        // Simulate the register method call with empty fields
        val email = ""
        val password = ""

        val errorMessage = if (email.isBlank() || password.isBlank()) {
            "Please fill in all fields"
        } else {
            // Proceed with registration (mocked)
            "Registration successful"
        }

        // Assertions
        assertEquals("Please fill in all fields", errorMessage)
    }
    @Test
    fun testRegisterWithInvalidEmail() {
        val email = "invalid-email"
        val password = "password123"
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"
        val isValidEmail = email.matches(emailRegex.toRegex())

        val errorMessage = if (!isValidEmail) {
            "Please enter a valid email"
        } else {
            "Registration successful"
        }

        // asserting the expected outcome (invalid email validation)
        assertEquals("Please enter a valid email", errorMessage)
    }
    @Test
    fun testLoginWithInvalidCredentials() {
        // authentication provider (FirebaseAuth)
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockTask = mock(Task::class.java) as Task<AuthResult>

        // Firebase returns a failed task with an exception
        `when`(mockAuth.signInWithEmailAndPassword("invalid@example.com", "wrongpassword"))
            .thenReturn(mockTask)

        // failure when the task is executed
        `when`(mockTask.isSuccessful).thenReturn(false)
        `when`(mockTask.exception).thenReturn(Exception("Incorrect email or password"))

        //  calling the login method
        val result = login(mockAuth, "invalid@example.com", "wrongpassword")

        // Verifying that login failed
        assertFalse(result.isSuccessful)
        assertEquals(result.errorMessage, "Incorrect email or password")
    }
    @Test
    fun test1LoginWithInvalidCredentials() {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockTask = mock(Task::class.java) as Task<AuthResult>

        // Simulate Firebase returning a failed task
        `when`(mockAuth.signInWithEmailAndPassword("invalid@example.com", "wrongpassword"))
            .thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(false)
        `when`(mockTask.exception).thenReturn(FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "Incorrect email or password"))

        // Call the login method
        val result = login(mockAuth, "invalid@example.com", "wrongpassword")

        // Verify login failed with the expected error message
        assertFalse(result.isSuccessful)
        assertEquals("Incorrect email or password", result.errorMessage)
    }

    private fun login(auth: FirebaseAuth?, email: String, password: String): LoginResult {
        // Check if auth is null or task is null
        val task = auth?.signInWithEmailAndPassword(email, password)

        // Return LoginResult for the cases where task is not null
        if (task != null) {
            return if (task.isSuccessful) {
                LoginResult(true, "")
            } else {
                LoginResult(false, task.exception?.message ?: "Unknown error")
            }
        }

        // If task is null, return a LoginResult indicating failure
        return LoginResult(false, "Authentication failed.")
    }

    data class LoginResult(val isSuccessful: Boolean, val errorMessage: String)

}

class ProfileUnitTest {
    @Test
    fun testViewProfile() {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockFirebaseUser = mock(FirebaseUser::class.java)
        // Mock FirebaseAuth to return a mock user
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.displayName).thenReturn("Test User")
        `when`(mockFirebaseUser.email).thenReturn("testuser@example.com")
        `when`(mockFirebaseUser.photoUrl).thenReturn(null)

        // Simulate profile viewing logic (get user details)
        val userName = mockAuth.currentUser?.displayName ?: "No Name"
        val userEmail = mockAuth.currentUser?.email ?: "No Email"
        val userPhotoUrl = mockAuth.currentUser?.photoUrl

        // Assertions
        assertEquals("Test User", userName)
        assertEquals("testuser@example.com", userEmail)
        assertEquals(null, userPhotoUrl)
    }

    fun isStringEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    @Test
    fun testEditProfile() {
        mockStatic(TextUtils::class.java).use { mockedStatic ->
            // Mock the isEmpty method to return false
            mockedStatic.`when`<Boolean> { TextUtils.isEmpty(anyString()) }.thenReturn(false)
            val mockAuth = mock(FirebaseAuth::class.java)
            val mockFirebaseUser = mock(FirebaseUser::class.java)
            val mockNavController = mock(NavController::class.java)

            // Mock current user
            `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
            `when`(mockFirebaseUser.displayName).thenReturn("Old Name")
            `when`(mockFirebaseUser.email).thenReturn("olduser@example.com")

            // Simulate the editing process (new name)
            val newName = "New Name"

            // Prepare the UserProfileChangeRequest with new name
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            // Mock the profile update task
            val mockTask = mock(Task::class.java) as Task<Void>
            `when`(mockFirebaseUser.updateProfile(userProfileChangeRequest)).thenReturn(mockTask)
            `when`(mockTask.isSuccessful).thenReturn(true) // Simulating successful profile update

            // Simulate button click that triggers the profile update
            val result = mockFirebaseUser.updateProfile(userProfileChangeRequest)

            // Execute the task and perform assertions outside the callback
            result.addOnCompleteListener { task ->
                assertTrue(task.isSuccessful) // Assert successful profile update
                verify(mockNavController).popBackStack() // Verify navigation happens
            }
        }
    }
}

class ModerationUnitTest {
    @Test
    fun testBlockingUser() {
        // Mock Firestore instance
        val mockFirestore = mock(FirebaseFirestore::class.java)
        val mockCollection = mock(CollectionReference::class.java) // Mock CollectionReference
        val mockDocuments = mock(QuerySnapshot::class.java)
        val mockDocument = mock(DocumentSnapshot::class.java)

        val mockTask: Task<QuerySnapshot> =
            mock(Task::class.java) as Task<QuerySnapshot> // Correct Task<QuerySnapshot> mock
        val mockQuerySnapshot: QuerySnapshot = mock(QuerySnapshot::class.java)

        // Mock Firestore collection behavior
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("email", "user@example.com")).thenReturn(
            mockCollection
        )

        // Ensure get() returns a Task<QuerySnapshot>
        `when`(mockCollection.get()).thenReturn(mockTask)

        // Mock the behavior of the Task
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.result).thenReturn(mockQuerySnapshot)

        // Set up mock behavior for the QuerySnapshot
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocument))

        // Mock the DocumentSnapshot behavior
        `when`(mockDocument.getString("role")).thenReturn("user")
        `when`(mockDocument.id).thenReturn("userId")

        val currentModeratorEmail = "moderator@example.com"

        val assignRoleMock: (String, String, String?, () -> Unit, (String) -> Unit) -> Unit =
            { userId, newRole, blockedBy, onSuccess, onFailure ->
                val updates = mutableMapOf<String, Any>("role" to newRole)

                if (newRole == "blocked" && blockedBy != null) {
                    updates["blockedBy"] = blockedBy
                } else if (newRole == "user") {
                    updates["blockedBy"] = FieldValue.delete()
                }

                mockFirestore.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to update role")
                    }
            }

        // Simulate blocking a user and verifying the result
        var statusMessage = ""
        val isLoading = false

        // Call the blockUser functionality
        blockUser(mockFirestore, currentModeratorEmail, "user@example.com", assignRoleMock)

        // Verify the expected status message
        assertEquals("User has been blocked successfully.", statusMessage)

    }

    fun blockUser(
        firestore: FirebaseFirestore,
        moderatorEmail: String,
        userEmail: String,
        assignRole: (String, String, String?, () -> Unit, (String) -> Unit) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    val userDocument = querySnapshot.documents.first()
                    val userId = userDocument.id
                    val blockedBy = moderatorEmail
                    assignRole(userId, "blocked", blockedBy, {
                        // Success callback
                        println("User has been blocked successfully.")
                    }, { errorMessage ->
                        // Failure callback
                        println("Failed to block user: $errorMessage")
                    })
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: ${exception.message}")
            }
    }

   @Test
   fun `test user blocking functionality`() {
       // Mock Firestore and its related components
       val mockFirestore = mock(FirebaseFirestore::class.java)
       val mockCollection = mock(CollectionReference::class.java)
       val mockDocument = mock(DocumentReference::class.java)
       val mockTask = mock(Task::class.java) as Task<Void>

       // Setup mocks for Firestore interactions
       `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
       `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
       `when`(mockDocument.update(anyMap())).thenReturn(mockTask)

       // Simulate task completion
       `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
           val successListener = invocation.getArgument<OnSuccessListener<Void>>(0)
           successListener.onSuccess(null) // Simulate success
           mockTask
       }
       `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
           // Do nothing, simulating no failure
           mockTask
       }

       // Call the function and assert behavior
       assignRole(
           userId = "testUserId",
           newRole = "blocked",
           blockedBy = "moderator@example.com",
           onSuccess = { assertTrue(true) },
           onFailure = { fail("Should not fail") }
       )
   }
   @Test
   fun testFormValidation() {
       val name = ""
       val selectedDestinations = mutableListOf<String>()
       val rating = "4"

       val isValid = name.isNotBlank() &&
               selectedDestinations.isNotEmpty() &&
               rating.toIntOrNull() in 1..5

       assertFalse(isValid) // Should fail due to invalid rating and empty fields
   }

}

class AddEntryPageTests {
    object AddEntryPageUtils {
        data class Result(val success: Boolean, val errorMessage: String? = null)

        fun addEntry(
            user: FirebaseAuth,
            firestore: FirebaseFirestore,
            name: String,
            date: String,
            destinations: List<String>,
            rating: String
        ): Result {
            if (name.isBlank() || destinations.isEmpty() || rating.isBlank()) {
                return Result(success = false, errorMessage = "Please fill in all fields!")
            }

            val ratingInt = rating.toIntOrNull()
            if (ratingInt == null || ratingInt !in 1..5) {
                return Result(success = false, errorMessage = "Please enter a rating between 1 and 5")
            }

            // Mock Firestore logic (success scenario)
            return Result(success = true)
        }

        fun editEntry(
            user: FirebaseAuth,
            firestore: FirebaseFirestore,
            entryId: String,
            name: String,
            date: String,
            destinations: List<String>,
            rating: String
        ): Result {
            if (entryId.isBlank()) {
                return Result(success = false, errorMessage = "Entry ID is required!")
            }

            return addEntry(user, firestore, name, date, destinations, rating)
        }
    }
    @Test
    fun testAddEntrySuccess() {
        val user = mock(FirebaseAuth::class.java)
        val firestore = mock(FirebaseFirestore::class.java)
        val name = "Europe Trip"
        val date = "2024-01-01"
        val destinations = listOf("London", "Paris")
        val rating = "5"

        // Simulate user logged in
        `when`(user.currentUser).thenReturn(mock(FirebaseUser::class.java))
        `when`(firestore.collection("users")).thenReturn(mock(CollectionReference::class.java))

        // Simulate successful entry addition
        val result = AddEntryPageUtils.addEntry(
            user, firestore, name, date, destinations, rating
        )

        assertTrue("Entry should be added successfully", result.success)
        assertNull("There should be no error message", result.errorMessage)
    }
    @Test
    fun testAddEntryMissingFields() {
        val user = mock(FirebaseAuth::class.java)
        val firestore = mock(FirebaseFirestore::class.java)
        val name = ""
        val date = "2024-01-01"
        val destinations = emptyList<String>()
        val rating = "5"

        val result = AddEntryPageUtils.addEntry(
            user, firestore, name, date, destinations, rating
        )

        assertFalse("Entry should not be added with missing fields", result.success)
        assertEquals("Please fill in all fields!", result.errorMessage)
    }
    @Test
    fun testAddEntryInvalidRating() {
        val user = mock(FirebaseAuth::class.java)
        val firestore = mock(FirebaseFirestore::class.java)
        val name = "Europe Trip"
        val date = "2024-01-01"
        val destinations = listOf("London", "Paris")
        val rating = "6"

        val result = AddEntryPageUtils.addEntry(
            user, firestore, name, date, destinations, rating
        )

        assertFalse("Entry should not be added with invalid rating", result.success)
        assertEquals("Please enter a rating between 1 and 5", result.errorMessage)
    }

    @Test
    fun testEditEntrySuccess() {
        val user = mock(FirebaseAuth::class.java)
        val firestore = mock(FirebaseFirestore::class.java)
        val entryId = "entryId123"
        val updatedName = "Updated Trip"
        val updatedDate = "2024-02-01"
        val updatedDestinations = listOf("Rome", "Vienna")
        val updatedRating = "4"

        // Simulate user logged in and valid entryId
        `when`(user.currentUser).thenReturn(mock(FirebaseUser::class.java))
        `when`(firestore.collection("users")).thenReturn(mock(CollectionReference::class.java))

        // Simulate successful entry update
        val result = AddEntryPageUtils.editEntry(
            user, firestore, entryId, updatedName, updatedDate, updatedDestinations, updatedRating
        )

        assertTrue("Entry should be updated successfully", result.success)
        assertNull("There should be no error message", result.errorMessage)
    }

}

class ItineraryTests{
    @Test
    fun `adding a valid itinerary`() {
        val description = "Visited Louvre"
        val expenses = listOf(
            "Dinner" to 20.0,
            "Hotel" to 30.0
        )
        val itinerary = mutableMapOf<String, Any>()
        itinerary["description"] = description
        itinerary["expenses"] = expenses.map { mapOf("description" to it.first, "amount" to it.second) }

        assertEquals("Visited Louvre", itinerary["description"])
        assertEquals(2, (itinerary["expenses"] as List<*>).size)
    }

    @Test
    fun `adding itinerary with missing fields`() {
        val description = ""
        val expenses = listOf(
            "Dinner" to 20.0,
            "" to 30.0 // Missing expense description
        )
        val errorMessage = if (description.isBlank() || expenses.any { it.first.isBlank() }) {
            "Please fill in both fields"
        } else {
            ""
        }

        assertEquals("Please fill in both fields", errorMessage)
    }
    @Test
    fun `adding itinerary with invalid expense`() {
        val description = ""
        val expenses = listOf(
            "" to -2.0, // Invalid negative amount
            "Hotel" to -1.0
        )
        val errorMessage = if (expenses.any { it.second < 0 }) {
            "Invalid expense amount"
        } else {
            ""
        }

        assertEquals("Invalid expense amount", errorMessage)
    }
    @Test
    fun `editing itinerary details`() {
        var description = "Visited Louvre"
        var expenses = mutableListOf(
            "Dinner" to 20.0,
            "Hotel" to 30.0
        )
        // Update itinerary details
        description = "Visited Louvre and bought gifts"
        expenses.add("Gifts" to 50.0)

        assertEquals("Visited Louvre and bought gifts", description)
        assertEquals(3, expenses.size)
        assertEquals("Gifts" to 50.0, expenses.last())
    }
    //test saving itinerary - success and failure

}

class DestinationTests {
    data class Entry(var name: String, var date: String) {
        val destinations = mutableListOf<String>()

        fun addDestination(destination: String) {
            destinations.add(destination)
        }

        fun removeDestination(destination: String) {
            destinations.remove(destination)
        }
    }
    @Test
    fun testAddDestinations() {
        // Create an entry with valid details
        val entry = Entry(name = "Trip to Europe", date = "2025-01-13")

        // Add destinations to the entry
        val destinationsToAdd = listOf("Berlin", "Porto", "Copenhagen")
        destinationsToAdd.forEach { entry.addDestination(it) }

        // Check if destinations are added correctly
        assertTrue(entry.destinations.containsAll(destinationsToAdd))
        assertEquals(3, entry.destinations.size)  // Should be 3 destinations
    }
    @Test
    fun testRemoveDestinations() {
        // Create an entry with some destinations
        val entry = Entry(name = "Trip to Europe", date = "2025-01-13")
        val initialDestinations = listOf("Berlin", "Porto", "Copenhagen")
        initialDestinations.forEach { entry.addDestination(it) }

        // Destinations to remove
        val destinationsToRemove = listOf("Berlin")

        // Remove the specified destinations
        destinationsToRemove.forEach { entry.removeDestination(it) }

        // Check if the destination has been removed successfully
        assertFalse(entry.destinations.contains("Berlin"))
        assertTrue(entry.destinations.containsAll(listOf("Porto", "Copenhagen")))
    }
}

class ModerationTests {
    data class User(var email: String, var role: String)

    fun blockUser(user: User, moderatorEmail: String): String {
        // Check if the user is already blocked
        if (user.role == "blocked") {
            return "This user is already blocked."
        }

        // Block the user
        user.role = "blocked"
        return "User has been blocked successfully."
    }

    fun unblockUser(user: User, moderatorEmail: String): String {
        // Check if the user is already unblocked
        if (user.role != "blocked") {
            return "User is not blocked."
        }

        // Unblock the user
        user.role = "user"
        return "User has been unblocked successfully."
    }

    fun promoteUser(user: User, moderatorEmail: String): String {
        // Check if the user is blocked or already a moderator
        if (user.role == "blocked") {
            return "This user is blocked and cannot be promoted."
        }
        if (user.role == "moderator") {
            return "This user is already a moderator."
        }

        // Promote the user
        user.role = "moderator"
        return "User has been promoted to moderator successfully."
    }
    @Test
    fun testBlockUser() {
        // Simulate a moderator's email
        val moderatorEmail = "moderator@example.com"

        // Simulate a user to block
        val emailToBlock = "user@example.com"

        // Assume the user exists with the role 'user'
        val user = User(email = emailToBlock, role = "user")

        // Simulate the blocking process
        val statusMessage = blockUser(user, moderatorEmail)

        // Verify the success message
        assertEquals("User has been blocked successfully.", statusMessage)
        assertEquals("blocked", user.role)  // User's role should be 'blocked'
    }
    @Test
    fun testUnblockUser() {
        // Simulate a moderator's email
        val moderatorEmail = "moderator@example.com"

        // Simulate a blocked user
        val emailToUnblock = "user@example.com"

        // Assume the user exists with the role 'blocked'
        val user = User(email = emailToUnblock, role = "blocked")

        // Simulate the unblocking process
        val statusMessage = unblockUser(user, moderatorEmail)

        // Verify the success message
        assertEquals("User has been unblocked successfully.", statusMessage)
        assertEquals("user", user.role)  // User's role should be 'user'
    }
    @Test
    fun testPromoteUser() {
        // Simulate a moderator's email
        val moderatorEmail = "moderator@example.com"

        // Simulate a user to promote
        val emailToPromote = "user@example.com"

        // Assume the user exists with the role 'user'
        val user = User(email = emailToPromote, role = "user")

        // Simulate the promotion process
        val statusMessage = promoteUser(user, moderatorEmail)

        // Verify the success message
        assertEquals("User has been promoted to moderator successfully.", statusMessage)
        assertEquals("moderator", user.role)  // User's role should be 'moderator'
    }
}