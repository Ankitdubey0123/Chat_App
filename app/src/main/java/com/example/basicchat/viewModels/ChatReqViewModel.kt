package com.example.basicchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.ChatRequest
import com.example.basicchat.models.repository.ChatRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatRequestViewModel(
    private val repository: ChatRequestRepository = ChatRequestRepository()
) : ViewModel() {

    private val _incomingRequests = MutableStateFlow<List<ChatRequest>>(emptyList())
    val incomingRequests: StateFlow<List<ChatRequest>> = _incomingRequests

    private val _outgoingRequests = MutableStateFlow<List<ChatRequest>>(emptyList())
    val outgoingRequests: StateFlow<List<ChatRequest>> = _outgoingRequests

    fun listenIncomingRequests(userId: String) {
        repository.listenIncomingRequests(userId) { requests ->
            _incomingRequests.value = requests
        }
    }

    fun listenOutgoingRequests(userId: String) {
        repository.listenOutgoingRequests(userId) { requests ->
            _outgoingRequests.value = requests
        }
    }

    fun sendRequest(fromId: String, toId: String) {
        viewModelScope.launch { repository.sendRequest(fromId, toId) }
    }

    fun acceptRequest(request: ChatRequest) {
        viewModelScope.launch { repository.acceptRequest(request) }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch { repository.rejectRequest(requestId) }
    }
}
