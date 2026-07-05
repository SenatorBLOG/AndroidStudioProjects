package com.breatheonline.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.CreatePinRequest
import com.breatheonline.breathe.data.models.GlobePinDto
import com.breatheonline.breathe.data.models.GlobeStatsDto
import com.breatheonline.breathe.data.models.ReportRequest
import com.breatheonline.breathe.utils.UserPrefsKeys
import com.breatheonline.breathe.utils.userPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Status ────────────────────────────────────────────────────────────────────

sealed interface PinsStatus {
    data object Loading : PinsStatus
    data object Empty   : PinsStatus
    data object Success : PinsStatus
    data class  Error(val message: String) : PinsStatus
}

// ── UI state ──────────────────────────────────────────────────────────────────

data class GlobePinsUiState(
    val pins:            List<GlobePinDto> = emptyList(),
    val stats:           GlobeStatsDto?    = null,
    val status:          PinsStatus        = PinsStatus.Loading,
    val filterTech:      String            = "all",
    val isAddingPin:     Boolean           = false,
    val snackbarMessage: String?           = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class GlobePinsViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(GlobePinsUiState())
    val state: StateFlow<GlobePinsUiState> = _state.asStateFlow()

    init {
        loadPins()
        loadStats()
    }

    fun loadPins() {
        viewModelScope.launch {
            _state.update { it.copy(status = PinsStatus.Loading) }
            val tech = _state.value.filterTech.takeIf { it != "all" }
            runCatching { apiService.getGlobePins(technique = tech) }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val pins = resp.body() ?: emptyList()
                        _state.update {
                            it.copy(
                                pins   = pins,
                                status = if (pins.isEmpty()) PinsStatus.Empty else PinsStatus.Success,
                            )
                        }
                    } else {
                        _state.update { it.copy(status = PinsStatus.Error(context.getString(R.string.globe_server_error, resp.code()))) }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(status = PinsStatus.Error(err.message ?: context.getString(R.string.globe_network_error))) }
                }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            runCatching { apiService.getGlobeStats() }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        _state.update { it.copy(stats = resp.body()) }
                    }
                }
        }
    }

    fun setFilterTech(tech: String) {
        if (_state.value.filterTech == tech) return
        _state.update { it.copy(filterTech = tech) }
        loadPins()
    }

    fun likePin(pinId: String) {
        val original = _state.value.pins.find { it.id == pinId } ?: return
        _state.update { s ->
            s.copy(pins = s.pins.map { p ->
                if (p.id == pinId) p.copy(likeCount = p.likeCount + 1) else p
            })
        }
        viewModelScope.launch {
            runCatching { apiService.likePin(pinId) }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body() ?: return@onSuccess
                        _state.update { s ->
                            s.copy(pins = s.pins.map { p ->
                                if (p.id == pinId) p.copy(likeCount = body.likeCount) else p
                            })
                        }
                    } else {
                        _state.update { s ->
                            s.copy(pins = s.pins.map { p -> if (p.id == pinId) original else p })
                        }
                    }
                }
                .onFailure {
                    _state.update { s ->
                        s.copy(pins = s.pins.map { p -> if (p.id == pinId) original else p })
                    }
                }
        }
    }

    fun reportPin(pinId: String) {
        viewModelScope.launch {
            runCatching { apiService.reportPin(pinId, ReportRequest()) }
                .onSuccess { _state.update { it.copy(snackbarMessage = context.getString(R.string.globe_report_submitted)) } }
                .onFailure { _state.update { it.copy(snackbarMessage = context.getString(R.string.globe_report_failed)) } }
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            runCatching { apiService.blockUser(userId) }
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            pins            = s.pins.filter { it.userId != userId },
                            snackbarMessage = context.getString(R.string.globe_user_blocked),
                        )
                    }
                }
                .onFailure { _state.update { it.copy(snackbarMessage = context.getString(R.string.globe_block_user_failed)) } }
        }
    }

    fun consumeSnackbar() { _state.update { it.copy(snackbarMessage = null) } }

    /** Returns true if the user has allowed optional data collection. */
    private suspend fun dataCollectionAllowed(): Boolean {
        val prefs = context.userPrefs.data.first()
        return prefs[UserPrefsKeys.DATA_COLLECTION_ENABLED] != false
    }

    fun createPin(
        lat:       Double,
        lng:       Double,
        city:      String,
        country:   String,
        title:     String,
        note:      String,
        technique: String,
    ) {
        viewModelScope.launch {
            if (!dataCollectionAllowed()) {
                _state.update { it.copy(snackbarMessage = context.getString(R.string.globe_data_collection_off)) }
                return@launch
            }
            _state.update { it.copy(isAddingPin = true) }
            runCatching {
                apiService.createPin(
                    CreatePinRequest(
                        lat = lat,
                        lng = lng,
                        city = city,
                        country = country,
                        title = title.ifBlank { context.getString(R.string.globe_meditation_spot) },
                        note = note,
                        technique = technique,
                    )
                )
            }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val pin = resp.body()
                        _state.update { s ->
                            s.copy(
                                pins       = listOfNotNull(pin) + s.pins,
                                status     = PinsStatus.Success,
                                isAddingPin = false,
                            )
                        }
                    } else {
                        _state.update { it.copy(isAddingPin = false) }
                    }
                }
                .onFailure {
                    _state.update { it.copy(isAddingPin = false) }
                }
        }
    }
}
