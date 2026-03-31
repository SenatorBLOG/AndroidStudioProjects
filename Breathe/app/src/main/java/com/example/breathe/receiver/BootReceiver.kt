package com.example.breathe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.breathe.utils.NotificationScheduler
import com.example.breathe.utils.UserPrefsKeys
import com.example.breathe.utils.userPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Restores the daily reminder alarm after a device reboot.
 *
 * AlarmManager alarms are cleared on reboot; this receiver listens for
 * [Intent.ACTION_BOOT_COMPLETED] and re-schedules if the user had
 * notifications enabled.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs   = context.userPrefs.data.first()
                val enabled = prefs[UserPrefsKeys.NOTIFICATIONS_ENABLED] == true
                val hour    = prefs[UserPrefsKeys.REMINDER_HOUR]         ?: 8
                val minute  = prefs[UserPrefsKeys.REMINDER_MINUTE]       ?: 0
                if (enabled) scheduler.schedule(hour, minute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
