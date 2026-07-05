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

    @Test fun `regularity is UNKNOWN when too few samples`() {
        assertEquals(com.breatheonline.breathe.utils.Regularity.UNKNOWN,
            com.breatheonline.breathe.utils.regularityOf(listOf(1380, 1390)))
    }

    @Test fun `regularity is REGULAR when stdev below 45 min`() {
        assertEquals(com.breatheonline.breathe.utils.Regularity.REGULAR,
            com.breatheonline.breathe.utils.regularityOf(listOf(1380, 1385, 1395, 1400, 1405)))
    }

    @Test fun `regularity is IRREGULAR when stdev above 45 min`() {
        assertEquals(com.breatheonline.breathe.utils.Regularity.IRREGULAR,
            com.breatheonline.breathe.utils.regularityOf(listOf(1380, 1260, 120, 1200, 1400)))
    }

    @Test fun `isoTimestamp to clock minutes`() {
        val mins = com.breatheonline.breathe.utils.clockMinutesOrNull("2026-04-16T22:35:00Z")
        assertEquals(true, mins != null)
    }

    @Test fun `buildSleepDayView handles missing stages`() {
        val dto = com.breatheonline.breathe.data.models.SleepDayDto(
            date = "2026-04-16",
            duration = 480,
            deepSleepMin = 96, remSleepMin = 120, lightSleepMin = 240, awakeMin = 24,
        )
        val view = com.breatheonline.breathe.utils.buildSleepDayView(dto, history7d = listOf(dto), avgSleepingHrBpm = 54)
        assertEquals(480, view.durationMin)
        assertEquals(true, view.stages.isEmpty())
        assertEquals("Excellent", view.qualityLabel)
    }

    @Test fun `insight triggers short sleep rule`() {
        val view = com.breatheonline.breathe.viewmodel.SleepDayView(
            date = java.time.LocalDate.of(2026, 4, 16),
            durationMin = 330, bedtime = "01:30", wakeTime = "07:00",
            stages = emptyList(),
            stageTotals = com.breatheonline.breathe.viewmodel.StageTotals(50, 180, 50, 50),
            score = 45, qualityLabel = "Fair", avgSleepingHrBpm = null, deltaVsAvg7dMin = null,
            nightMetrics = emptyList(),
        )
        val msg = com.breatheonline.breathe.utils.buildLocalInsight(view, com.breatheonline.breathe.utils.Regularity.REGULAR)
        assertEquals(true, msg.contains("6 hour") || msg.contains("6 час"))
    }
}
