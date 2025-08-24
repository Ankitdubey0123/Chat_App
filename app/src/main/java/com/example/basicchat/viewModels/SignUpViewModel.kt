package com.example.basicchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.AuthRepository
import com.example.basicchat.models.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signUp(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.signUpWithEmail(username, email, password)
            _uiState.value = if (result.isSuccess) {
                onSuccess()
                AuthUiState()
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.signInWithEmail(email, password)
            _uiState.value = if (result.isSuccess) {
                onSuccess()
                AuthUiState()
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.signInWithGoogle(idToken)
            _uiState.value = if (result.isSuccess) {
                onSuccess()
                AuthUiState()
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    val currentUser get() = repository.currentUser
}