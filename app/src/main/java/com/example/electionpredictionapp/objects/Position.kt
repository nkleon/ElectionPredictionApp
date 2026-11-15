package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

class Position(
    @DocumentId val id: String = "",
    val name: String = ""
)