package com.example.travelitinerary

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.travelitinerary", appContext.packageName)
    }

}

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRegisterWithValidCredentials() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "register") {
                composable("register") { RegisterScreen(navController = navController) }
                composable("main-page") { MainPage(navController) }
                composable("login", content = { Text("Log In", modifier = Modifier.testTag("LoginForm")) })
            }
        }

        // Enter valid email and password
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Wait for navigation to happen
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("LoginForm").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify that the user has been redirected to the Login screen
        composeTestRule.onNodeWithTag("LoginForm").assertExists()
    }


    @Test
    fun testRegisterWithEmptyFields() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "register") {
                composable("register") { RegisterScreen(navController = navController) }
                composable("main-page") { MainPage(navController) }
            }
        }

        // Try to register without filling the fields
        composeTestRule.onNodeWithText("Register").performClick()

        // Verify that the error message is shown
        composeTestRule.onNodeWithText("Please fill in all fields").assertExists()
    }

    @Test
    fun testRegisterWithInvalidEmail() {
        // Launch the RegisterScreen
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "register") {
                composable("register") { RegisterScreen(navController = navController) }
                composable("main-page") { MainPage(navController) }
            }
        }

        // Enter an invalid email
        composeTestRule.onNodeWithText("Email").performTextInput("invalid-email")

        // Enter a valid password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Click the Register button
        composeTestRule.onNodeWithText("Register").performClick()

        // Verify that the error message appears
        composeTestRule.onNodeWithText("Please enter a valid email").assertExists()
    }
    @Test
    fun testLoginWithValidCredentials() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("register") { RegisterScreen(navController) }
                composable("main-page", content = { Text("Main Page", modifier = Modifier.testTag("MainPage")) }) // Mock main page
                composable("login") { LoginForm(navController) }
            }
        }

        // Enter valid email and password
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Click the Login button
        composeTestRule.onNodeWithText("Log In").performClick()

        // Verify the user is redirected to the main page
        composeTestRule.onNodeWithTag("MainPage").assertExists()

    }
    @Test
    fun testLoginWithInvalidCredentials() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("register") { RegisterScreen(navController) }
                composable("main-page") { MainPage(navController) }
                composable("login") { LoginForm(navController) }
            }
        }
        composeTestRule.onNodeWithText("Email").performTextInput("invalid@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Log In").performClick()

        // Wait for the error message to appear
        composeTestRule.onNodeWithText("Email").performTextInput("invalid@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Log In").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Incorrect email or password").fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("The supplied auth credential is incorrect, malformed or has expired.").fetchSemanticsNodes().isNotEmpty() ||
                    composeTestRule.onAllNodesWithText("Login failed. Please try again.").fetchSemanticsNodes().isNotEmpty()
        }
    }

}

class ModeratorScreenTest{
    @get:Rule
    val composeTestRule = createComposeRule()
    @Test
    fun testBlockUser() {
        composeTestRule.setContent {
            ModeratorMainPage(navController = rememberNavController(), currentModeratorEmail = "moderator@example.com")
        }

        // Enter a valid user email
        composeTestRule.onNodeWithText("Enter user email").performTextInput("user@example.com")

        // Click the Block User button
        composeTestRule.onNodeWithText("Block User").performClick()

        composeTestRule.setContent {
            ModeratorMainPage(navController = rememberNavController(), currentModeratorEmail = "moderator@example.com")
        }

        // verify that the success message is displayed
        composeTestRule.onNodeWithText("User has been blocked successfully.").assertExists()

        // Simulate the blocked user trying to log in
        composeTestRule.setContent {
            BlockedUserPage(blockedBy = "moderator@example.com", navController = rememberNavController())
        }

        // Verify the BlockedUserPage is displayed
        composeTestRule.onNodeWithText("You have been blocked.").assertExists()
    }
    @Test
    fun testUnblockUser() {
        composeTestRule.setContent {
            ModeratorMainPage(navController = rememberNavController(), currentModeratorEmail = "moderator@example.com")
        }

        // Enter a blocked user email
        composeTestRule.onNodeWithText("Enter user email").performTextInput("user@example.com")

        // Click the Unblock User button
        composeTestRule.onNodeWithText("Unblock User").performClick()

        // Verify that the success message is displayed
        composeTestRule.onNodeWithText("User has been unblocked successfully.").assertExists()

        // Simulate the unblocked user trying to log in
        composeTestRule.setContent {
        }

        // Verify the user can now access their account
    }
    @Test
    fun testPromoteUser() {
        // Launch ModeratorMainPage
        composeTestRule.setContent {
            ModeratorMainPage(navController = rememberNavController(), currentModeratorEmail = "moderator@example.com")
        }

        // Enter a regular user email
        composeTestRule.onNodeWithText("Enter user email").performTextInput("user@example.com")

        // Click the Promote to Moderator button
        composeTestRule.onNodeWithText("Promote to Moderator").performClick()

        // Verify that the success message is displayed
        composeTestRule.onNodeWithText("User has been promoted to moderator successfully.").assertExists()

        // Verify that the user now has moderator privileges
        composeTestRule.setContent {
            // Verify access to moderator actions
        }
    }
    @Test
    fun testAddValidEntry() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "add-entry") {
                composable("add-entry") { AddEntryPage(navController) }
                composable("main-page") { MainPage(navController) }
            }
        }

        composeTestRule.onNodeWithText("Entry Name").performTextInput("Europe Trip")
        composeTestRule.onNodeWithText("Search Destination").performTextInput("London")
        composeTestRule.onNodeWithText("Rating (1-5)").performTextInput("5")

        composeTestRule.onNodeWithText("Save Entry").performClick()

        composeTestRule.onNodeWithText("Entry saved successfully!")
            .assertIsDisplayed()
    }

}

class ProfileTests {
    @get:Rule
    val composeTestRule = createComposeRule()

}