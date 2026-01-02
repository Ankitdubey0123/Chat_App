package com.example.basicchat.models.repository

import com.example.basicchat.models.User



import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersRef = firestore.collection("users")

    val currentUser get() = auth.currentUser

    // ---------------- EMAIL SIGN-UP ----------------
    suspend fun signUpWithEmail(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID is null"))

            saveUser(uid, username.ifBlank { email.substringBefore("@") }, email)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignUp failed", e)
            Result.failure(e)
        }
    }

    // ---------------- EMAIL LOGIN ----------------
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User null"))

            saveUser(
                user.uid,
                user.displayName ?: email.substringBefore("@"),
                user.email ?: email
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignIn failed", e)
            Result.failure(e)
        }
    }

    // ---------------- GOOGLE SIGN-IN ----------------
    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User null"))

            saveUser(
                user.uid,
                user.displayName ?: user.email?.substringBefore("@") ?: "User",
                user.email ?: ""
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google SignIn failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // ---------------- SAVE USER ----------------
    private suspend fun saveUser(uid: String, name: String, email: String) {
        val user = User(uid = uid, name = name, email = email)
        usersRef.document(uid).set(user).await()
    }
}
