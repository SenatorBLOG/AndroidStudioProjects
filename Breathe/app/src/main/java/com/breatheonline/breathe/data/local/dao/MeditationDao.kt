package com.breatheonline.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breatheonline.breathe.data.models.MeditationSession
import kotlinx.coroutines.flow.Flow

data class DayTotal(val date: String, val totalDuration: Int)

@Dao
interface MeditationDao {
    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Query("""
        SELECT date(date/1000, 'unixepoch', 'localtime') as date, SUM(duration) as totalDuration 
        FROM meditation_sessions 
        GROUP BY date
    """)
    fun getTotalsByDay(): Flow<List<DayTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession): Long

    @Query("UPDATE meditation_sessions SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE meditation_sessions SET remoteId = :remoteId WHERE id = :id")
    suspend fun updateRemoteId(id: Long, remoteId: String)

    @Query("SELECT * FROM meditation_sessions WHERE synced = 0")
    suspend fun getUnsyncedSessions(): List<MeditationSession>
}
