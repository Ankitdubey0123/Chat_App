package com.example.basicchat.models.repository

import com.example.basicchat.models.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun messagesRef(chatId: String) =
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")

    fun loadMessages(
        currentUserId: String,
        otherUserId: String,
        onResult: (List<Message>) -> Unit
    ) {
        val chatId = getChatId(currentUserId, otherUserId)

        messagesRef(chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val messages =
                    snapshot?.toObjects(Message::class.java) ?: emptyList()

                onResult(messages)
            }
    }

    // ✅ ONE generic function for ALL message types
    suspend fun sendMessage(message: Message) {
        val chatId = getChatId(message.fromId, message.toId)
        messagesRef(chatId).add(message).await()
    }

    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2"
        else "${user2}_$user1"
    }
}
