package com.example.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.RemoteSession
import com.example.breathe.data.repository.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class HistoryState(
    val sessions: List<RemoteSession> = emptyList(),
    val status:   SessionsStatus      = SessionsStatus.Loading,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiService:     ApiService,
    private val meditationRepo: MeditationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        observeLocal()                            // show Room sessions instantly
        viewModelScope.launch { syncFromApi() }   // refresh from API in background
    }

    /**
     * Collect Room sessions and display them immediately.
     * Stops updating once the API has returned a successful response.
     */
    private fun observeLocal() {
        viewModelScope.launch {
            meditationRepo.getAllSessions().collect { local ->
                _state.update { current ->
                    // Don't overwrite a successful API response with local data
                    if (current.status is SessionsStatus.Success &&
                        current.sessions.any { it.id.length > 10 }  // looks like a Mongo id
                    ) return@update current

                    val mapped = local
                        .sortedByDescending { it.date }
                        .map { s ->
                            RemoteSession(
                                id          = s.remoteId ?: s.id.toString(),
                                type        = s.type,
                                duration    = s.duration,
                                completedAt = Instant.ofEpochMilli(s.date).toString(),
                            )
                        }
                    current.copy(
                        sessions = mapped,
                        status   = if (mapped.isEmpty()) SessionsStatus.Loading
                                   else                  SessionsStatus.Success,
                    )
                }
            }
        }
    }

    private suspend fun syncFromApi() {
        runCatching { apiService.getSessions() }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    val list = (resp.body() ?: emptyList())
                        .sortedByDescending { it.completedAt }
                    _state.update {
                        it.copy(
                            sessions = list,
                            status   = if (list.isEmpty()) SessionsStatus.Empty
                                       else               SessionsStatus.Success,
                        )
                    }
                } else {
                    // Keep showing local sessions; only show error if list is empty
                    _state.update { current ->
                        if (current.sessions.isEmpty())
                            current.copy(status = SessionsStatus.Error("Server error ${resp.code()}"))
                        else current
                    }
                }
            }
            .onFailure { err ->
                _state.update { current ->
                    if (current.sessions.isEmpty())
                        current.copy(status = SessionsStatus.Error(err.message ?: "Network error"))
                    else current
                }
            }
    }

    fun fetch() {
        viewModelScope.launch {
            // Show loading spinner only when the list is currently empty
            if (_state.value.sessions.isEmpty()) {
                _state.update { it.copy(status = SessionsStatus.Loading) }
            }
            syncFromApi()
        }
    }
}
