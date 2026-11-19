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
import com.example.electionpredictionapp.auth.AuthRoutine
import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.objects.*
import com.example.electionpredictionapp.ui_composable.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val USER_PROFILE = "userProfile"
    const val EDIT_USER_PROFILE = "editUserProfile"
    const val CHANGE_PASSWORD = "changePassword"
    const val ABOUT_APP = "aboutApp"
    const val LOGOUT = "logout"
    const val FILL_SURVEY = "fillSurvey"
    const val VIEW_ALL_CANDIDACIES = "viewAllCandidacies"
    const val VIEW_OWN_CANDIDACY = "viewOwnCandidacy"
    // const val EDIT_OWN_CAND_PROFILE = "editOwnCandProfile"
    const val VIEW_PREDICTIONS = "viewPredictions"
    const val VIEW_USERS = "viewUsers"
    const val VIEW_POSITIONS = "viewPositions"
    // const val NEW_POSITION = "newPosition"
    const val VIEW_SURVEYS = "viewSurveys"
    // const val NEW_SURVEY = "newSurvey"
}

object UserRepository {
    suspend fun getUser(uid: String): User? {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("users")
            .document(uid)
            .get().await()
        return if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }
    suspend fun getAllUsers(): List<User> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("users")
            .get().await()
        return snapshot.toObjects(User::class.java)
    }
}

object PositionRepository {
    suspend fun getAllPositions(): List<Position> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("positions")
            .get().await()
        return snapshot.toObjects(Position::class.java)
    }
    suspend fun addPosition(name: String) {
        FirebaseInstances.firestoreInst
            .collection("positions")
            .add(mapOf("name" to name))
            .await()
    }
    suspend fun updatePosition(positionId: String, newName: String) {
        FirebaseInstances.firestoreInst
            .collection("positions")
            .document(positionId)
            .update("name", newName)
            .await()
    }
}

object SurveyRepository {
    suspend fun getSurveyDisplayItems(): List<SurveyDisplayItem> {
        val surveySnapshots = FirebaseInstances.firestoreInst
            .collection("surveys").get().await()
        return surveySnapshots.documents.map { doc ->
            val surveyId = doc.id
            val surveyName = doc.getString("name") ?: ""
            val positionName = doc.getString("positionName") ?: ""
            val openStatus = doc.getString("openStatus") ?: ""
            val responseCountQuery = FirebaseInstances.firestoreInst
                .collection("surveys")
                .document(surveyId)
                .collection("responses")
                .count()
                .get(AggregateSource.SERVER)
                .await()
            val responseCount = responseCountQuery.count.toInt()
            SurveyDisplayItem(
                id = surveyId,
                name = surveyName,
                positionName = positionName,
                openStatus = openStatus,
                responseCount = responseCount
            )
        }
    }

    suspend fun addSurvey(name: String, positionName: String, openStatus: String) {
        val surveyData = mapOf(
            "name" to name,
            "positionName" to positionName,
            "openStatus" to openStatus
        )
        FirebaseInstances.firestoreInst.collection("surveys")
            .add(surveyData).await()
    }

    suspend fun updateSurvey(
        surveyId: String, name: String, positionName: String, openStatus: String
    ) {
        val updates = mapOf(
            "name" to name,
            "positionName" to positionName,
            "openStatus" to openStatus
        )
        FirebaseInstances.firestoreInst.collection("surveys")
            .document(surveyId).update(updates)
            .await()
    }
}

object CandidacyRepository {
    suspend fun getOwnCandidacy(uid: String): Candidacy {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("candidacies").document(uid).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(Candidacy::class.java) ?: Candidacy(uid)
        } else {
            Candidacy(id = uid)
        }
    }

    suspend fun saveOwnCandidacy(uid: String, positionName: String, manifesto: String) {
        val data = mapOf(
            "positionName" to positionName,
            "manifesto" to manifesto
        )
        FirebaseInstances.firestoreInst.collection("candidacies")
            .document(uid).set(data, SetOptions.merge()).await()
    }

    suspend fun getAllCandidacies(): List<CandidacyDisplayItem> {
        val candidacySnapshots = FirebaseInstances.firestoreInst
            .collection("candidacies").get().await()
        val userSnapshots = FirebaseInstances.firestoreInst
            .collection("users").get().await()

        val usersById = userSnapshots.documents.associate { doc ->
            doc.id to doc.toObject(User::class.java)
        }

        return candidacySnapshots.documents.mapNotNull { doc ->
            val candidacy = doc.toObject(Candidacy::class.java) ?: return@mapNotNull null
            val candidacyId = candidacy.id.ifBlank { doc.id }
            val user = usersById[candidacyId]

            CandidacyDisplayItem(
                id = candidacyId,
                firstName = user?.firstName ?: "",
                lastName = user?.lastName ?: "",
                positionName = candidacy.positionName,
                manifesto = candidacy.manifesto
            )
        }
    }
}

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    val navController = rememberNavController()
    val authRoutine = remember { AuthRoutine() }
    val coroutineScope = rememberCoroutineScope()

    var uid by remember { mutableStateOf(FirebaseInstances.authInst.currentUser?.uid) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allPositions by remember { mutableStateOf<List<Position>>(emptyList()) }
    var allSurveys by remember { mutableStateOf<List<SurveyDisplayItem>>(emptyList()) }

    var ownCandidacy by remember { mutableStateOf(Candidacy()) }
    var allCandidacies by remember { mutableStateOf<List<CandidacyDisplayItem>>(emptyList()) }

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
                    { navController.navigate(AppRoutes.LOGOUT) },
                    { navController.navigate(AppRoutes.VIEW_USERS) },
                    { navController.navigate(AppRoutes.VIEW_POSITIONS) },
                    { navController.navigate(AppRoutes.VIEW_SURVEYS) },
                    { navController.navigate(AppRoutes.VIEW_OWN_CANDIDACY) },
                    {},
                    emptyList(),
                    {},
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
                    authRoutine.userChangePassword(oldPassword, newPassword, confirmPassword) { success, error ->
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
            authRoutine.userLogout { success, error ->
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
                    allPositions = PositionRepository.getAllPositions()
                }
            }
            ViewSurveysScreen(
                allSurveys,
                allPositions,
                { name, positionName, openStatus ->
                    coroutineScope.launch {
                        SurveyRepository.addSurvey(name, positionName, openStatus)
                        allSurveys = SurveyRepository.getSurveyDisplayItems()
                    }
                },
                { surveyId, name, positionName, openStatus ->
                    coroutineScope.launch {
                        SurveyRepository.updateSurvey(surveyId, name, positionName, openStatus)
                        allSurveys = SurveyRepository.getSurveyDisplayItems()
                    }
                },
                { navController.popBackStack() }
            )
        }
    }
}
