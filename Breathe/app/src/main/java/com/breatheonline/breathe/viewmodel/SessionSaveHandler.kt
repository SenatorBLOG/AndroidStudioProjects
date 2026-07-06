package com.breatheonline.breathe.viewmodel

import android.content.Context
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.CreateSessionRequest
import com.breatheonline.breathe.data.models.MeditationSession
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Handles persisting a completed breathing session to Room and the API.
 * Extracted from [BreatheViewModel] to keep the ViewModel as an orchestrator
 * and isolate the save/sync logic for easier testing and reuse.
 */
class SessionSaveHandler @Inject constructor(
    private val apiService:      ApiService,
    private val meditationRepo:  MeditationRepository,
    @ApplicationContext private val context: Context,
) {

    /**
     * Saves the session:
     * 1. Attempts to POST to the API immediately.
     * 2. Persists to Room with [isSynced] = true/false based on the API result.
     * 3. If the API call failed, schedules [SyncWorker] to retry when network is available.
     */
    suspend fun save(
        exerciseType:  String,
        elapsedSeconds: Int,
        cycles:         Int,
        completedAt:    String,
        feedback:       SessionFeedback,
    ) {
        if (elapsedSeconds <= 0) return

        val synced = runCatching {
            apiService.createSession(
                CreateSessionRequest(
                    type             = exerciseType,
                    sessionLength    = (elapsedSeconds / 60).coerceAtLeast(1),
                    cycles           = cycles,
                    completedAt      = completedAt,
                    sessionDate      = completedAt,
                    timeOfDay        = feedback.timeOfDay,
                    moodBefore       = feedback.moodBefore,
                    moodAfter        = feedback.moodAfter,
                    focusLevel       = feedback.focusLevel,
                    stressLevel      = feedback.stressLevel,
                    breathingDepth   = feedback.breathingDepth,
                    calmnessScore    = feedback.calmnessScore,
                    distractionCount = feedback.distractionCount,
                    notes            = feedback.notes,
                    noiseLevel       = feedback.noiseLevel,
                )
            )
        }.map { it.isSuccessful }.getOrDefault(false)

        meditationRepo.insertSession(
            MeditationSession(
                duration         = elapsedSeconds,
                date             = System.currentTimeMillis(),
                isSynced         = synced,
                type             = exerciseType,
                cycles           = cycles,
                moodBefore       = feedback.moodBefore,
                moodAfter        = feedback.moodAfter,
                focusLevel       = feedback.focusLevel,
                stressLevel      = feedback.stressLevel,
                breathingDepth   = feedback.breathingDepth,
                calmnessScore    = feedback.calmnessScore,
                distractionCount = feedback.distractionCount,
                noiseLevel       = feedback.noiseLevel,
                notes            = feedback.notes,
                timeOfDay        = feedback.timeOfDay,
            )
        )

        if (!synced) SyncWorker.schedule(context)
    }
}
