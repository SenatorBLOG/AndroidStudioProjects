package com.example.breathe.viewmodel


import android.util.Log
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
        viewModelScope.launch {
            repository.getAllSessions().collect { sessionList ->
                _sessions.value = sessionList
                sessionList.forEach { session ->
                    Log.d("StatsViewModel", "Loaded session: Date=${toLocalDate(session.date)}, Duration=${session.duration}")
                }
                computeStats()
            }
        }
    }

    /**
     * Save a new session to the database.
     */

    fun saveSession(duration: Long, date: Long) {
        viewModelScope.launch {
            val session = MeditationSession(duration = duration.toInt(), date = date)
            repository.insertSession(session)
            Log.d("StatsViewModel", "Saved session: duration=$duration, date=$date, localDate=${toLocalDate(date)}")
        }
    }

    /**
     * Compute total minutes, best streak, and sessions this week from raw sessions.
     */
    private fun computeStats() {
        val list = _sessions.value
        val now = LocalDate.now(ZoneId.systemDefault())

        // Total meditation minutes
        val totalMin = (list.sumOf { it.duration } / 60).toInt()

        // Best streak calculation
        val dates = list.map { toLocalDate(it.date) }.toSet()
        var bestStreak = 0
        var currentStreak = 0

        // Improved streak calculation: iterate from today backwards
        if (dates.contains(now)) { // Start current streak if today has a session
            currentStreak = 1
            bestStreak = 1
        }

        var currentDate = now.minusDays(1) // Start checking from yesterday
        while (dates.contains(currentDate) && currentDate.isAfter(dates.minOrNull()?.minusDays(1) ?: LocalDate.MIN)) {
            currentStreak++
            bestStreak = maxOf(bestStreak, currentStreak)
            currentDate = currentDate.minusDays(1)
        }
        // If today had no session, and we are looking for overall best streak,
        // we need to iterate forward from minDate or through sorted unique dates
        // to find the longest sequence.
        // Let's refine the best streak calculation for accuracy.

        val sortedUniqueDates = dates.sorted() // Sort all unique dates

        if (sortedUniqueDates.isNotEmpty()) {
            currentStreak = 0
            bestStreak = 0 // Reset for actual calculation

            // Iterate through sorted unique dates to find the longest consecutive sequence
            var previousDate: LocalDate? = null
            for (date in sortedUniqueDates) {
                if (previousDate == null || date.isEqual(previousDate.plusDays(1))) {
                    currentStreak++
                } else {
                    currentStreak = 1 // Streak broken, restart
                }
                bestStreak = maxOf(bestStreak, currentStreak)
                previousDate = date
            }
        }

        // Sessions this week
        // Определяем начало текущей недели (например, понедельник)
        val today = LocalDate.now(ZoneId.systemDefault())
        val startOfWeek = today.with(DayOfWeek.MONDAY) // Это уже начало недели

        Log.d("StatsViewModel", "Calculating sessions for week starting: $startOfWeek (today: $today)")

        val thisWeekCount = list.count { session ->
            val sessionDate = toLocalDate(session.date)
            // sessionDate >= startOfWeek && sessionDate <= today.plusDays(1) // Учитываем текущий день
            sessionDate >= startOfWeek && sessionDate <= today
        }

        _state.value = StatsState(
            totalMeditationMinutes = totalMin,
            bestStreakDays = bestStreak,
            sessionsThisWeek = thisWeekCount
        )
        Log.d("StatsViewModel", "Computed Stats: Total=${totalMin}min, Best Streak=${bestStreak} days, Week Count=${thisWeekCount}")
    }

    /**
     * Convert epoch millis to LocalDate.
     */
    private fun toLocalDate(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate().also {
                // Log.d("StatsViewModel", "Converted timestamp $timestamp to $it") // Убрать лишний лог, чтобы не засорять
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