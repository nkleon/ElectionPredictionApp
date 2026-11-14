package com.example.electionpredictionapp.auth

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances

class AuthRoutine {

    fun userLogin(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        FirebaseInstances.authInst.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    fun userRegister(
        firstName: String, lastName: String, dateOfBirth: String, gender: String,
        accountRole: String, email: String, password: String, onResult: (Boolean, String?) -> Unit
    ) {
        FirebaseInstances.authInst.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseInstances.authInst.currentUser!!
                    val userData = hashMapOf(
                        "accountRole" to accountRole,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "gender" to gender,
                        "dateOfBirth" to dateOfBirth,
                        "email" to email,
                    )
                    FirebaseInstances.firestoreInst.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            onResult(true, null)
                        }
                        .addOnFailureListener { exception ->
                            onResult(false, exception.localizedMessage)
                        }
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }
}