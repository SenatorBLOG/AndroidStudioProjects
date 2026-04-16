package com.breatheonline.breathe.viewmodel

import com.breatheonline.breathe.data.models.SleepStageSegment
import com.breatheonline.breathe.utils.Regularity
import java.time.LocalDate

enum class SleepView { DAY, WEEK, MONTH }

data class StageTotals(
    val deepMin: Int, val lightMin: Int, val remMin: Int, val awakeMin: Int,
) { val totalMin get() = deepMin + lightMin + remMin + awakeMin }

data class SleepDayView(
    val date: LocalDate,
    val durationMin: Int,
    val bedtime: String,
    val wakeTime: String,
    val stages: List<SleepStageSegment>,
    val stageTotals: StageTotals,
    val score: Int,
    val qualityLabel: String,
    val avgSleepingHrBpm: Int?,
    val deltaVsAvg7dMin: Int?,
)

data class DayScorePoint(val date: LocalDate, val score: Int?)
data class DayStageStack(
    val date: LocalDate,
    val deepMin: Int, val lightMin: Int, val remMin: Int, val awakeMin: Int,
)
data class DayClockPoint(val date: LocalDate, val minOfDay: Int?)

data class SleepScheduleAggregate(
    val avgFallAsleepMinOfDay: Int?,
    val latestFallAsleepMinOfDay: Int?,
    val avgWakeMinOfDay: Int?,
    val earliestWakeMinOfDay: Int?,
    val fallAsleepSeries: List<DayClockPoint>,
    val wakeSeries: List<DayClockPoint>,
    val fallAsleepRegularity: Regularity,
    val wakeRegularity: Regularity,
)

data class SleepWeekView(
    val rangeLabel: String,
    val scorePoints: List<DayScorePoint>,
    val scoreAvg: Int,
    val scoreDeltaVsPrevWeek: Int?,
    val schedule: SleepScheduleAggregate,
    val idealDuration: List<DayStageStack>,
    val idealAvgMin: Int,
    val idealDeltaVsPrevWeekMin: Int?,
)

data class SleepMonthView(
    val monthLabel: String,
    val scorePoints: List<DayScorePoint>,
    val scoreAvg: Int,
    val scoreBaseline: Int?,
    val scoreDeltaVsPrevMonth: Int?,
    val schedule: SleepScheduleAggregate,
    val idealDuration: List<DayStageStack>,
    val idealAvgMin: Int,
    val idealDeltaVsPrevMonthMin: Int?,
)
