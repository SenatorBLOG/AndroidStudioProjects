package com.breatheonline.breathe.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.breatheonline.breathe.receiver.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels the daily breathing reminder via [AlarmManager].
 *
 * - Uses [AlarmManager.setExactAndAllowWhileIdle] when exact alarms are permitted
 *   (always on API < 31; requires [AlarmManager.canScheduleExactAlarms] on API 31+).
 * - Falls back to [AlarmManager.setAndAllowWhileIdle] (inexact, ±1 h) otherwise so
 *   that the reminder still fires even without the exact-alarm permission.
 * - Each alarm fires [ReminderReceiver], which shows the notification and
 *   re-schedules itself for the next day via [goAsync].
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID      = "breathe_reminders"
        const val NOTIFICATION_ID = 1001
        private const val ALARM_REQUEST_CODE = 2001
    }

    init { ensureChannelExists() }

    // ── Notification channel ──────────────────────────────────────────────────

    fun ensureChannelExists() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Daily breathing exercise reminders" }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    // ── Scheduling ────────────────────────────────────────────────────────────

    /**
     * Schedule (or reschedule) the daily alarm for [hour]:[minute].
     * If the time has already passed today the alarm fires tomorrow.
     */
    fun schedule(hour: Int, minute: Int) {
        val am      = context.getSystemService(AlarmManager::class.java)
        val trigger = nextTriggerMillis(hour, minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarmIntent())
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarmIntent())
        }
    }

    fun cancel() {
        context.getSystemService(AlarmManager::class.java).cancel(alarmIntent())
    }

    /** Returns true when exact alarms are available on this device. */
    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        else true

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun nextTriggerMillis(hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
