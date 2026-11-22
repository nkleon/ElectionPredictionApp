package com.example.electionpredictionapp.repositories

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.Prediction
import kotlinx.coroutines.tasks.await

object PredictionRepository {
    suspend fun getAllPredictions(): List<Prediction> {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("prediction")
            .get().await()

        return snapshot.documents.map { doc ->
            val rawShares = doc.get("shares") as? Map<*, *>
            val parsedShares: Map<Int, Double> = rawShares?.mapNotNull { (k, v) ->
                val keyInt = when (k) {
                    is Int -> k
                    is Long -> k.toInt()
                    is String -> k.toIntOrNull()
                    else -> null
                }
                val valDouble = when (v) {
                    is Double -> v
                    is Float -> v.toDouble()
                    is Long -> v.toDouble()
                    is Int -> v.toDouble()
                    is Number -> v.toDouble()
                    else -> null
                }
                if (keyInt != null && valDouble != null) keyInt to valDouble else null
            }?.toMap() ?: emptyMap()

            Prediction(
                id = doc.id,
                shares = parsedShares
            )
        }
    }
}