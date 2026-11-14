package com.example.electionpredictionapp.routes

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electionpredictionapp.MainActivity
import com.example.electionpredictionapp.auth.AuthRoutine
import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.ui_composable.*

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    val navcontroller = rememberNavController()
    val authRoutine = remember { AuthRoutine() }

    NavHost(navController = navcontroller, startDestination = "login") {
        val currentUser = FirebaseInstances.authInst.currentUser
        composable("login") {
            LoginScreen(
                onLogin = { email, password ->
                    authRoutine.userLogin(email, password) { success, error ->
                        if (success) {
                            navcontroller.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                mainActivity,
                                "Invalid credentials", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onRegister = { navcontroller.navigate("register")},
            )
        }

        composable("register") {
            RegisterScreen(
                onRegister = {
                        firstName, lastName, dateOfBirth, gender, accountRole, email, password ->
                    authRoutine.userRegister(
                        firstName, lastName, dateOfBirth, gender,
                        accountRole, email, password
                    ) { success, error ->
                        if (success) {
                            navcontroller.navigate("home") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            Toast.makeText(
                                mainActivity,
                                "Unsuccessful registration", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, { navcontroller.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                "John",
                { navcontroller.navigate("profile") },
                { navcontroller.navigate("about_app") }
            )
        }

        composable("about_app") {
            AboutScreen({ navcontroller.popBackStack() })
        }

        composable("profile") {
            ProfileScreen(
                "John", "Doe", "Male", "1 January 2000",
                "Supporter",
                { navcontroller.popBackStack() },
                {})
        }
    }
}
