package com.breatheonline.breathe

import com.breatheonline.breathe.utils.computeSleepScore
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepAggregationsTest {
    @Test fun `score is 0 for zero duration`() {
        assertEquals(0, computeSleepScore(durationMin = 0, deepMin = 0, remMin = 0, awakeMin = 0))
    }

    @Test fun `score is near max for ideal night`() {
        val s = computeSleepScore(durationMin = 480, deepMin = 96, remMin = 120, awakeMin = 0)
        assertEquals(100, s)
    }

    @Test fun `score is penalised for short sleep`() {
        val s = computeSleepScore(durationMin = 240, deepMin = 48, remMin = 60, awakeMin = 0)
        assertEquals(70, s)
    }

    @Test fun `score is clamped at 100 for oversleep`() {
        val s = computeSleepScore(durationMin = 720, deepMin = 200, remMin = 200, awakeMin = 0)
        assertEquals(100, s)
    }
}
