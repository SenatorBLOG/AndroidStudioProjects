package com.example.breathe.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Shared DataStore delegate for user settings (notifications, reminder time, etc.).
 *
 * Defined here — not in ProfileScreen — so BroadcastReceivers and ViewModels
 * can access the same store without creating a duplicate instance.
 * The DataStore name intentionally matches the old private delegate in ProfileScreen.kt
 * so existing persisted values (notifications_enabled) are preserved.
 */
val Context.userPrefs: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

object UserPrefsKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val REMINDER_HOUR         = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE       = intPreferencesKey("reminder_minute")
}
