package com.example.electionpredictionapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electionpredictionapp.ui.theme.ElectionPredictionAppTheme
import com.example.electionpredictionapp.ui_composable.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElectionPredictionAppTheme {
                AppNavigation(this)
            }
        }
    }
}

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    val navcontroller = rememberNavController()

    NavHost(navController = navcontroller, startDestination = "home") {
        composable("login") {
            LoginScreen(
                onLogin = { email, password ->
                    Toast.makeText(
                        mainActivity, "Login tapped", Toast.LENGTH_SHORT
                    ).show()
                          },
                onRegister = { navcontroller.navigate("register")},
            )
        }

        composable("register") {
            RegisterScreen(
                ::emptyR, { navcontroller.navigate("login") }
            )
        }

        composable("home") {
            HomeScreen(
                { navcontroller.navigate("about_app") },{}
            )
        }

        composable("about_app") {
            AboutScreen({ navcontroller.navigate("home") })
        }

        composable("profile") {
            ProfileScreen(
                "John", "Doe", "Male", "1 January 2000",
                "Supporter",
                { navcontroller.navigate("home") },
                {})
        }
    }
}

fun emptyR(firstName: String, lastName: String, email: String, dateOfBirth: String, gender: String,  accountRole: String,
          password: String){}
