package com.example.electionpredictionapp.dataclasses

import com.google.firebase.firestore.DocumentId

data class Candidacy(
    @DocumentId val id: String = "",
    val positionName: String = "",
    val manifesto: String = "",
    val createdTimestamp: String = ""
)
