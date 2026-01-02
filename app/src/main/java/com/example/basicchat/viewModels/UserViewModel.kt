package com.example.basicchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.ChatRequest
import com.example.basicchat.models.ConnectionStatus
import com.example.basicchat.models.User
import com.example.basicchat.models.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------------- UI MODEL ----------------
data class UserUiModel(
    val user: User,
    val status: ConnectionStatus = ConnectionStatus.NONE
)

// ---------------- VIEWMODEL ----------------
class UsersViewModel(
    private val repository: UserRepository = UserRepository(),
    private val requestViewModel: ChatRequestViewModel = ChatRequestViewModel()
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserUiModel>>(emptyList())
    val users: StateFlow<List<UserUiModel>> = _users

    private val currentUserId = repository.auth.currentUser?.uid ?: ""

    init {
        viewModelScope.launch { repository.saveUserIfNew() }
        observeUsers()
        observeRequests()
    }

    // ---------------- LISTEN FOR USERS ----------------
    private fun observeUsers() {
        repository.listenForUsers { userList ->
            val userUiList = userList.map { user ->
                UserUiModel(user = user, status = ConnectionStatus.NONE)
            }
            _users.value = userUiList
        }
    }

    // ---------------- OBSERVE REQUESTS ----------------
    private fun observeRequests() {
        if (currentUserId.isEmpty()) return

        // Listen incoming & outgoing requests
        requestViewModel.listenIncomingRequests(currentUserId)
        requestViewModel.listenOutgoingRequests(currentUserId)

        // Update user status whenever requests change
        viewModelScope.launch {
            requestViewModel.incomingRequests.collect { incoming ->
                val updated = _users.value.map { userUi ->
                    val status = when {
                        incoming.any { it.fromId == userUi.user.uid && it.status == "pending" } -> ConnectionStatus.RECEIVED
                        incoming.any { it.fromId == userUi.user.uid && it.status == "accepted" } -> ConnectionStatus.ACCEPTED
                        incoming.any { it.fromId == userUi.user.uid && it.status == "rejected" } -> ConnectionStatus.REJECTED
                        else -> userUi.status
                    }
                    userUi.copy(status = status)
                }
                _users.value = updated
            }
        }

        viewModelScope.launch {
            requestViewModel.outgoingRequests.collect { outgoing ->
                val updated = _users.value.map { userUi ->
                    val status = when {
                        outgoing.any { it.toId == userUi.user.uid && it.status == "pending" } -> ConnectionStatus.SENT
                        outgoing.any { it.toId == userUi.user.uid && it.status == "accepted" } -> ConnectionStatus.ACCEPTED
                        outgoing.any { it.toId == userUi.user.uid && it.status == "rejected" } -> ConnectionStatus.REJECTED
                        else -> userUi.status
                    }
                    userUi.copy(status = status)
                }
                _users.value = updated
            }
        }
    }

    // ---------------- SEND / ACCEPT / REJECT REQUEST ----------------
    fun sendRequest(toUserId: String) {
        if (currentUserId.isNotEmpty()) {
            requestViewModel.sendRequest(currentUserId, toUserId)
        }
    }

    fun acceptRequest(userId: String) {
        val request = requestViewModel.incomingRequests.value.find { it.fromId == userId }
        request?.let { requestViewModel.acceptRequest(it) }
    }
    fun rejectRequest(requestId: String) {
        requestViewModel.rejectRequest(requestId)
    }

    // ---------------- SIGN OUT ----------------
    fun signOut() {
        repository.signOut()
        _users.value = emptyList()
    }

    // ---------------- UPDATE PROFILE IMAGE ----------------
    fun updateProfileImage(url: String) {
        viewModelScope.launch {
            repository.updateProfileImage(url)
            // Update local _users list for current user
            _users.value = _users.value.map {
                if (it.user.uid == currentUserId) it.copy(user = it.user.copy(profileImageUrl = url))
                else it
            }
        }
    }
}
