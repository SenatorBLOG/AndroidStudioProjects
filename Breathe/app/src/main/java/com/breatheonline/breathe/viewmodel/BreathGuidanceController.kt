package com.breatheonline.breathe.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech

/**
 * Handles all sensory feedback (TTS voice and haptic vibration) for a breathing session.
 * Extracted from [BreatheViewModel] so the ViewModel only orchestrates state,
 * while this class owns the Android-hardware concerns.
 *
 * Call [init] once when the session begins and [release] in [BreatheViewModel.onCleared].
 */
internal class BreathGuidanceController(private val context: Context) {

    private var tts:      TextToSpeech? = null
    private var ttsReady: Boolean        = false

    fun init() {
        tts = TextToSpeech(context) { status -> ttsReady = (status == TextToSpeech.SUCCESS) }
    }

    /** Fires vibration or TTS for the given phase, depending on [mode]. */
    fun apply(phase: BreathPhase, mode: GuidanceMode, gender: VoiceGender) {
        when (mode) {
            GuidanceMode.SILENT    -> Unit
            GuidanceMode.VIBRATION -> vibrateForPhase(phase)
            GuidanceMode.VOICE     -> speakPhase(phase, gender)
        }
    }

    /** Stops TTS and releases resources. Must be called when the ViewModel is cleared. */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // ── Private ───────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun vibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        else
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private fun vibrateForPhase(phase: BreathPhase) {
        val vib = vibrator() ?: return
        val effect = when (phase) {
            BreathPhase.INHALE        -> VibrationEffect.createOneShot(700, 90)
            BreathPhase.HOLD1,
            BreathPhase.HOLD2         -> VibrationEffect.createWaveform(longArrayOf(0, 200, 150, 200), -1)
            BreathPhase.EXHALE        -> VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
            else                      -> return
        }
        vib.vibrate(effect)
    }

    private fun speakPhase(phase: BreathPhase, gender: VoiceGender) {
        val text = when (phase) {
            BreathPhase.INHALE        -> "Breathe in"
            BreathPhase.HOLD1,
            BreathPhase.HOLD2         -> "Hold"
            BreathPhase.EXHALE        -> "Breathe out"
            else                      -> return
        }
        val engine = tts ?: return
        if (!ttsReady) return
        // Female: higher pitch + slower pace → warm, calm, non-robotic feel
        // Male:   lower pitch + natural pace
        engine.setPitch(if (gender == VoiceGender.FEMALE) 1.25f else 0.88f)
        engine.setSpeechRate(if (gender == VoiceGender.FEMALE) 0.75f else 0.88f)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
