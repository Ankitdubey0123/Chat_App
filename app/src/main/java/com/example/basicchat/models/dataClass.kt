package com.example.basicchat.models





data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)




data class Message(
    val id: String = "",        // message unique id
    val fromId: String = "",    // sender id
    val toId: String = "",      // receiver id
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)



data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
