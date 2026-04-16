package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.example.breathe.data.repository.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

data class HomeUiState(
    val userName:      String = "",
    val todayMinutes:  Int    = 0,
    val currentStreak: Int    = 0,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val meditationRepository: MeditationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        fetchProfile()
        observeLocalSessions()
    }

    // ── Profile (first name for greeting) ─────────────────────────────────────

    private fun fetchProfile() {
        viewModelScope.launch {
            runCatching { apiService.getProfile() }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val first = resp.body()?.name?.split(" ")?.firstOrNull().orEmpty()
                        _state.update { it.copy(userName = first) }
                    }
                }
        }
    }

    // ── Local sessions → today's minutes + current streak ─────────────────────

    private fun observeLocalSessions() {
        viewModelScope.launch {
            meditationRepository.getAllSessions().collect { sessions ->
                val zone  = ZoneId.systemDefault()
                val today = LocalDate.now(zone)

                val todayMinutes = sessions
                    .filter { epochToDate(it.date, zone) == today }
                    .sumOf { it.duration } / 60

                val dates  = sessions.map { epochToDate(it.date, zone) }.toSet()
                val streak = computeCurrentStreak(dates, today)

                _state.update { it.copy(todayMinutes = todayMinutes, currentStreak = streak) }
            }
        }
    }

    private fun epochToDate(ms: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()

    /**
     * Count consecutive days ending today (or yesterday if today has no session).
     */
    private fun computeCurrentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        val start = when {
            dates.contains(today)               -> today
            dates.contains(today.minusDays(1))  -> today.minusDays(1)
            else                                -> return 0
        }
        var count = 0
        var day   = start
        while (dates.contains(day)) { count++; day = day.minusDays(1) }
        return count
    }
}
