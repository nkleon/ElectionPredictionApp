package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class SurveyResponse(
    @DocumentId val userId: String = "",
    val surveyId: String = "",
    val q1Ans: String = "",
    val q2Ans: String = "",
    val q3Ans: String = ""
)