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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate

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
    const val VIEW_PREDICTIONS = "viewPredictions"
    const val VIEW_USERS = "viewUsers"
    const val VIEW_POSITIONS = "viewPositions"
    const val VIEW_SURVEYS = "viewSurveys"
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
                openStatus = openStatus,
                responseCount = responseCount
            )
        }
    }

    suspend fun getUnansweredOpenSurveys(uid: String): List<SurveyDisplayItem> = coroutineScope {
        val openSnapshots = FirebaseInstances.firestoreInst
            .collection("surveys")
            .whereEqualTo("openStatus", "Open")
            .get()
            .await()

        openSnapshots.documents.map { doc ->
            async {
                val surveyId = doc.id
                val surveyRef = FirebaseInstances.firestoreInst
                    .collection("surveys")
                    .document(surveyId)

                val responseDoc = surveyRef
                    .collection("responses")
                    .document(uid)
                    .get()
                    .await()

                if (responseDoc.exists()) {
                    null
                } else {
                    SurveyDisplayItem(
                        id = surveyId,
                        name = doc.getString("name") ?: "",
                        openStatus = doc.getString("openStatus") ?: "Open",
                        responseCount = 0 // Not needed on the Supporter home screen
                    )
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun addSurvey(name: String, openStatus: String) {
        val surveyData = mapOf(
            "name" to name,
            "openStatus" to openStatus
        )
        FirebaseInstances.firestoreInst.collection("surveys")
            .add(surveyData).await()
    }

    suspend fun updateSurvey(
        surveyId: String, name: String, openStatus: String
    ) {
        val updates = mapOf(
            "name" to name,
            "openStatus" to openStatus
        )
        FirebaseInstances.firestoreInst.collection("surveys")
            .document(surveyId).update(updates)
            .await()
    }

    suspend fun submitSurveyResponse(
        surveyId: String,
        user: User,
        response: SurveyResponse,
        candidacies: List<CandidacyDisplayItem>
    ) {
        val userBirthyr = LocalDate.parse(user.dateOfBirth).year
        val candidatesByPosition = candidacies.groupBy { it.positionName }
            .mapValues { (_, list) ->
                list.sortedBy { it.createdTimestamp } // Ascending order
            }

        val mappedCandidates = mutableMapOf<String, Int>()
        for ((positionName, selectedCandidateName) in response.preferredCandidates!!) {
            val positionCandidates = candidatesByPosition[positionName].orEmpty()

            val fieldKey = mapPositionToFieldKey(positionName)
            mappedCandidates[fieldKey] = mapSelectedCandidateCode(
                selectedCandidateName = selectedCandidateName,
                positionCandidates = positionCandidates
            )
        }

        val responseData = buildMap<String, Any> {
            put("birthyr", userBirthyr)
            put("gender4", mapGender(user.gender))
            put("newsint", mapInterestLevel(response.interestLevel))
            put("presvote20post", mapPastParticipation(response.pastParticipation))
            put("CC22_363", mapWillParticipate(response.willParticipate))
            put("CC22_320d", mapLeadershipApproval(response.leadershipApproval))
            put("CC22_320g", mapLeadershipApproval(response.leadershipApproval))
            putAll(mappedCandidates)
        }
        FirebaseInstances.firestoreInst
            .collection("surveys")
            .document(surveyId)
            .collection("responses")
            .document(user.id)
            .set(responseData)
            .await()
    }

    private fun mapGender(value: String): Int = when (value.trim().lowercase()) {
        "male" -> 1
        "female" -> 2
        else -> value.toIntOrNull() ?: 0
    }

    private fun mapInterestLevel(value: String): Int = when (value.trim().lowercase()) {
        "most of the time" -> 1
        "some of the time" -> 2
        "only now and then" -> 3
        "hardly at all" -> 4
        "don't know" -> 7
        else -> value.toIntOrNull() ?: 0
    }

    private fun mapPastParticipation(value: String): Int = when (value.trim().lowercase()) {
        "yes" -> 5
        "no" -> 6
        else -> value.toIntOrNull() ?: 0
    }

    private fun mapWillParticipate(value: String): Int = when (value.trim().lowercase()) {
        "yes, definitely" -> 1
        "probably" -> 2
        "no" -> 5
        "undecided" -> 6
        else -> value.toIntOrNull() ?: 0
    }

    private fun mapLeadershipApproval(value: String): Int = when (value.trim().lowercase()) {
        "strongly approve" -> 1
        "somewhat approve" -> 2
        "somewhat disapprove" -> 3
        "strongly disapprove" -> 4
        "not sure" -> 5
        else -> value.toIntOrNull() ?: 0
    }

    private fun mapSelectedCandidateCode(
        selectedCandidateName: String,
        positionCandidates: List<CandidacyDisplayItem>
    ): Int {
        val normalized = selectedCandidateName.trim()
        val index = positionCandidates.indexOfFirst { candidacy ->
            val fullName = "${candidacy.firstName} ${candidacy.lastName}".trim()
            fullName.equals(normalized, ignoreCase = true)
        }
        if (index != -1) return index + 1

        return when (normalized.trim().lowercase()) {
            "other" -> 7
            "i'm not sure" -> 8
            "no one" -> 9
            else -> 0
        }
    }

    private fun mapPositionToFieldKey(positionName: String): String =
        when (positionName.trim().lowercase()) {
            "gov", "governor", "state governor" -> "CC22_366"
            "sen", "senator", "state senator" -> "CC22_365"
            // Fallback: lowercase camelCase or snake_case if an unmapped position is added
            else -> positionName.trim().lowercase().replace(Regex("\\s+"), "_")
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
        val docRef = FirebaseInstances.firestoreInst
            .collection("candidacies")
            .document(uid)
        val exists = docRef.get().await().exists()
        val now = Instant.now().toString()

        val data = buildMap<String, Any> {
            put("positionName", positionName)
            put("manifesto", manifesto)
            // put("updatedTimestamp", now)
            if (!exists) {
                put("createdTimestamp", now)
            }
        }
        docRef.set(data, SetOptions.merge()).await()
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

object PredictionRepository {
    suspend fun getAllPredictions(): List<Prediction> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("prediction")
            .get().await()

        return snapshot.documents.map { doc ->
            val rawShares = doc.get("shares") as? Map<*, *>
            val parsedShares: Map<Int, Double> = rawShares?.mapNotNull { (k, v) ->
                val keyInt = when (k) {
                    is Int -> k
                    is Long -> k.toInt()
                    is String -> k.toIntOrNull()
                    else -> null
                }
                val valDouble = when (v) {
                    is Double -> v
                    is Float -> v.toDouble()
                    is Long -> v.toDouble()
                    is Int -> v.toDouble()
                    is Number -> v.toDouble()
                    else -> null
                }
                if (keyInt != null && valDouble != null) keyInt to valDouble else null
            }?.toMap() ?: emptyMap()

            Prediction(
                id = doc.id,
                shares = parsedShares
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
