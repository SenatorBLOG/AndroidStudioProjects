package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.api.GoogleLoginRequest
import com.breatheonline.breathe.data.models.AuthResponse
import com.breatheonline.breathe.data.models.LoginRequest
import com.breatheonline.breathe.data.models.RegisterRequest
import com.breatheonline.breathe.utils.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) {

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = apiService.login(LoginRequest(email.trim(), password))
        if (response.isSuccessful) {
            val body = requireNotNull(response.body()) { "Empty response body" }
            tokenManager.saveToken(body.token)
            body
        } else {
            error(httpError(response.code(), response.errorBody()?.string()))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> = runCatching {
        val response = apiService.register(RegisterRequest(name.trim(), email.trim(), password))
        if (response.isSuccessful) {
            val body = requireNotNull(response.body()) { "Empty response body" }
            tokenManager.saveToken(body.token)
            body
        } else {
            error(httpError(response.code(), response.errorBody()?.string()))
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = runCatching {
        // Теперь передаем idToken в поле accessToken, как того требует бэкенд
        val response = apiService.loginWithGoogle(GoogleLoginRequest(accessToken = idToken))
        if (response.isSuccessful) {
            val body = requireNotNull(response.body()) { "Empty response body" }
            tokenManager.saveToken(body.token)
            body
        } else {
            error(httpError(response.code(), response.errorBody()?.string()))
        }
    }

    fun logout() = tokenManager.clearToken()

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun httpError(code: Int, rawBody: String?): String = when (code) {
        400  -> "Invalid request. Check your inputs."
        401  -> "Email or password is incorrect."
        409  -> "An account with this email already exists."
        422  -> "Please check your inputs and try again."
        429  -> "Too many attempts. Please wait a moment."
        in 500..599 -> "Server error ($code). Please try again later."
        else -> rawBody?.take(120) ?: "Something went wrong ($code)."
    }
}
