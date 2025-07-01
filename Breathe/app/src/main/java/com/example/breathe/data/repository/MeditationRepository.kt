package com.example.breathe.data.repository

import com.example.breathe.data.local.dao.DayTotal
import com.example.breathe.data.local.dao.MeditationDao
import com.example.breathe.data.models.MeditationSession
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

    suspend fun insertSession(session: MeditationSession) {
        dao.insertSession(session)
    }
}