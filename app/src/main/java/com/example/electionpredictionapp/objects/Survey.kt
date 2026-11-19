package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class Survey(
    @DocumentId val id: String = "",
    val name: String = "",
    val openStatus: String = "Closed",
    val positionId: String = "",
)