package com.example.basicchat.models




import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

import com.google.firebase.database.*





import com.google.firebase.database.FirebaseDatabase

// ---------------- AUTH REPOSITORY ----------------
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
) {

    val currentUser get() = auth.currentUser

    // ---------------- EMAIL SIGN-UP ----------------
    suspend fun signUpWithEmail(username: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID is null"))

            // Save user profile to RTDB
            saveUserToDatabase(uid, username.ifBlank { email.substringBefore("@") }, email)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignUp failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ---------------- EMAIL LOGIN ----------------
    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID is null"))

            // Always make sure user exists in DB
            val username = result.user?.displayName ?: email.substringBefore("@")
            val userEmail = result.user?.email ?: email
            saveUserToDatabase(uid, username, userEmail)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignIn failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ---------------- GOOGLE SIGN-IN ----------------
    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))

            saveUserToDatabase(
                user.uid,
                user.displayName ?: user.email?.substringBefore("@") ?: "User",
                user.email ?: ""
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google SignIn failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ---------------- SIGN OUT ----------------
    fun signOut() {
        auth.signOut()
    }

    // ---------------- SAVE USER TO DATABASE ----------------
    private fun saveUserToDatabase(uid: String, username: String, email: String) {
        val user = User(uid = uid, name = username, email = email)
        db.child(uid).setValue(user)
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "Failed to save user: ${e.message}", e)
            }
    }
}



class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
) {

    fun listenForUsers(onUsersChanged: (List<User>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null && user.uid != auth.currentUser?.uid) {
                        list.add(user)
                    }
                }
                Log.d("UserRepository", "Fetched ${list.size} users")
                onUsersChanged(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserRepository", "Database error: ${error.message}", error.toException())
            }
        })
    }

    fun saveUserIfNew() {
        val currentUser = auth.currentUser ?: return
        val userRef = database.child(currentUser.uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val newUser = User(
                    uid = currentUser.uid,
                    name = currentUser.displayName ?: currentUser.email ?: "Unknown",
                    email = currentUser.email ?: ""
                )
                userRef.setValue(newUser).addOnSuccessListener {
                    Log.d("UserRepository", "User saved: ${newUser.uid}")
                }.addOnFailureListener { e ->
                    Log.e("UserRepository", "Failed to save user: ${e.message}")
                }
            } else {
                Log.d("UserRepository", "User already exists: ${currentUser.uid}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

class ChatRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun loadMessages(currentUserId: String, otherUserId: String, onResult: (List<Message>) -> Unit) {
        val chatId = getChatId(currentUserId, otherUserId)
        val chatRef = db.child("chats").child(chatId)

        chatRef.get().addOnSuccessListener { snapshot ->
            val msgs = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
            onResult(msgs.sortedByDescending { it.timestamp })
        }

        chatRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val msgs = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onResult(msgs.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun sendMessage(fromId: String, toId: String, text: String) {
        val chatId = getChatId(fromId, toId)
        val messageId = db.child("chats").child(chatId).push().key ?: return

        val message = Message(
            id = messageId,
            fromId = fromId,
            toId = toId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        db.child("chats").child(chatId).child(messageId).setValue(message)
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }
}
