package com.example.breathe

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension property
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Preference keys
val totalMeditationTimeKey = longPreferencesKey("total_meditation_time")
val bestStreakKey = intPreferencesKey("best_streak")
val sessionsThisWeekKey = intPreferencesKey("sessions_this_week")
val lastSessionDateKey = stringPreferencesKey("last_session_date")
val lastWeekResetDateKey = stringPreferencesKey("last_week_reset_date")
val currentStreakKey = intPreferencesKey("current_streak")
val themeKey = stringPreferencesKey("theme")

// Helper functions to read from DataStore
fun Context.getTotalMeditationTimeFlow(): Flow<Long> {
    return dataStore.data.map { it[totalMeditationTimeKey] ?: 0L }
}

fun Context.getBestStreakFlow(): Flow<Int> {
    return dataStore.data.map { it[bestStreakKey] ?: 0 }
}

fun Context.getSessionsThisWeekFlow(): Flow<Int> {
    return dataStore.data.map { it[sessionsThisWeekKey] ?: 0 }
}

fun Context.getThemeFlow(): Flow<String> {
    return dataStore.data.map { it[themeKey] ?: "Ocean" }
}

suspend fun Context.updateTheme(newTheme: String) {
    dataStore.edit { settings ->
        settings[themeKey] = newTheme
    }
}