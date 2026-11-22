package com.example.electionpredictionapp.repositories

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.example.electionpredictionapp.dataclasses.Candidacy
import com.example.electionpredictionapp.dataclasses.CandidacyDisplayItem
import com.example.electionpredictionapp.dataclasses.User
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant

object CandidacyRepository {
    suspend fun getOwnCandidacy(uid: String): Candidacy {
        val snapshot = FirebaseInstances.firestoreInst
            .collection("candidacies").document(uid).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(Candidacy::class.java) ?: Candidacy(uid)
        } else {
            Candidacy(id = uid)
        }
    }

    suspend fun saveOwnCandidacy(uid: String, positionName: String, manifesto: String) {
        val docRef = FirebaseInstances.firestoreInst
            .collection("candidacies")
            .document(uid)
        val exists = docRef.get().await().exists()
        val now = Instant.now().toString()

        val data = buildMap<String, Any> {
            put("positionName", positionName)
            put("manifesto", manifesto)
            // put("updatedTimestamp", now)
            if (!exists) {
                put("createdTimestamp", now)
            }
        }
        docRef.set(data, SetOptions.merge()).await()
    }

    suspend fun getAllCandidacies(): List<CandidacyDisplayItem> {
        val candidacySnapshots = FirebaseInstances.firestoreInst
            .collection("candidacies").get().await()
        val userSnapshots = FirebaseInstances.firestoreInst
            .collection("users").get().await()

        val usersById = userSnapshots.documents.associate { doc ->
            doc.id to doc.toObject(User::class.java)
        }

        return candidacySnapshots.documents.mapNotNull { doc ->
            val candidacy = doc.toObject(Candidacy::class.java) ?: return@mapNotNull null
            val candidacyId = candidacy.id.ifBlank { doc.id }
            val user = usersById[candidacyId]

            CandidacyDisplayItem(
                id = candidacyId,
                firstName = user?.firstName ?: "",
                lastName = user?.lastName ?: "",
                positionName = candidacy.positionName,
                manifesto = candidacy.manifesto
            )
        }
    }
}