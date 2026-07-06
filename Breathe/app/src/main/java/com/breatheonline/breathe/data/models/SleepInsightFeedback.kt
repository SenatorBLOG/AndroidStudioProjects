package com.breatheonline.breathe.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_insight_feedback")
data class SleepInsightFeedback(
    @PrimaryKey val insightKey: String,
    val insightText: String,
    val sentiment: Int,
    val createdAt: Long,
)
