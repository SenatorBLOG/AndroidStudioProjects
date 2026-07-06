package com.breatheonline.breathe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.breatheonline.breathe.data.models.SleepInsightFeedback
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepInsightFeedbackDao {
    @Query("SELECT * FROM sleep_insight_feedback WHERE insightKey = :insightKey LIMIT 1")
    fun observeFeedback(insightKey: String): Flow<SleepInsightFeedback?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(feedback: SleepInsightFeedback)
}
