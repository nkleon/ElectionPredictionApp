package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class Candidacy(
    @DocumentId val id: String = "",
    val positionName: String = "",
    val manifesto: String = ""
)
