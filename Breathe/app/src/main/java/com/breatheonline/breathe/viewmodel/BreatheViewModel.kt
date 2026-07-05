package com.breatheonline.breathe.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.utils.SessionCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// ── Phase / Guidance ──────────────────────────────────────────────────────────

enum class BreathPhase  { IDLE, INHALE, HOLD1, EXHALE, HOLD2, DONE }
enum class GuidanceMode { SILENT, VIBRATION, VOICE }
enum class VoiceGender  { FEMALE, MALE }

// ── Presets ───────────────────────────────────────────────────────────────────

data class BreathPreset(
    val key:     String,
    @StringRes val labelRes: Int,
    val inhaleS: Int,
    val hold1S:  Int,
    val exhaleS: Int,
    val hold2S:  Int,
)

val BREATH_PRESETS = listOf(
    BreathPreset("box",       R.string.history_session_box,       4, 4, 4, 4),
    BreathPreset("4-7-8",     R.string.history_session_4_7_8,     4, 7, 8, 0),
    BreathPreset("coherent",  R.string.history_session_coherent,  5, 0, 5, 1),
    BreathPreset("wimhof",    R.string.history_session_wim_hof,   2, 1, 2, 1),
    BreathPreset("belly",     R.string.history_session_belly,     4, 0, 6, 2),
    BreathPreset("alternate", R.string.history_session_alternate, 4, 4, 4, 2),
    BreathPreset("morning",   R.string.history_session_morning,   4, 4, 4, 0),
)

// ── Feedback ──────────────────────────────────────────────────────────────────

data class SessionFeedback(
    val moodBefore:       Int    = 5,
    val moodAfter:        Int    = 5,
    val focusLevel:       Int    = 5,
    val stressLevel:      Int    = 5,
    val breathingDepth:   Int    = 5,
    val calmnessScore:    Int    = 5,
    val distractionCount: Int    = 0,
    val noiseLevel:       String = "Quiet",
    val notes:            String = "",
    val timeOfDay:        String = "",
)

// ── UI State ──────────────────────────────────────────────────────────────────

data class BreatheState(
    val phase:             BreathPhase  = BreathPhase.IDLE,
    val phaseDurationMs:   Int          = 0,
    val phaseSecondsLeft:  Int          = 0,
    val cyclesCompleted:   Int          = 0,
    val elapsedSeconds:    Int          = 0,
    val exerciseName:      String       = "",
    val isCompleted:       Boolean      = false,
    val showFeedbackModal: Boolean      = false,
    val guidanceMode:      GuidanceMode = GuidanceMode.SILENT,
    val voiceGender:       VoiceGender  = VoiceGender.FEMALE,
    val isPaused:          Boolean      = false,
    // Editable pattern (only mutable when IDLE)
    val inhaleS:           Int          = 4,
    val hold1S:            Int          = 7,
    val exhaleS:           Int          = 8,
    val hold2S:            Int          = 0,
    val selectedPreset:    String       = "4-7-8",
    // All-time stats (from local DB)
    val streakDays:        Int          = 0,
    val totalMinutes:      Int          = 0,
    val totalSessions:     Int          = 0,
    val heartRate:         Int?         = null,
)

// ── ViewModel — orchestrates state; delegates business logic to controllers ────

