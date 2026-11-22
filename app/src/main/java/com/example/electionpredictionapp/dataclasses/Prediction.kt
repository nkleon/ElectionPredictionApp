package com.example.electionpredictionapp.dataclasses

import com.google.firebase.firestore.DocumentId

data class Prediction(
    @DocumentId val id: String = "",
    val shares: Map<Int, Double> = emptyMap()
)