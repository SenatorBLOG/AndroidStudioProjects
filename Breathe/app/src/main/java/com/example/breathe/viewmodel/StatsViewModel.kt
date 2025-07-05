package com.example.breathe.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.local.dao.DayTotal
import com.example.breathe.data.models.MeditationSession
import com.example.breathe.data.repository.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI state representing overall stats.
 */
data class StatsState(
    val totalMeditationMinutes: Int = 0,
    val bestStreakDays: Int = 0,
    val sessionsThisWeek: Int = 0
)

/**
 * Single-day total aggregation for display.
 */
data class DayTotal(
    val day: String,
    val totalDuration: Long
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MeditationRepository
) : ViewModel() {

    // StateFlow for the three overall stats
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    // Raw sessions list
    private val _sessions = MutableStateFlow<List<MeditationSession>>(emptyList())
    val sessions: StateFlow<List<MeditationSession>> = _sessions.asStateFlow()

    /**
     * Aggregated per-day totals, coming directly from SQLite group-by query.
     */
    val totalsByDay: StateFlow<List<DayTotal>> = repository.getTotalsByDay()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        // Collect raw session list and update overall stats
        viewModelScope.launch {
            repository.getAllSessions().collect { list ->
                _sessions.value = list
                computeStats()
            }
        }
    }

    /**
     * Save a new session to the database.
     */

    fun saveSession(duration: Long, date: Long) {
        viewModelScope.launch {
            val session = MeditationSession(duration = duration, date = date)
            repository.insertSession(session)
            // Обновляем sessions вручную после сохранения
            val updatedSessions = repository.getAllSessions().first()
            _sessions.value = updatedSessions
            computeStats()
            android.util.Log.d("StatsViewModel", "Saved session: duration=$duration, date=$date, localDate=${toLocalDate(date)}, sessionsCount=${updatedSessions.size}")
        }
    }

    /**
     * Compute total minutes, best streak, and sessions this week from raw sessions.
     */
    private fun computeStats() {
        val list = _sessions.value
        val now = LocalDate.now()

        // Total meditation minutes
        val totalMin = (list.sumOf { it.duration } / 60).toInt()

        // Best streak calculation
        val dates = list.map { toLocalDate(it.date) }.toSet()
        var bestStreak = 0
        var currentStreak = 0
        var cursor = dates.minOrNull() ?: now
        while (cursor <= now) {
            if (dates.contains(cursor)) {
                currentStreak++
                bestStreak = maxOf(bestStreak, currentStreak)
            } else {
                currentStreak = 0
            }
            cursor = cursor.plusDays(1)
        }

        // Sessions this week
        val weekStart = now.with(DayOfWeek.MONDAY)
        val thisWeekCount = list.count { toLocalDate(it.date) in weekStart..now }

        _state.value = StatsState(
            totalMeditationMinutes = totalMin,
            bestStreakDays = bestStreak,
            sessionsThisWeek = thisWeekCount
        )
    }

    /**
     * Convert epoch millis to LocalDate.
     */
    private fun toLocalDate(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.of("America/Los_Angeles")) // Явно указываем PDT
            .toLocalDate().also {
                android.util.Log.d("StatsViewModel", "Converted timestamp $timestamp to $it")
            }

    /**
     * Format minutes into "HH:mm" clock string.
     */
    fun formatMinutesToClock(min: Int): String {
        val h = min / 60
        val m = min % 60
        return String.format("%02d:%02d", h, m)
    }
}