package com.example.breathe.data.repository

import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.CreateSessionRequest
import com.example.breathe.data.models.MeditationSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Single source of truth for sessions.
 * Room is the local store; the API is synced in the background.
 */
class SessionRepository @Inject constructor(
    private val meditationRepo: MeditationRepository,
    private val apiService:     ApiService,
) {
    /** All sessions from Room, ordered newest-first. Reacts to DB changes. */
    fun getAll(): Flow<List<MeditationSession>> = meditationRepo.getAllSessions()

    /**
     * Push every unsynced Room session to the backend.
     * On success: updates [MeditationSession.remoteId] and sets [MeditationSession.isSynced].
     * On failure: leaves the record untouched — it will retry on the next call.
     */
    suspend fun sync() {
        val unsynced = meditationRepo.getUnsyncedSessions()
        unsynced.forEach { session ->
            runCatching {
                apiService.createSession(
                    CreateSessionRequest(
                        type          = session.type,
                        sessionLength = (session.duration / 60).coerceAtLeast(1),
                        cycles        = 0,
                        completedAt   = Instant.ofEpochMilli(session.date).toString(),
                        sessionDate   = Instant.ofEpochMilli(session.date).toString(),
                        noiseLevel    = "",
                    )
                )
            }.onSuccess { resp ->
                if (resp.isSuccessful) {
                    resp.body()?.let { remote ->
                        meditationRepo.updateRemoteId(session.id.toLong(), remote.id)
                    }
                }
            }
            // Failures are silently ignored — isSynced stays false for the next sync attempt
        }
    }
}
