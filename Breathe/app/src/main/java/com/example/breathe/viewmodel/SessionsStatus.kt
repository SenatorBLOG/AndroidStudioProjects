package com.example.breathe.viewmodel

sealed interface SessionsStatus {
    data object Loading : SessionsStatus
    data object Success : SessionsStatus
    data object Empty   : SessionsStatus
    data class  Error(val message: String) : SessionsStatus
}
