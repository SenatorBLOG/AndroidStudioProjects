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
    suspend fun insertSession(session: MeditationSession)

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
