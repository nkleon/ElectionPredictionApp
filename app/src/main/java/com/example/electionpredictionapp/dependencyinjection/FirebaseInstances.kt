package com.example.electionpredictionapp.dependencyinjection

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseInstances {
    val authInst: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestoreInst: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}