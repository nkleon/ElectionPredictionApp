package com.example.electionpredictionapp.auth

import com.example.electionpredictionapp.dependencyinjection.FirebaseInstances
import com.google.firebase.auth.EmailAuthProvider

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

    fun userLogout(onResult: (Boolean, String?) -> Unit) {
        try {
            FirebaseInstances.authInst.signOut()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage)
        }
    }

    fun userChangePassword(
        oldPass: String, newPass: String, confirmPass: String, onResult: (Boolean, String?) -> Unit
    ) {
        if (newPass != confirmPass) {
            onResult(false, "New passwords do not match")
            return
        }

        val user = FirebaseInstances.authInst.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(
                                false,
                                task.exception?.localizedMessage ?: "Failed to update password"
                            )
                        }
                    }
                } else {
                    onResult(false, "Current password invalid")
                }
            }
        } else {
            onResult(false, "No authenticated user found")
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