@HiltViewModel
class BreatheViewModel @Inject constructor(
    savedStateHandle:     SavedStateHandle,
    private val saveHandler:      SessionSaveHandler,
    private val meditationRepo:   MeditationRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val exerciseType: String = savedStateHandle["exerciseType"] ?: "4-7-8"

    private val _state = MutableStateFlow(buildInitialState(exerciseType, context))
    val state: StateFlow<BreatheState> = _state.asStateFlow()

    // Extracted controllers
    private val timer    = BreathTimerController(viewModelScope)
    private val guidance = BreathGuidanceController(context)

    // Pending session data captured when the user ends a session
    private var sessionEnded       = false
    private var sessionSaved       = false
    private var pendingElapsed     = 0
    private var pendingCycles      = 0
    private var pendingCompletedAt = ""

    init {
        guidance.init()
        observeLocalStats()
    }

    // ── Session control ───────────────────────────────────────────────────────

    fun startSession() {
        if (_state.value.phase != BreathPhase.IDLE) return
        val st = _state.value
        timer.start(
            inhaleS          = st.inhaleS,
            hold1S           = st.hold1S,
            exhaleS          = st.exhaleS,
            hold2S           = st.hold2S,
            isPaused         = { _state.value.isPaused },
            onPhaseChanged   = { ph, durMs, secsLeft ->
                _state.update { it.copy(phase = ph, phaseDurationMs = durMs, phaseSecondsLeft = secsLeft) }
            },
            onPhaseTick      = { secsLeft -> _state.update { it.copy(phaseSecondsLeft = secsLeft) } },
            onElapsedTick    = { _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) } },
            onCycleCompleted = { _state.update { it.copy(cyclesCompleted = it.cyclesCompleted + 1) } },
            onGuidance       = { phase ->
                val s = _state.value
                guidance.apply(phase, s.guidanceMode, s.voiceGender)
            },
        )
    }

    fun togglePause() {
        val phase = _state.value.phase
        if (phase == BreathPhase.IDLE || phase == BreathPhase.DONE) return
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    fun endSession() {
        if (sessionEnded) return
        sessionEnded = true
        timer.stop()
        pendingElapsed     = _state.value.elapsedSeconds
        pendingCycles      = _state.value.cyclesCompleted
        pendingCompletedAt = Instant.now().toString()
        _state.update {
            it.copy(
                phase             = BreathPhase.DONE,
                isCompleted       = true,
                isPaused          = false,
                showFeedbackModal = pendingElapsed > 0,
            )
        }
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    fun submitFeedback(feedback: SessionFeedback) {
        if (sessionSaved) return
        sessionSaved = true
        _state.update { it.copy(showFeedbackModal = false) }
        viewModelScope.launch {
            saveHandler.save(
                exerciseType   = exerciseType,
                elapsedSeconds = pendingElapsed,
                cycles         = pendingCycles,
                completedAt    = pendingCompletedAt,
                feedback       = feedback,
            )
        }
    }

    fun skipFeedback() = submitFeedback(SessionFeedback())

    // ── Pattern editor (only when IDLE) ───────────────────────────────────────

    fun updatePattern(inhaleS: Int, hold1S: Int, exhaleS: Int, hold2S: Int) {
        if (_state.value.phase != BreathPhase.IDLE) return
        val i  = inhaleS.coerceIn(1, 10)
        val h1 = hold1S.coerceIn(0, 10)
        val e  = exhaleS.coerceIn(1, 10)
        val h2 = hold2S.coerceIn(0, 10)
        _state.update {
            it.copy(inhaleS = i, hold1S = h1, exhaleS = e, hold2S = h2,
                selectedPreset = matchPreset(i, h1, e, h2))
        }
    }

    fun applyPreset(key: String) {
        if (_state.value.phase != BreathPhase.IDLE) return
        val p = BREATH_PRESETS.firstOrNull { it.key == key } ?: return
        _state.update {
            it.copy(inhaleS = p.inhaleS, hold1S = p.hold1S, exhaleS = p.exhaleS, hold2S = p.hold2S,
                selectedPreset = key, exerciseName = context.getString(p.labelRes))
        }
    }

    fun setGuidance(mode: GuidanceMode, gender: VoiceGender) {
        _state.update { it.copy(guidanceMode = mode, voiceGender = gender) }
    }

    // ── Local DB stats ────────────────────────────────────────────────────────

    private fun observeLocalStats() {
        viewModelScope.launch {
            meditationRepo.getAllSessions().collect { sessions ->
                val zone  = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val dates = sessions.map { s ->
                    Instant.ofEpochMilli(s.date).atZone(zone).toLocalDate()
                }.toSet()
                _state.update {
                    it.copy(
                        streakDays    = SessionCalculations.computeCurrentStreak(dates, today),
                        totalMinutes  = sessions.sumOf { s -> s.duration } / 60,
                        totalSessions = sessions.size,
                    )
                }
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        guidance.release()
        super.onCleared()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun matchPreset(i: Int, h1: Int, e: Int, h2: Int): String =
        BREATH_PRESETS.firstOrNull {
            it.inhaleS == i && it.hold1S == h1 && it.exhaleS == e && it.hold2S == h2
        }?.key ?: ""
}

// ── File-level helpers ────────────────────────────────────────────────────────

private data class PatternParams(@StringRes val labelRes: Int, val i: Int, val h1: Int, val e: Int, val h2: Int)

private fun presetParams(exerciseType: String): PatternParams = when {
    exerciseType.startsWith("custom_") -> {
        val p = exerciseType.split("_")
        PatternParams(R.string.history_session_custom,
            p.getOrNull(1)?.toIntOrNull() ?: 4,
            p.getOrNull(2)?.toIntOrNull() ?: 0,
            p.getOrNull(3)?.toIntOrNull() ?: 4,
            p.getOrNull(4)?.toIntOrNull() ?: 0)
    }
    else -> when (exerciseType) {
        "box"       -> PatternParams(R.string.history_session_box,       4, 4, 4, 4)
        "wimhof"    -> PatternParams(R.string.history_session_wim_hof,   2, 1, 2, 1)
        "coherent"  -> PatternParams(R.string.history_session_coherent,  5, 0, 5, 1)
        "belly"     -> PatternParams(R.string.history_session_belly,     4, 0, 6, 2)
        "morning"   -> PatternParams(R.string.history_session_morning,   4, 4, 4, 0)
        "alternate" -> PatternParams(R.string.history_session_alternate, 4, 4, 4, 2)
        "deep"      -> PatternParams(R.string.history_session_deep,      5, 0, 7, 0)
        "energy"    -> PatternParams(R.string.history_session_energising, 4, 0, 6, 0)
        else        -> PatternParams(R.string.history_session_4_7_8,     4, 7, 8, 0)
    }
}

private fun buildInitialState(exerciseType: String, context: Context): BreatheState {
    val p = presetParams(exerciseType)
    val presetKey = BREATH_PRESETS.firstOrNull { it.key == exerciseType }?.key
        ?: BREATH_PRESETS.firstOrNull {
            it.inhaleS == p.i && it.hold1S == p.h1 && it.exhaleS == p.e && it.hold2S == p.h2
        }?.key ?: ""
    return BreatheState(
        exerciseName   = context.getString(p.labelRes),
        inhaleS        = p.i,
        hold1S         = p.h1,
        exhaleS        = p.e,
        hold2S         = p.h2,
        selectedPreset = presetKey,
    )
}
