package com.example.basicchat.models.repository

import android.util.Log
import com.example.basicchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersRef = firestore.collection("users")

    // ---------------- Listen for users except current ----------------
    fun listenForUsers(onUsersChanged: (List<User>) -> Unit) {
        usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserRepository", "Firestore error", error)
                return@addSnapshotListener
            }

            val currentUid = auth.currentUser?.uid
            Log.d("UserRepository", "Current UID: $currentUid")
            Log.d("UserRepository", "Snapshot count: ${snapshot?.documents?.size}")

            val users = snapshot?.documents
                ?.mapNotNull { it.toObject(User::class.java) }
                ?.filter { it.uid != currentUid } // Exclude self
                ?: emptyList()

            Log.d("UserRepository", "Users list after filtering: $users")
            onUsersChanged(users)
        }
    }

    // ---------------- Save current user if new ----------------
    suspend fun saveUserIfNew(profileImageUrl: String? = null) {
        val currentUser = auth.currentUser ?: return
        val docRef = usersRef.document(currentUser.uid)
        val doc = docRef.get().await()

        if (!doc.exists()) {
            val user = User(
                uid = currentUser.uid,
                name = currentUser.displayName
                    ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                email = currentUser.email ?: "",
                profileImageUrl = profileImageUrl // ✅ Save initial profile image if available
            )
            docRef.set(user).await()
            Log.d("UserRepository", "User saved: ${user.uid}")
        } else {
            Log.d("UserRepository", "User already exists: ${currentUser.uid}")
        }
    }

    // ---------------- Update profile image ----------------
    suspend fun updateProfileImage(imageUrl: String) {
        val currentUid = auth.currentUser?.uid ?: return
        try {
            usersRef.document(currentUid)
                .update("profileImageUrl", imageUrl)
                .await()
            Log.d("UserRepository", "Profile image updated: $imageUrl")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile image", e)
        }
    }

    // ---------------- Sign out ----------------
    fun signOut() {
        auth.signOut()
    }
}
