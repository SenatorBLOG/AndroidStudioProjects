package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.repository.IntegrationRepository
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.utils.SessionCalculations
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
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

data class HomeUiState(
    val userName: String = "",
    val todayMinutes: Int = 0,
    val currentStreak: Int = 0,
    val restingHeartRate: Int? = null,
    val lastSleepHours: Double? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val meditationRepository: MeditationRepository,
    private val integrationRepository: IntegrationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        fetchProfile()
        observeLocalSessions()
        observeIntegrations()
        viewModelScope.launch { integrationRepository.refresh() }
    }

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

    private fun observeLocalSessions() {
        viewModelScope.launch {
            meditationRepository.getAllSessions().collect { sessions ->
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val todayMinutes = sessions
                    .filter { epochToDate(it.date, zone) == today }
                    .sumOf { it.duration } / 60
                val dates = sessions.map { epochToDate(it.date, zone) }.toSet()
                val streak = SessionCalculations.computeCurrentStreak(dates, today)
                _state.update { it.copy(todayMinutes = todayMinutes, currentStreak = streak) }
            }
        }
    }

    private fun observeIntegrations() {
        viewModelScope.launch {
            integrationRepository.integrations.collect { integrations ->
                if (integrations.isEmpty()) return@collect
                val latestHr = mergeHeartRateDays(
                    integrations.flatMap { it.data?.heartRate.orEmpty() }
                ).lastOrNull()?.let { it.restingRate ?: it.avgRate }

                val latestSleep = mergeSleepDays(
                    integrations.flatMap { it.data?.sleep.orEmpty() }
                ).lastOrNull()?.duration?.let { it / 60.0 }

                _state.update { it.copy(restingHeartRate = latestHr, lastSleepHours = latestSleep) }
            }
        }
    }

    private fun epochToDate(ms: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
}
