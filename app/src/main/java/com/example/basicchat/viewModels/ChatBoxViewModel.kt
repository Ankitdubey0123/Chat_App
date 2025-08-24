package com.example.basicchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.ChatRepository
import com.example.basicchat.models.Message

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // ✅ Load messages from repository
    fun loadMessages(currentUserId: String, otherUserId: String) {
        viewModelScope.launch {
            repository.loadMessages(currentUserId, otherUserId) { msgs ->
                _messages.value = msgs
            }
        }
    }

    // ✅ Send message through repository
    fun sendMessage(fromId: String, toId: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(fromId, toId, text)
        }
    }
}
