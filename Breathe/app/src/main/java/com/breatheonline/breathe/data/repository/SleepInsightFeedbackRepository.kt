package com.breatheonline.breathe.data.repository

import com.breatheonline.breathe.data.local.dao.SleepInsightFeedbackDao
import com.breatheonline.breathe.data.models.SleepInsightFeedback
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SleepInsightFeedbackRepository @Inject constructor(
    private val dao: SleepInsightFeedbackDao,
) {
    fun observeFeedback(insightKey: String): Flow<SleepInsightFeedback?> =
        dao.observeFeedback(insightKey)

    suspend fun upsert(feedback: SleepInsightFeedback) =
        dao.upsert(feedback)
}
