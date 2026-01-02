package com.example.basicchat.models





data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null  // ✅ Add this for profile pictures
)




data class Message(
    val id: String = "",        // message unique id
    val fromId: String = "",    // sender id
    val toId: String = "",      // receiver id
    val text: String = "",
    val fileUrl: String? = null, // null if it's text only
    val timestamp: Long = System.currentTimeMillis(),
    val fileName : String = "",
    val type : String = ""

)



data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)


data class ChatRequest(
    val id: String = "",
    val fromId: String = "",
    val toId: String = "",
    val status: String = "",
    val timestamp: Long = 0L
)


enum class ConnectionStatus {
    NONE,       // No request between users
    SENT,       // Current user sent request
    RECEIVED,   // Current user received request
    ACCEPTED,    // Both users can chat
    REJECTED
}