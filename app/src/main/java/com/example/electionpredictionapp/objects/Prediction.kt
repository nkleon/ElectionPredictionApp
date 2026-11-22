package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class Prediction(
    @DocumentId val id: String = "",
    val shares: Map<Int, Double> = emptyMap()
)