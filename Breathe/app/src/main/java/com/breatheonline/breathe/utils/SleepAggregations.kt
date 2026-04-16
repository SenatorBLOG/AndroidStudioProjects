package com.breatheonline.breathe.utils

import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.viewmodel.DayClockPoint
import com.breatheonline.breathe.viewmodel.DayScorePoint
import com.breatheonline.breathe.viewmodel.DayStageStack
import com.breatheonline.breathe.viewmodel.SleepDayView
import com.breatheonline.breathe.viewmodel.SleepMonthView
import com.breatheonline.breathe.viewmodel.SleepScheduleAggregate
import com.breatheonline.breathe.viewmodel.SleepWeekView
import com.breatheonline.breathe.viewmodel.StageTotals
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Score components (weighted out of 100):
 *  - Duration (60): hits max at 480 min (8h), scales linearly below.
 *  - Deep ratio (20): hits max when deep ≥ 20% of duration.
 *  - REM ratio (15): hits max when rem ≥ 25% of duration.
 *  - Awake penalty (5): full credit at 0 awake; zero at ≥ 60 min awake.
 */
fun computeSleepScore(
    durationMin: Int,
    deepMin:     Int,
    remMin:      Int,
    awakeMin:    Int,
): Int {
    if (durationMin <= 0) return 0
    val duration = min(durationMin.toFloat() / 480f, 1f) * 60f
    val deepRatio = deepMin.toFloat() / durationMin
    val remRatio = remMin.toFloat() / durationMin
    val deep = min(deepRatio / 0.20f, 1f) * 20f
    val rem = min(remRatio / 0.25f, 1f) * 15f
    val awake = (1f - min(awakeMin.toFloat() / 60f, 1f)) * 5f
    val total = (duration + deep + rem + awake).toInt()
    return total.coerceIn(0, 100)
}

fun qualityLabelFor(score: Int): String = when {
    score < 40 -> "Poor"
    score < 60 -> "Fair"
    score < 80 -> "Good"
    else -> "Excellent"
}

enum class Regularity { REGULAR, IRREGULAR, UNKNOWN }

fun regularityOf(minutes: List<Int>, thresholdMin: Double = 45.0): Regularity {
    if (minutes.size < 5) return Regularity.UNKNOWN
    val mean = minutes.average()
    val variance = minutes.map { (it - mean) * (it - mean) }.average()
    val stdev = sqrt(variance)
    return if (stdev <= thresholdMin) Regularity.REGULAR else Regularity.IRREGULAR
}

fun clockMinutesOrNull(iso: String?, zoneId: ZoneId = ZoneId.systemDefault()): Int? {
    if (iso == null) return null
    return runCatching {
        val t = Instant.parse(iso).atZone(zoneId).toLocalTime()
        t.hour * 60 + t.minute
    }.getOrNull()
}

private fun formatClock(min: Int?): String =
    if (min == null) "--:--" else "%02d:%02d".format(min / 60, min % 60)

fun buildSleepDayView(
    dto: SleepDayDto,
    history7d: List<SleepDayDto>,
    avgSleepingHrBpm: Int?,
): SleepDayView {
    val deep = dto.deepSleepMin ?: 0
    val light = dto.lightSleepMin ?: 0
    val rem = dto.remSleepMin ?: 0
    val awake = dto.awakeMin ?: 0
    val score = dto.score ?: computeSleepScore(dto.duration, deep, rem, awake)
    val avg7d = if (history7d.isEmpty()) dto.duration else history7d.map { it.duration }.average().toInt()
    val delta = dto.duration - avg7d
    return SleepDayView(
        date = parseHealthDate(dto.date) ?: LocalDate.now(),
        durationMin = dto.duration,
        bedtime = formatClock(clockMinutesOrNull(dto.bedtime)),
        wakeTime = formatClock(clockMinutesOrNull(dto.wakeTime)),
        stages = dto.stages.orEmpty(),
        stageTotals = StageTotals(deep, light, rem, awake),
        score = score,
        qualityLabel = qualityLabelFor(score),
        avgSleepingHrBpm = avgSleepingHrBpm,
        deltaVsAvg7dMin = delta,
    )
}

fun buildSleepWeekView(
    days: List<SleepDayDto>,
    prevWeekAvgDurationMin: Int?,
    prevWeekAvgScore: Int?,
    rangeLabel: String,
): SleepWeekView {
    val points = days.map { dto ->
        DayScorePoint(
            date = parseHealthDate(dto.date) ?: LocalDate.now(),
            score = dto.score ?: computeSleepScore(dto.duration, dto.deepSleepMin ?: 0, dto.remSleepMin ?: 0, dto.awakeMin ?: 0),
        )
    }
    val avg = points.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    val scoreDelta = prevWeekAvgScore?.let { avg - it }
    val stacks = days.map { dto ->
        DayStageStack(
            date = parseHealthDate(dto.date) ?: LocalDate.now(),
            deepMin = dto.deepSleepMin ?: 0,
            lightMin = dto.lightSleepMin ?: 0,
            remMin = dto.remSleepMin ?: 0,
            awakeMin = dto.awakeMin ?: 0,
        )
    }
    val idealAvg = days.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    val idealDelta = prevWeekAvgDurationMin?.let { idealAvg - it }
    val schedule = buildSchedule(days)
    return SleepWeekView(rangeLabel, points, avg, scoreDelta, schedule, stacks, idealAvg, idealDelta)
}

fun buildSleepMonthView(
    days: List<SleepDayDto>,
    baselineScore: Int?,
    prevMonthAvgDurationMin: Int?,
    prevMonthAvgScore: Int?,
    monthLabel: String,
): SleepMonthView {
    val weekView = buildSleepWeekView(days, prevMonthAvgDurationMin, prevMonthAvgScore, monthLabel)
    return SleepMonthView(
        monthLabel = monthLabel,
        scorePoints = weekView.scorePoints,
        scoreAvg = weekView.scoreAvg,
        scoreBaseline = baselineScore,
        scoreDeltaVsPrevMonth = weekView.scoreDeltaVsPrevWeek,
        schedule = weekView.schedule,
        idealDuration = weekView.idealDuration,
        idealAvgMin = weekView.idealAvgMin,
        idealDeltaVsPrevMonthMin = weekView.idealDeltaVsPrevWeekMin,
    )
}

private fun buildSchedule(days: List<SleepDayDto>): SleepScheduleAggregate {
    val fallSeries = days.map { DayClockPoint(parseHealthDate(it.date) ?: LocalDate.now(), clockMinutesOrNull(it.bedtime)) }
    val wakeSeries = days.map { DayClockPoint(parseHealthDate(it.date) ?: LocalDate.now(), clockMinutesOrNull(it.wakeTime)) }
    val fallMinutes = fallSeries.mapNotNull { it.minOfDay }
    val wakeMinutes = wakeSeries.mapNotNull { it.minOfDay }
    return SleepScheduleAggregate(
        avgFallAsleepMinOfDay = fallMinutes.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        latestFallAsleepMinOfDay = fallMinutes.maxOrNull(),
        avgWakeMinOfDay = wakeMinutes.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        earliestWakeMinOfDay = wakeMinutes.minOrNull(),
        fallAsleepSeries = fallSeries,
        wakeSeries = wakeSeries,
        fallAsleepRegularity = regularityOf(fallMinutes),
        wakeRegularity = regularityOf(wakeMinutes),
    )
}
