package com.example.electionpredictionapp.routes

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electionpredictionapp.MainActivity
import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.*
import com.example.electionpredictionapp.repositories.*
import com.example.electionpredictionapp.ui_composable.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    var uid by remember { mutableStateOf(FirebaseInstances.authInst.currentUser?.uid) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allPositions by remember { mutableStateOf<List<Position>>(emptyList()) }
    var allSurveys by remember { mutableStateOf<List<SurveyDisplayItem>>(emptyList()) }

    var ownCandidacy by remember { mutableStateOf(Candidacy()) }
    var allCandidacies by remember { mutableStateOf<List<CandidacyDisplayItem>>(emptyList()) }

    var openSurveys by remember { mutableStateOf<List<SurveyDisplayItem>>(emptyList()) }
    var selectedOpenSurvey by remember { mutableStateOf<SurveyDisplayItem?>(null) }

    var allPredictions by remember { mutableStateOf<List<Prediction>>(emptyList()) }

    DisposableEffect (Unit) {
        val listener = FirebaseAuth.AuthStateListener { uid = it.currentUser?.uid }
        FirebaseInstances.authInst.addAuthStateListener(listener)
        onDispose { FirebaseInstances.authInst.removeAuthStateListener(listener) }
    }

    LaunchedEffect(uid) {
        loggedInUser = uid?.let { UserRepository.getUser(it) }
    }

    LaunchedEffect(uid, loggedInUser?.accountRole) {
        if (uid != null && loggedInUser?.accountRole == "Supporter") {
            openSurveys = SurveyRepository.getUnansweredOpenSurveys(uid!!)
        } else if (uid == null) {
            openSurveys = emptyList()
        }
    }

    NavHost(navController, AppRoutes.LOGIN) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLogin = { email, password ->
                    AuthRepository.userLogin(email, password) { success, _ ->
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
                    AuthRepository.userRegister(
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
                    { navController.navigate(AppRoutes.LOGOUT) },
                    { navController.navigate(AppRoutes.VIEW_USERS) },
                    { navController.navigate(AppRoutes.VIEW_POSITIONS) },
                    { navController.navigate(AppRoutes.VIEW_SURVEYS) },
                    { navController.navigate(AppRoutes.VIEW_OWN_CANDIDACY) },
                    { navController.navigate(AppRoutes.VIEW_PREDICTIONS) },
                    openSurveys,
                    { survey ->
                        selectedOpenSurvey = survey
                        navController.navigate(AppRoutes.FILL_SURVEY)
                    },
                    { navController.navigate(AppRoutes.VIEW_ALL_CANDIDACIES) }
                )
            }
        }

        composable(AppRoutes.USER_PROFILE) {
            loggedInUser?.let {
                ProfileScreen(
                    it,
                    { navController.popBackStack() },
                    { navController.navigate(AppRoutes.CHANGE_PASSWORD) })
            }
        }

        composable(AppRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                { navController.popBackStack() },
                { oldPassword, newPassword, confirmPassword ->
                    AuthRepository.userChangePassword(oldPassword, newPassword, confirmPassword) { success, error ->
                        if (success) {
                            Toast.makeText(
                                mainActivity,
                                "Password changed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                mainActivity,
                                error ?: "Password change failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        composable(AppRoutes.ABOUT_APP) {
            AboutScreen({ navController.popBackStack() })
        }

        composable(AppRoutes.LOGOUT) {
            AuthRepository.userLogout { success, error ->
                if (success) {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    Toast.makeText(
                        mainActivity,
                        error ?: "Logout failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        composable(AppRoutes.FILL_SURVEY) {
            LaunchedEffect(uid) {
                if (uid != null) {
                    if (allCandidacies.isEmpty()) {
                        allCandidacies = CandidacyRepository.getAllCandidacies()
                    }
                    if (allPositions.isEmpty()) {
                        allPositions = PositionRepository.getAllPositions()
                    }
                }
            }

            FillSurveyScreen(
                candidacies = allCandidacies,
                positions = allPositions,
                onCancel = { navController.popBackStack() },
                onSubmit = { surveyResponse ->
                    coroutineScope.launch {
                        SurveyRepository.submitSurveyResponse(
                            selectedOpenSurvey!!.id,
                            loggedInUser!!,
                            surveyResponse,
                            allCandidacies
                        )
                        openSurveys = openSurveys.filterNot { it.id == selectedOpenSurvey!!.id }
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(AppRoutes.VIEW_ALL_CANDIDACIES) {
            LaunchedEffect(uid) {
                allCandidacies = if (uid != null) {
                    CandidacyRepository.getAllCandidacies()
                } else {
                    emptyList()
                }
            }
            ViewAllCandidaciesScreen(
                candidacies = allCandidacies,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_OWN_CANDIDACY) {
            LaunchedEffect(uid) {
                if (uid != null) {
                    ownCandidacy = CandidacyRepository.getOwnCandidacy(uid!!)
                    allPositions = PositionRepository.getAllPositions()
                }
            }

            ViewOwnCandidacyScreen(
                candidacy = ownCandidacy,
                positions = allPositions,
                onEditCandidacy = { _, positionName, manifesto ->
                    val userId = uid ?: return@ViewOwnCandidacyScreen
                    coroutineScope.launch {
                        CandidacyRepository.saveOwnCandidacy(userId, positionName, manifesto)
                        ownCandidacy = Candidacy(
                            id = userId,
                            positionName = positionName,
                            manifesto = manifesto
                        )
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_PREDICTIONS) {
            LaunchedEffect(uid) {
                if (uid != null) {
                    if (allCandidacies.isEmpty()) {
                        allCandidacies = CandidacyRepository.getAllCandidacies()
                    }
                    allPredictions = PredictionRepository.getAllPredictions()
                } else {
                    allPredictions = emptyList()
                }
            }

            ViewPredictionsScreen(
                candidates = allCandidacies,
                predictions = allPredictions,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_USERS) {
            LaunchedEffect(uid) {
                allUsers = if (uid != null) UserRepository.getAllUsers() else emptyList()
            }
            ViewUsersScreen(
                allUsers,
                { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_POSITIONS) {
            LaunchedEffect(uid) {
                allPositions = if (uid != null) PositionRepository.getAllPositions() else emptyList()
            }
            ViewPositionsScreen(
                allPositions,
                { name ->
                    coroutineScope.launch {
                        PositionRepository.addPosition(name)
                        allPositions = PositionRepository.getAllPositions()
                    }
                },
                { positionId, positionName ->
                    coroutineScope.launch {
                        PositionRepository.updatePosition(positionId, positionName)
                        allPositions = PositionRepository.getAllPositions()
                    }
                },
                { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VIEW_SURVEYS) {
            LaunchedEffect(uid) {
                if (uid != null) {
                    allSurveys = SurveyRepository.getSurveyDisplayItems()
                }
            }
            ViewSurveysScreen(
                allSurveys,
                { name, openStatus ->
                    coroutineScope.launch {
                        SurveyRepository.addSurvey(name, openStatus)
                        allSurveys = SurveyRepository.getSurveyDisplayItems()
                    }
                },
                { surveyId, name, openStatus ->
                    coroutineScope.launch {
                        SurveyRepository.updateSurvey(surveyId, name, openStatus)
                        allSurveys = SurveyRepository.getSurveyDisplayItems()
                    }
                },
                { navController.popBackStack() }
            )
        }
    }
}
