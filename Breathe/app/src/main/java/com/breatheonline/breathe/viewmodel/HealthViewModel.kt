package com.breatheonline.breathe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.HrDayDto
import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthState(
    val isLoading:    Boolean         = true,
    val error:        String?         = null,
    val sources:      List<String>    = emptyList(),
    // Sorted ascending (oldest → newest) for chart rendering
    val sleepDays:    List<SleepDayDto> = emptyList(),
    val heartRateDays: List<HrDayDto>  = emptyList(),
    // Aggregates
    val avgSleep7dMin: Int?  = null,
    val restingHr:     Int?  = null,
    val recoveryScore: Int?  = null,
    val hasData:       Boolean = false,
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(HealthState())
    val state: StateFlow<HealthState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { apiService.getIntegrationStatus() }
                .onSuccess { resp ->
                    if (!resp.isSuccessful) {
                        _state.update { it.copy(isLoading = false, error = "Failed to load health data") }
                        return@onSuccess
                    }
                    val connected = resp.body()?.filter { it.connected && it.data != null } ?: emptyList()

                    val allSleep = mergeSleepDays(connected
                        .flatMap { it.data?.sleep ?: emptyList() }
                    ).takeLast(14)

                    val allHr = mergeHeartRateDays(connected
                        .flatMap { it.data?.heartRate ?: emptyList() }
                    ).takeLast(14)

                    val last7Sleep = allSleep.takeLast(7)
                    val avgSleep   = if (last7Sleep.isEmpty()) null else last7Sleep.map { it.duration }.average().toInt()
                    val restingHr  = allHr.lastOrNull { it.restingRate != null }?.restingRate
                    val recovery   = restingHr?.let { hr -> ((120 - hr).toFloat() / 60f * 100f).toInt().coerceIn(0, 100) }

                    _state.update {
                        it.copy(
                            isLoading     = false,
                            sources       = connected.map { c -> c.provider },
                            sleepDays     = allSleep,
                            heartRateDays = allHr,
                            avgSleep7dMin = avgSleep,
                            restingHr     = restingHr,
                            recoveryScore = recovery,
                            hasData       = allSleep.isNotEmpty() || allHr.isNotEmpty(),
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("HealthViewModel", "getIntegrationStatus failed", e)
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Network error") }
                }
        }
    }
}
