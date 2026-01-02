package com.example.basicchat.models.repository

import com.example.basicchat.models.ChatRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRequestRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val requestsRef = firestore.collection("chat_requests")
    private val chatsRef = firestore.collection("chats")

    // Send request
    suspend fun sendRequest(fromId: String, toId: String) {
        val doc = requestsRef.document()
        val data = mapOf(
            "id" to doc.id,
            "fromId" to fromId,
            "toId" to toId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        doc.set(data).await()
    }

    // Listen incoming requests
    fun listenIncomingRequests(userId: String, onResult: (List<ChatRequest>) -> Unit) {
        requestsRef
            .whereEqualTo("toId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ChatRequest::class.java) ?: emptyList()
                onResult(list)
            }
    }

    // Listen outgoing requests
    fun listenOutgoingRequests(userId: String, onResult: (List<ChatRequest>) -> Unit) {
        requestsRef
            .whereEqualTo("fromId", userId)
            .whereIn("status", listOf("pending", "accepted"))
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ChatRequest::class.java) ?: emptyList()
                onResult(list)
            }
    }

    // Accept request
    suspend fun acceptRequest(request: ChatRequest) {
        val chatId = getChatId(request.fromId, request.toId)
        chatsRef.document(chatId).set(
            mapOf(
                "chatId" to chatId,
                "participants" to listOf(request.fromId, request.toId),
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        requestsRef.document(request.id).update("status", "accepted").await()
    }

    // Reject request
    suspend fun rejectRequest(requestId: String) {
        requestsRef.document(requestId).update("status", "rejected").await()
    }

    private fun getChatId(u1: String, u2: String): String {
        return if (u1 < u2) "${u1}_$u2" else "${u2}_$u1"
    }
}
