package com.example.basicchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.User
import com.example.basicchat.models.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsersViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        repository.saveUserIfNew()  // ✅ Ensure current user is written to DB
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            repository.listenForUsers { list ->
                _users.value = list
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _users.value = emptyList()
    }
}
