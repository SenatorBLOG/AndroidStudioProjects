package com.example.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.CoachHistoryItem
import com.example.breathe.data.models.CoachMessageRequest
import com.example.breathe.data.models.CoachTechnique
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Chat message ──────────────────────────────────────────────────────────────

enum class ChatRole { USER, COACH }

data class ChatMessage(
    val id:        String,
    val role:      ChatRole,
    val text:      String,
    val technique: CoachTechnique? = null,
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class AiCoachUiState(
    val messages:        List<ChatMessage> = emptyList(),
    val isLoading:       Boolean           = false,
    val limitReached:    Boolean           = false,
    val hoursUntilReset: Int               = 0,
    val messagesLeft:    Int?              = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

private const val WELCOME_ID   = "welcome"
private const val WELCOME_TEXT =
    "Hi! I'm your AI breathing coach. Tell me how you're feeling — stressed, anxious, tired — and I'll recommend the perfect technique for you. 🌿"

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(AiCoachUiState())
    val state: StateFlow<AiCoachUiState> = _state.asStateFlow()

    init { addCoachMessage(WELCOME_ID, WELCOME_TEXT) }

    // ── Public API ────────────────────────────────────────────────────────────

    fun send(text: String) {
        if (text.isBlank() || _state.value.isLoading) return

        // Snapshot history BEFORE adding the new user message
        val history = _state.value.messages
            .filterNot { it.id == WELCOME_ID }
            .map { CoachHistoryItem(if (it.role == ChatRole.COACH) "model" else "user", it.text) }

        _state.update { s ->
            s.copy(
                messages = s.messages + ChatMessage(
                    id   = System.currentTimeMillis().toString(),
                    role = ChatRole.USER,
                    text = text.trim(),
                ),
                isLoading    = true,
                limitReached = false,
            )
        }

        viewModelScope.launch {
            runCatching {
                apiService.sendCoachMessage(CoachMessageRequest(text.trim(), history))
            }
            .onSuccess { resp ->
                when {
                    resp.isSuccessful -> {
                        val body = resp.body()!!
                        _state.update { s ->
                            s.copy(
                                messages = s.messages + ChatMessage(
                                    id        = "${System.currentTimeMillis()}_c",
                                    role      = ChatRole.COACH,
                                    text      = body.reply,
                                    technique = body.technique,
                                ),
                                isLoading    = false,
                                messagesLeft = body.messagesLeft,
                            )
                        }
                    }
                    resp.code() == 429 -> {
                        val hours = parseHours(resp.errorBody()?.string())
                        _state.update { it.copy(isLoading = false, limitReached = true, hoursUntilReset = hours) }
                    }
                    else -> appendError("Server error ${resp.code()}. Please try again.")
                }
            }
            .onFailure { appendError("Network error. Check your connection and try again.") }
        }
    }

    fun reset() {
        _state.value = AiCoachUiState()
        addCoachMessage(WELCOME_ID, WELCOME_TEXT)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun addCoachMessage(id: String, text: String, technique: CoachTechnique? = null) {
        _state.update { s ->
            s.copy(messages = s.messages + ChatMessage(id, ChatRole.COACH, text, technique))
        }
    }

    private fun appendError(msg: String) {
        _state.update { s ->
            s.copy(
                messages  = s.messages + ChatMessage("err_${System.currentTimeMillis()}", ChatRole.COACH, msg),
                isLoading = false,
            )
        }
    }

    private fun parseHours(body: String?): Int = try {
        Regex(""""hoursUntilReset"\s*:\s*(\d+)""").find(body ?: "")
            ?.groupValues?.get(1)?.toInt() ?: 6
    } catch (_: Exception) { 6 }
}
