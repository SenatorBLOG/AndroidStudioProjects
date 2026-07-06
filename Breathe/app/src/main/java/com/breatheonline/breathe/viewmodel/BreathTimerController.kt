package com.breatheonline.breathe.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages the breathing phase cycle and the elapsed-time ticker.
 * Pure Kotlin — no Android or ViewModel dependencies, making it easy to unit-test.
 *
 * [BreatheViewModel] creates one instance and delegates all timer logic here,
 * keeping the ViewModel as an orchestrator of state rather than a container
 * for coroutine bookkeeping.
 */
internal class BreathTimerController(private val scope: CoroutineScope) {

    private var exerciseJob: Job? = null
    private var elapsedJob:  Job? = null

    /**
     * Starts the phase-cycling loop and the elapsed timer.
     * All pattern values are captured at [start]-time; they cannot be changed mid-session.
     *
     * @param isPaused  lambda returning the current paused flag from ViewModel state
     * @param onPhaseChanged  called when a new phase begins: (phase, durationMs, secsLeft)
     * @param onPhaseTick     called every second during a phase with the remaining seconds
     * @param onElapsedTick   called every second the session is not paused
     * @param onCycleCompleted  called each time one full inhale→exhale cycle finishes
     * @param onGuidance      called when guidance (TTS/vibration) should fire for a phase
     */
    fun start(
        inhaleS: Int,
        hold1S:  Int,
        exhaleS: Int,
        hold2S:  Int,
        isPaused:         () -> Boolean,
        onPhaseChanged:   (BreathPhase, durationMs: Int, secsLeft: Int) -> Unit,
        onPhaseTick:      (secsLeft: Int) -> Unit,
        onElapsedTick:    () -> Unit,
        onCycleCompleted: () -> Unit,
        onGuidance:       (BreathPhase) -> Unit,
    ) {
        startElapsedTimer(isPaused, onElapsedTick)
        startExercise(inhaleS, hold1S, exhaleS, hold2S, isPaused, onPhaseChanged, onPhaseTick, onCycleCompleted, onGuidance)
    }

    /** Cancels both the phase loop and the elapsed timer. */
    fun stop() {
        exerciseJob?.cancel()
        elapsedJob?.cancel()
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun startElapsedTimer(isPaused: () -> Boolean, onElapsedTick: () -> Unit) {
        elapsedJob = scope.launch {
            while (true) {
                delay(1000L)
                if (!isPaused()) onElapsedTick()
            }
        }
    }

    private fun startExercise(
        inhaleS: Int, hold1S: Int, exhaleS: Int, hold2S: Int,
        isPaused:         () -> Boolean,
        onPhaseChanged:   (BreathPhase, Int, Int) -> Unit,
        onPhaseTick:      (Int) -> Unit,
        onCycleCompleted: () -> Unit,
        onGuidance:       (BreathPhase) -> Unit,
    ) {
        exerciseJob = scope.launch {
            delay(400L)
            while (true) {
                while (isPaused()) delay(250L)
                runPhase(BreathPhase.INHALE, inhaleS * 1000, isPaused, onPhaseChanged, onPhaseTick, onGuidance)
                if (hold1S > 0) runPhase(BreathPhase.HOLD1, hold1S * 1000, isPaused, onPhaseChanged, onPhaseTick, onGuidance)
                runPhase(BreathPhase.EXHALE, exhaleS * 1000, isPaused, onPhaseChanged, onPhaseTick, onGuidance)
                if (hold2S > 0) runPhase(BreathPhase.HOLD2, hold2S * 1000, isPaused, onPhaseChanged, onPhaseTick, onGuidance)
                onCycleCompleted()
            }
        }
    }

    private suspend fun runPhase(
        phase:          BreathPhase,
        durationMs:     Int,
        isPaused:       () -> Boolean,
        onPhaseChanged: (BreathPhase, Int, Int) -> Unit,
        onPhaseTick:    (Int) -> Unit,
        onGuidance:     (BreathPhase) -> Unit,
    ) {
        val secs = durationMs / 1000
        onPhaseChanged(phase, durationMs, secs)
        onGuidance(phase)
        repeat(secs) { elapsed ->
            delay(1000L)
            while (isPaused()) delay(250L)
            onPhaseTick(secs - elapsed - 1)
        }
    }
}
