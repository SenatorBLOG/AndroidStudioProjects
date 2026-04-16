package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.models.AuthResponse
import com.breatheonline.breathe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

sealed interface AuthUiState {
    data object Idle    : AuthUiState
    data object Loading : AuthUiState
    data class  Success(val response: AuthResponse) : AuthUiState
    data class  Error(val message: String)          : AuthUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _loginState    = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    // ── Actions ───────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (_loginState.value is AuthUiState.Loading) return
        _loginState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.login(email, password)
                .onSuccess { _loginState.value = AuthUiState.Success(it) }
                .onFailure { _loginState.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_registerState.value is AuthUiState.Loading) return
        _registerState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.register(name, email, password)
                .onSuccess { _registerState.value = AuthUiState.Success(it) }
                .onFailure { _registerState.value = AuthUiState.Error(it.message ?: "Registration failed") }
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (_loginState.value is AuthUiState.Loading) return
        _loginState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.loginWithGoogle(idToken)
                .onSuccess { _loginState.value = AuthUiState.Success(it) }
                .onFailure { _loginState.value = AuthUiState.Error(it.message ?: "Google Sign-In failed") }
        }
    }

    fun resetLoginState()    { _loginState.value    = AuthUiState.Idle }
    fun resetRegisterState() { _registerState.value = AuthUiState.Idle }
}
