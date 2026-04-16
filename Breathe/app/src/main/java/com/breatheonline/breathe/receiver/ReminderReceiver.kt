package com.breatheonline.breathe.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.breathe.MainActivity
import com.breatheonline.breathe.R
import com.breatheonline.breathe.utils.NotificationScheduler
import com.breatheonline.breathe.utils.UserPrefsKeys
import com.breatheonline.breathe.utils.userPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when the daily alarm triggers.
 *
 * 1. Shows the breathing reminder notification immediately.
 * 2. Reschedules the alarm for the same time tomorrow (via [goAsync] so the
 *    coroutine completes before the receiver is recycled).
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)

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

    private fun showNotification(context: Context) {
        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationScheduler.Companion.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NotificationScheduler.Companion.NOTIFICATION_ID, notification)
    }
}
