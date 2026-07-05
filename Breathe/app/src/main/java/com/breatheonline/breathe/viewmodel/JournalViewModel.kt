package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.NlpInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalState(
    val insights:  NlpInsights? = null,
    val isLoading: Boolean      = false,
    val error:     String?      = null,
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { apiService.getNlpInsights() }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        _state.update { it.copy(insights = resp.body(), isLoading = false) }
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Server error ${resp.code()}") }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message ?: "Network error") }
                }
        }
    }
}
