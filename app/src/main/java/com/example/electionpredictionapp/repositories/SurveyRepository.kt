package com.example.electionpredictionapp.repositories

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.CandidacyDisplayItem
import com.example.electionpredictionapp.dataclasses.SurveyDisplayItem
import com.example.electionpredictionapp.dataclasses.SurveyResponse
import com.example.electionpredictionapp.dataclasses.User
import com.google.firebase.firestore.AggregateSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.orEmpty

object SurveyRepository {
    suspend fun getSurveyDisplayItems(): List<SurveyDisplayItem> {
        val surveySnapshots = FirebaseInstances.firestoreInst.collection("surveys")
            .get().await()
        return surveySnapshots.documents.map { doc ->
            val surveyId = doc.id
            val surveyName = doc.getString("name") ?: ""
            val openStatus = doc.getString("openStatus") ?: ""
            val responseCountQuery = FirebaseInstances.firestoreInst
                .collection("surveys").document(surveyId)
                .collection("responses").count()
                .get(AggregateSource.SERVER).await()
            val responseCount = responseCountQuery.count.toInt()
            SurveyDisplayItem(surveyId, surveyName, openStatus, responseCount)
        }
    }

    suspend fun getUnansweredOpenSurveys(uid: String): List<SurveyDisplayItem> = coroutineScope {
        val openSnapshots = FirebaseInstances.firestoreInst
            .collection("surveys").whereEqualTo("openStatus", "Open")
            .get().await()
        openSnapshots.documents.map { doc ->
            async {
                val surveyId = doc.id
                val surveyRef = FirebaseInstances.firestoreInst.collection("surveys")
                    .document(surveyId)
                val responseDoc = surveyRef
                    .collection("responses").document(uid)
                    .get().await()
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
        surveyId: String, user: User, response: SurveyResponse,
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
            .collection("surveys").document(surveyId)
            .collection("responses").document(user.id)
            .set(responseData).await()
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