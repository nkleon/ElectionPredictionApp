package com.example.electionpredictionapp.repositories

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.User
import kotlinx.coroutines.tasks.await

object UserRepository {
    suspend fun getUser(uid: String): User? {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("users").document(uid)
            .get().await()
        return if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }
    suspend fun getAllUsers(): List<User> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("users")
            .get().await()
        return snapshot.toObjects(User::class.java)
    }
}