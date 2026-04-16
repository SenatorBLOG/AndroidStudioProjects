package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.local.dao.DayTotal
import com.breatheonline.breathe.data.local.dao.MeditationDao
import com.breatheonline.breathe.data.models.MeditationSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MeditationRepository @Inject constructor(
    private val dao: MeditationDao
) {
    fun getAllSessions(): Flow<List<MeditationSession>> {
        return dao.getAllSessions()
    }
    fun getTotalsByDay(): Flow<List<DayTotal>> =
        dao.getTotalsByDay()

    suspend fun insertSession(session: MeditationSession): Long =
        dao.insertSession(session)

    suspend fun markAsSynced(id: Long) =
        dao.markAsSynced(id)

    suspend fun updateRemoteId(id: Long, remoteId: String) =
        dao.updateRemoteId(id, remoteId)

    suspend fun getUnsyncedSessions(): List<MeditationSession> =
        dao.getUnsyncedSessions()
}
