package com.example.breathe.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.CreateSessionRequest
import com.example.breathe.data.models.MeditationSession
import com.example.breathe.data.repository.MeditationRepository
import com.example.breathe.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val label:   String,
    val inhaleS: Int,
    val hold1S:  Int,
    val exhaleS: Int,
    val hold2S:  Int,
)

val BREATH_PRESETS = listOf(
    BreathPreset("box",       "Box",       4, 4, 4, 4),
    BreathPreset("4-7-8",     "4-7-8",     4, 7, 8, 0),
    BreathPreset("coherent",  "Calm",      5, 0, 5, 1),
    BreathPreset("wimhof",    "Energize",  2, 1, 2, 1),
    BreathPreset("belly",     "Belly",     4, 0, 6, 2),
    BreathPreset("alternate", "Alternate", 4, 4, 4, 2),
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
    // Session control
    val isPaused:          Boolean      = false,
    // Editable pattern (seconds, only mutable when IDLE)
    val inhaleS:           Int          = 4,
    val hold1S:            Int          = 7,
    val exhaleS:           Int          = 8,
    val hold2S:            Int          = 0,
    val selectedPreset:    String       = "4-7-8",
    // All-time stats (from local DB)
    val streakDays:        Int          = 0,
    val totalMinutes:      Int          = 0,
    val totalSessions:     Int          = 0,
    // Heart rate (nullable until a source is connected)
    val heartRate:         Int?         = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BreatheViewModel @Inject constructor(
    savedStateHandle:           SavedStateHandle,
    private val apiService:     ApiService,
    private val meditationRepo: MeditationRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val exerciseType: String = savedStateHandle["exerciseType"] ?: "4-7-8"

    private val _state = MutableStateFlow(buildInitialState(exerciseType))
    val state: StateFlow<BreatheState> = _state.asStateFlow()

    private var exerciseJob: Job? = null
    private var elapsedJob:  Job? = null
    private var sessionEnded = false

    private var pendingElapsed:     Int    = 0
    private var pendingCompletedAt: String = ""
    private var pendingCycles:      Int    = 0

    private var tts:      TextToSpeech? = null
    private var ttsReady: Boolean        = false

    init {
        tts = TextToSpeech(context) { status -> ttsReady = (status == TextToSpeech.SUCCESS) }
        observeLocalStats()
    }

    // ── Start / Pause / Resume ────────────────────────────────────────────────

    fun startSession() {
        if (_state.value.phase != BreathPhase.IDLE) return
        startElapsedTimer()
        startExercise()
    }

    fun togglePause() {
        val st = _state.value
        if (st.phase == BreathPhase.IDLE || st.phase == BreathPhase.DONE) return
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    // ── Pattern editor (only when IDLE) ───────────────────────────────────────

    fun updatePattern(inhaleS: Int, hold1S: Int, exhaleS: Int, hold2S: Int) {
        if (_state.value.phase != BreathPhase.IDLE) return
        val i = inhaleS.coerceIn(1, 10)
        val h1 = hold1S.coerceIn(0, 10)
        val e = exhaleS.coerceIn(1, 10)
        val h2 = hold2S.coerceIn(0, 10)
        _state.update {
            it.copy(
                inhaleS        = i,
                hold1S         = h1,
                exhaleS        = e,
                hold2S         = h2,
                selectedPreset = matchPreset(i, h1, e, h2),
            )
        }
    }

    fun applyPreset(key: String) {
        if (_state.value.phase != BreathPhase.IDLE) return
        val p = BREATH_PRESETS.firstOrNull { it.key == key } ?: return
        _state.update {
            it.copy(
                inhaleS        = p.inhaleS,
                hold1S         = p.hold1S,
                exhaleS        = p.exhaleS,
                hold2S         = p.hold2S,
                selectedPreset = key,
                exerciseName   = "${p.label} Breathing",
            )
        }
    }

    private fun matchPreset(i: Int, h1: Int, e: Int, h2: Int): String =
        BREATH_PRESETS.firstOrNull {
            it.inhaleS == i && it.hold1S == h1 && it.exhaleS == e && it.hold2S == h2
        }?.key ?: ""

    // ── Guidance ──────────────────────────────────────────────────────────────

    fun setGuidance(mode: GuidanceMode, gender: VoiceGender) {
        _state.update { it.copy(guidanceMode = mode, voiceGender = gender) }
    }

    private fun applyGuidance(phase: BreathPhase) {
        when (_state.value.guidanceMode) {
            GuidanceMode.SILENT    -> Unit
            GuidanceMode.VIBRATION -> vibrateForPhase(phase)
            GuidanceMode.VOICE     -> speakPhase(phase, _state.value.voiceGender)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        else
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private fun vibrateForPhase(phase: BreathPhase) {
        val vib = vibrator() ?: return
        val effect = when (phase) {
            BreathPhase.INHALE         -> VibrationEffect.createOneShot(700, 90)
            BreathPhase.HOLD1,
            BreathPhase.HOLD2          -> VibrationEffect.createWaveform(longArrayOf(0, 200, 150, 200), -1)
            BreathPhase.EXHALE         -> VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
            else                       -> return
        }
        vib.vibrate(effect)
    }

    private fun speakPhase(phase: BreathPhase, gender: VoiceGender) {
        val text = when (phase) {
            BreathPhase.INHALE         -> "Breathe in"
            BreathPhase.HOLD1,
            BreathPhase.HOLD2          -> "Hold"
            BreathPhase.EXHALE         -> "Breathe out"
            else                       -> return
        }
        val engine = tts ?: return
        if (!ttsReady) return
        engine.setPitch(if (gender == VoiceGender.FEMALE) 1.15f else 0.85f)
        engine.setSpeechRate(0.85f)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ── Breathing cycle ───────────────────────────────────────────────────────

    private fun startExercise() {
        exerciseJob = viewModelScope.launch {
            delay(400L)
            while (true) {
                while (_state.value.isPaused) delay(100L)
                runPhase(BreathPhase.INHALE,  _state.value.inhaleS * 1000)
                val h1 = _state.value.hold1S
                if (h1 > 0) runPhase(BreathPhase.HOLD1, h1 * 1000)
                runPhase(BreathPhase.EXHALE, _state.value.exhaleS * 1000)
                val h2 = _state.value.hold2S
                if (h2 > 0) runPhase(BreathPhase.HOLD2, h2 * 1000)
                _state.update { it.copy(cyclesCompleted = it.cyclesCompleted + 1) }
            }
        }
    }

    private suspend fun runPhase(phase: BreathPhase, durationMs: Int) {
        val secs = durationMs / 1000
        _state.update { it.copy(phase = phase, phaseDurationMs = durationMs, phaseSecondsLeft = secs) }
        applyGuidance(phase)
        repeat(secs) { elapsed ->
            delay(1000L)
            while (_state.value.isPaused) delay(100L)
            _state.update { it.copy(phaseSecondsLeft = secs - elapsed - 1) }
        }
    }

    // ── Elapsed timer ─────────────────────────────────────────────────────────

    private fun startElapsedTimer() {
        elapsedJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_state.value.isPaused) {
                    _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    // ── Stats (streak / totals) from local DB ─────────────────────────────────

    private fun observeLocalStats() {
        viewModelScope.launch {
            meditationRepo.getAllSessions().collect { sessions ->
                val zone  = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val dates = sessions.map { s ->
                    Instant.ofEpochMilli(s.date).atZone(zone).toLocalDate()
                }.toSet()
                val streak = run {
                    val start = when {
                        dates.contains(today)              -> today
                        dates.contains(today.minusDays(1)) -> today.minusDays(1)
                        else                               -> return@run 0
                    }
                    var count = 0; var day = start
                    while (dates.contains(day)) { count++; day = day.minusDays(1) }
                    count
                }
                _state.update {
                    it.copy(
                        streakDays    = streak,
                        totalMinutes  = sessions.sumOf { s -> s.duration } / 60,
                        totalSessions = sessions.size,
                    )
                }
            }
        }
    }

    // ── End session ───────────────────────────────────────────────────────────

    fun endSession() {
        if (sessionEnded) return
        sessionEnded = true
        exerciseJob?.cancel()
        elapsedJob?.cancel()

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

    fun submitFeedback(feedback: SessionFeedback) {
        _state.update { it.copy(showFeedbackModal = false) }
        if (pendingElapsed <= 0) return

        viewModelScope.launch {
            val synced = runCatching {
                apiService.createSession(
                    CreateSessionRequest(
                        type             = exerciseType,
                        sessionLength    = (pendingElapsed / 60).coerceAtLeast(1),
                        cycles           = pendingCycles,
                        completedAt      = pendingCompletedAt,
                        sessionDate      = pendingCompletedAt,
                        timeOfDay        = feedback.timeOfDay,
                        noiseLevel       = feedback.noiseLevel,
                        moodBefore       = feedback.moodBefore,
                        moodAfter        = feedback.moodAfter,
                        focusLevel       = feedback.focusLevel,
                        stressLevel      = feedback.stressLevel,
                        breathingDepth   = feedback.breathingDepth,
                        calmnessScore    = feedback.calmnessScore,
                        distractionCount = feedback.distractionCount,
                        notes            = feedback.notes,
                    )
                )
            }.map { it.isSuccessful }.getOrDefault(false)

            meditationRepo.insertSession(
                MeditationSession(
                    duration = pendingElapsed,
                    date     = System.currentTimeMillis(),
                    isSynced = synced,
                    type     = exerciseType,
                )
            )
            if (!synced) SyncWorker.schedule(context)
        }
    }

    fun skipFeedback() = submitFeedback(SessionFeedback())

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private data class PatternParams(
    val name: String, val i: Int, val h1: Int, val e: Int, val h2: Int,
)

private fun presetParams(exerciseType: String): PatternParams = when {
    exerciseType.startsWith("custom_") -> {
        val p = exerciseType.split("_")
        PatternParams(
            "Custom",
            p.getOrNull(1)?.toIntOrNull() ?: 4,
            p.getOrNull(2)?.toIntOrNull() ?: 0,
            p.getOrNull(3)?.toIntOrNull() ?: 4,
            p.getOrNull(4)?.toIntOrNull() ?: 0,
        )
    }
    else -> when (exerciseType) {
        "box"       -> PatternParams("Box",       4, 4, 4, 4)
        "wimhof"    -> PatternParams("Wim Hof",   2, 1, 2, 1)
        "coherent"  -> PatternParams("Calm",      5, 0, 5, 1)
        "belly"     -> PatternParams("Belly",     4, 0, 6, 2)
        "morning"   -> PatternParams("Morning",   4, 4, 4, 0)
        "alternate" -> PatternParams("Alternate", 4, 4, 4, 2)
        "deep"      -> PatternParams("Deep",      5, 0, 7, 0)
        "energy"    -> PatternParams("Energize",  4, 0, 6, 0)
        else        -> PatternParams("4-7-8",     4, 7, 8, 0)
    }
}

private fun buildInitialState(exerciseType: String): BreatheState {
    val p = presetParams(exerciseType)
    val presetKey = BREATH_PRESETS.firstOrNull { it.key == exerciseType }?.key
        ?: BREATH_PRESETS.firstOrNull {
            it.inhaleS == p.i && it.hold1S == p.h1 && it.exhaleS == p.e && it.hold2S == p.h2
        }?.key ?: ""
    return BreatheState(
        exerciseName   = "${p.name} Breathing",
        inhaleS        = p.i,
        hold1S         = p.h1,
        exhaleS        = p.e,
        hold2S         = p.h2,
        selectedPreset = presetKey,
    )
}
