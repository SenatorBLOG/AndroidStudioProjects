package com.breatheonline.breathe.ui.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Process-local launcher for the AI coach bottom sheet.
 * Set `pendingPrompt` to a non-null string + `open = true` to trigger.
 * The host screen (StatsScreen) observes and opens the sheet.
 */
object AiCoachLauncher {
    val open: MutableState<Boolean> = mutableStateOf(false)
    val pendingPrompt: MutableState<String?> = mutableStateOf(null)

    fun request(prompt: String) {
        pendingPrompt.value = prompt
        open.value = true
    }

    fun dismiss() {
        open.value = false
        pendingPrompt.value = null
    }
}
