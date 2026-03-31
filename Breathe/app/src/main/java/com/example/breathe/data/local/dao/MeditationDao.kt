package com.example.breathe.data.local.dao

import com.example.breathe.data.models.MeditationSession
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DayTotal(
    val day: String,
    val totalDuration: Long
)

@Dao
interface MeditationDao {
    @Insert
    suspend fun insertSession(session: MeditationSession): Long   // returns auto-generated row id

    @Query("UPDATE meditation_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE meditation_sessions SET remoteId = :remoteId, isSynced = 1 WHERE id = :id")
    suspend fun updateRemoteId(id: Long, remoteId: String)

    @Query("SELECT * FROM meditation_sessions WHERE isSynced = 0 ORDER BY date ASC")
    suspend fun getUnsyncedSessions(): List<MeditationSession>

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    // ← new:
    @Query("""
    SELECT
      date(date / 1000, 'unixepoch') AS day,
      SUM(duration)            AS totalDuration
    FROM meditation_sessions
    GROUP BY day
    ORDER BY day DESC
  """)
    fun getTotalsByDay(): Flow<List<DayTotal>>
}
