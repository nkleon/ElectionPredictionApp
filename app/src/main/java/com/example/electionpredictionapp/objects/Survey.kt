package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class Survey(
    @DocumentId val id: String = "",
    val name: String = "",
    val status: Boolean = false,
    val positionId: String = "",
)