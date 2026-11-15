package com.example.electionpredictionapp.routes

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electionpredictionapp.MainActivity
import com.example.electionpredictionapp.auth.AuthRoutine
import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.objects.*
import com.example.electionpredictionapp.ui_composable.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val USER_PROFILE = "userProfile"
    const val EDIT_USER_PROFILE = "editUserProfile"
    const val CHANGE_PASSWORD = "changePassword"
    const val ABOUT_APP = "aboutApp"
    const val VIEW_ALL_CAND_PROFILES = "viewAllCandProfiles"
    const val VIEW_OWN_CAND_PROFILE = "viewOwnCandProfile"
    const val EDIT_OWN_CAND_PROFILE = "editOwnCandProfile"
    const val VIEW_PREDICTIONS = "viewPredictions"
    const val VIEW_USERS = "viewUsers"
    const val VIEW_POSITIONS = "viewPositions"
    const val NEW_POSITION = "newPosition"
    const val VIEW_SURVEYS = "viewSurveys"
    const val NEW_SURVEY = "newSurvey"
}

object UserRepository {
    suspend fun getUser(uid: String): User? {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("users")
            .document(uid)
            .get().await()
        return if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }
}

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    val navController = rememberNavController()
    val authRoutine = remember { AuthRoutine() }

    var uid by remember { mutableStateOf(FirebaseInstances.authInst.currentUser?.uid) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    DisposableEffect (Unit) {
        val listener = FirebaseAuth.AuthStateListener { uid = it.currentUser?.uid }
        FirebaseInstances.authInst.addAuthStateListener(listener)
        onDispose { FirebaseInstances.authInst.removeAuthStateListener(listener) }
    }

    LaunchedEffect(uid) {
        loggedInUser = uid?.let { UserRepository.getUser(it) }
    }

    NavHost(navController, AppRoutes.LOGIN) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLogin = { email, password ->
                    authRoutine.userLogin(email, password) { success, _ ->
                        if (success) {
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                mainActivity,
                                "Invalid credentials",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRegister = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onRegister = { firstName, lastName, dateOfBirth, gender, accountRole, email, password ->
                    authRoutine.userRegister(
                        firstName, lastName, dateOfBirth, gender,
                        accountRole, email, password
                    ) { success, _ ->
                        if (success) {
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            Toast.makeText(
                                mainActivity,
                                "Unsuccessful registration", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, { navController.popBackStack() }
            )
        }

        composable(AppRoutes.HOME) {
            loggedInUser?.let {
                HomeScreen(
                    it.firstName,
                    it.accountRole,
                    { navController.navigate(AppRoutes.USER_PROFILE) },
                    { navController.navigate(AppRoutes.ABOUT_APP) },
                    { navController.navigate(AppRoutes.VIEW_USERS) },
                    { navController.navigate(AppRoutes.VIEW_POSITIONS) },
                    { navController.navigate(AppRoutes.VIEW_SURVEYS) },
                    { navController.navigate(AppRoutes.VIEW_POSITIONS) },
                    {},
                    emptyList(),
                    {},
                    {},
                )
            }
        }

        composable(AppRoutes.USER_PROFILE) {
            loggedInUser?.let {
                ProfileScreen(
                    it.firstName, it.lastName, it.gender, it.dateOfBirth, it.accountRole,
                    { navController.popBackStack() },
                    { navController.navigate(AppRoutes.CHANGE_PASSWORD) })
            }
        }

        composable(AppRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                { navController.popBackStack() },
                {_,_,_->}
            )
        }

        composable(AppRoutes.ABOUT_APP) {
            AboutScreen({ navController.popBackStack() })
        }

        composable(AppRoutes.VIEW_USERS) {
            ViewUsersScreen(
                emptyList(),
                { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_POSITIONS) {
            ViewPositionsScreen(
                emptyList(),
                {},
                {},
                { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_SURVEYS) {
            ViewSurveysScreen(
                emptyList(),
                {},
                {},
                { navController.popBackStack() }
            )
        }
    }
}