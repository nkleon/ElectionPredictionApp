package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class Position(
    @DocumentId val id: String = "",
    val name: String = ""
)