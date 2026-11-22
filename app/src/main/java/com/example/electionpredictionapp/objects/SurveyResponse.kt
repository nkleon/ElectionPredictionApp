package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class SurveyResponse(
    @DocumentId val id: String = "",
    val userId: String = "",
    val interestLevel: String = "",
    val pastParticipation: String = "",
    val willParticipate: String = "",
    val leadershipApproval: String = "",
    val preferredCandidates: Map<String, String>? = null
)