package com.example.electionpredictionapp.objects

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val accountRole: String = "",
    val email: String = "",
)