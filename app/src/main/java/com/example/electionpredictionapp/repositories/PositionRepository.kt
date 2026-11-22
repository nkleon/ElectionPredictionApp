package com.example.electionpredictionapp.repositories

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.Position
import kotlinx.coroutines.tasks.await

object PositionRepository {
    suspend fun getAllPositions(): List<Position> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("positions")
            .get().await()
        return snapshot.toObjects(Position::class.java)
    }
    suspend fun addPosition(name: String) {
        FirebaseInstances.firestoreInst
            .collection("positions")
            .add(mapOf("name" to name))
            .await()
    }
    suspend fun updatePosition(positionId: String, newName: String) {
        FirebaseInstances.firestoreInst
            .collection("positions")
            .document(positionId)
            .update("name", newName)
            .await()
    }
}