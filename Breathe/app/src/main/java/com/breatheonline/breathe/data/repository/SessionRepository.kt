package com.breatheonline.breathe.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.CreateSessionRequest
import com.breatheonline.breathe.data.models.MeditationSession
import com.breatheonline.breathe.utils.UserPrefsKeys
import com.breatheonline.breathe.utils.userPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Single source of truth for sessions.
 * Room is the local store; the API is synced in the background.
 */
class SessionRepository @Inject constructor(
    private val meditationRepo: MeditationRepository,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) {
    /** All sessions from Room, ordered newest-first. Reacts to DB changes. */
    fun getAll(): Flow<List<MeditationSession>> = meditationRepo.getAllSessions()

    /**
     * Push every unsynced Room session to the backend.
     * On success: updates [MeditationSession.remoteId] and sets [MeditationSession.isSynced].
     * On failure: leaves the record untouched — it will retry on the next call.
     */
    suspend fun sync() {
        // Respect the user's data-collection preference — don't upload if opted out
        val prefs = context.userPrefs.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        if (prefs[UserPrefsKeys.DATA_COLLECTION_ENABLED] == false) return

        val unsynced = meditationRepo.getUnsyncedSessions()
        unsynced.forEach { session ->
            runCatching {
                val instant     = Instant.ofEpochMilli(session.date)
                val completedAt = instant.toString()
                val sessionDate = instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()
                apiService.createSession(
                    CreateSessionRequest(
                        type             = session.type,
                        sessionLength    = (session.duration / 60).coerceAtLeast(1),
                        cycles           = session.cycles,
                        completedAt      = completedAt,
                        sessionDate      = sessionDate,
                        timeOfDay        = session.timeOfDay,
                        moodBefore       = session.moodBefore,
                        moodAfter        = session.moodAfter,
                        focusLevel       = session.focusLevel,
                        stressLevel      = session.stressLevel,
                        breathingDepth   = session.breathingDepth,
                        calmnessScore    = session.calmnessScore,
                        distractionCount = session.distractionCount,
                        notes            = session.notes,
                        noiseLevel       = session.noiseLevel,
                    )
                )
            }.onSuccess { resp ->
                if (resp.isSuccessful) {
                    meditationRepo.markAsSynced(session.id.toLong())
                    resp.body()?.let { remote ->
                        meditationRepo.updateRemoteId(session.id.toLong(), remote.id)
                    }
                }
            }
            // Failures are silently ignored — isSynced stays false for the next sync attempt
        }
    }
}
