package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.CoachMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StoryState {
    data object Idle    : StoryState
    data object Loading : StoryState
    data class  Success(val text: String) : StoryState
    data class  Error(val message: String) : StoryState
}

@HiltViewModel
class SleepStoryViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _storyState = MutableStateFlow<StoryState>(StoryState.Idle)
    val storyState: StateFlow<StoryState> = _storyState.asStateFlow()

    fun generateStory() {
        if (_storyState.value is StoryState.Loading) return
        _storyState.value = StoryState.Loading
        viewModelScope.launch {
            runCatching {
                apiService.sendCoachMessage(
                    CoachMessageRequest(
                        message = "Generate a short, calming bedtime story for sleep and relaxation. Make it peaceful, dreamy, and soothing — about 150-200 words. No headings, just the story.",
                        history = emptyList(),
                    )
                )
            }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    val text = resp.body()?.reply ?: "Sweet dreams..."
                    _storyState.value = StoryState.Success(text)
                } else {
                    _storyState.value = StoryState.Error("Server error ${resp.code()}")
                }
            }
            .onFailure { err ->
                _storyState.value = StoryState.Error(err.message ?: "Network error")
            }
        }
    }

    fun reset() { _storyState.value = StoryState.Idle }
}
