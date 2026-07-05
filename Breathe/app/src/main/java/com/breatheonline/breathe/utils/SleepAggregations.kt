package com.breatheonline.breathe.utils

import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.data.models.SleepStage
import com.breatheonline.breathe.data.models.SleepStageSegment
import com.breatheonline.breathe.viewmodel.DayClockPoint
import com.breatheonline.breathe.viewmodel.DayScorePoint
import com.breatheonline.breathe.viewmodel.DayStageStack
import com.breatheonline.breathe.viewmodel.MetricTone
import com.breatheonline.breathe.viewmodel.NightMetric
import com.breatheonline.breathe.viewmodel.SleepDayView
import com.breatheonline.breathe.viewmodel.SleepMonthView
import com.breatheonline.breathe.viewmodel.SleepScheduleAggregate
import com.breatheonline.breathe.viewmodel.SleepWeekView
import com.breatheonline.breathe.viewmodel.StageTotals
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.math.min
import kotlin.math.sqrt

fun computeSleepScore(
    durationMin: Int,
    deepMin: Int,
    remMin: Int,
    awakeMin: Int,
): Int {
    if (durationMin <= 0) return 0
    val duration = min(durationMin.toFloat() / 480f, 1f) * 60f
    val deepRatio = deepMin.toFloat() / durationMin
    val remRatio = remMin.toFloat() / durationMin
    val deep = min(deepRatio / 0.20f, 1f) * 20f
    val rem = min(remRatio / 0.25f, 1f) * 15f
    val awake = (1f - min(awakeMin.toFloat() / 60f, 1f)) * 5f
    return (duration + deep + rem + awake).toInt().coerceIn(0, 100)
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
    return if (sqrt(variance) <= thresholdMin) Regularity.REGULAR else Regularity.IRREGULAR
}

fun clockMinutesOrNull(iso: String?, zoneId: ZoneId = ZoneId.systemDefault()): Int? {
    if (iso == null) return null
    return runCatching {
        val time = Instant.parse(iso).atZone(zoneId).toLocalTime()
        time.hour * 60 + time.minute
    }.getOrNull()
}

private fun formatClock(minutes: Int?): String =
    if (minutes == null) "--:--" else "%02d:%02d".format(minutes / 60, minutes % 60)

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
    val avg7d = history7d.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: dto.duration
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
        deltaVsAvg7dMin = dto.duration - avg7d,
        nightMetrics = buildNightMetrics(
            durationMin = dto.duration,
            deepMin = deep,
            lightMin = light,
            remMin = rem,
            awakeMin = awake,
            score = score,
            avgSleepingHrBpm = avgSleepingHrBpm,
            stages = dto.stages.orEmpty(),
        ),
    )
}

private fun buildNightMetrics(
    durationMin: Int,
    deepMin: Int,
    lightMin: Int,
    remMin: Int,
    awakeMin: Int,
    score: Int,
    avgSleepingHrBpm: Int?,
    stages: List<SleepStageSegment>,
): List<NightMetric> {
    val targetMin = 8 * 60
    val totalInBed = (durationMin + awakeMin).coerceAtLeast(1)
    val efficiency = ((durationMin.toFloat() / totalInBed) * 100).toInt().coerceIn(0, 100)
    val deepPct = if (durationMin > 0) ((deepMin.toFloat() / durationMin) * 100).toInt() else 0
    val remPct = if (durationMin > 0) ((remMin.toFloat() / durationMin) * 100).toInt() else 0
    val awakeSegments = stages.count { it.stage == SleepStage.AWAKE }
    val continuityLabel = when {
        awakeMin >= 75 || awakeSegments >= 4 -> "Fragmented"
        awakeMin >= 35 || awakeSegments >= 2 -> "Interrupted"
        durationMin == 0 -> "No data"
        else -> "Stable"
    }
    val continuityTone = when (continuityLabel) {
        "Stable" -> MetricTone.POSITIVE
        "Interrupted" -> MetricTone.NEUTRAL
        "Fragmented" -> MetricTone.CAUTION
        else -> MetricTone.MUTED
    }
    val durationDelta = durationMin - targetMin
    val durationValue = when {
        durationMin <= 0 -> "--"
        durationDelta >= 0 -> "+${durationDelta}m"
        else -> "${durationDelta}m"
    }
    val durationStatus = when {
        durationMin >= 450 -> "On target"
        durationMin >= 390 -> "Slightly short"
        durationMin > 0 -> "Sleep debt"
        else -> "No data"
    }
    val durationTone = when {
        durationMin >= 450 -> MetricTone.POSITIVE
        durationMin >= 390 -> MetricTone.NEUTRAL
        durationMin > 0 -> MetricTone.CAUTION
        else -> MetricTone.MUTED
    }
    val recoveryTone = when {
        score >= 80 -> MetricTone.POSITIVE
        score >= 60 -> MetricTone.NEUTRAL
        else -> MetricTone.CAUTION
    }
    val deepTone = when {
        deepPct >= 18 -> MetricTone.POSITIVE
        deepPct >= 12 -> MetricTone.NEUTRAL
        else -> MetricTone.CAUTION
    }
    val remTone = when {
        remPct in 20..30 -> MetricTone.POSITIVE
        remPct in 16..34 -> MetricTone.NEUTRAL
        else -> MetricTone.CAUTION
    }
    val hrTone = when {
        avgSleepingHrBpm == null -> MetricTone.MUTED
        avgSleepingHrBpm <= 56 -> MetricTone.POSITIVE
        avgSleepingHrBpm <= 65 -> MetricTone.NEUTRAL
        else -> MetricTone.CAUTION
    }

    return listOf(
        NightMetric(
            label = "Recovery",
            value = "$score/100",
            status = qualityLabelFor(score),
            supporting = "Blends duration, deep sleep, REM share, and awake time into one nightly recovery signal.",
            tone = recoveryTone,
        ),
        NightMetric(
            label = "Sleep efficiency",
            value = "$efficiency%",
            status = if (efficiency >= 92) "Efficient" else if (efficiency >= 85) "Okay" else "Low",
            supporting = "Estimated from time asleep vs awake-in-bed. Lower values usually mean a more broken night.",
            tone = when {
                efficiency >= 92 -> MetricTone.POSITIVE
                efficiency >= 85 -> MetricTone.NEUTRAL
                else -> MetricTone.CAUTION
            },
        ),
        NightMetric(
            label = "Duration vs target",
            value = durationValue,
            status = durationStatus,
            supporting = "Compared with an 8-hour target so you can see whether the night paid down or added sleep debt.",
            tone = durationTone,
        ),
        NightMetric(
            label = "Continuity",
            value = continuityLabel,
            status = if (awakeSegments > 0) "$awakeSegments awake segments" else "$awakeMin min awake",
            supporting = "Derived from awake minutes and visible awake segments. Stable nights usually improve recovery quality.",
            tone = continuityTone,
        ),
        NightMetric(
            label = "Deep sleep",
            value = "$deepPct%",
            status = if (deepMin > 0) formatStageMinutes(deepMin) else "No stage data",
            supporting = "Deep sleep is the most physically restorative phase. Around 15-25% is a strong range for many nights.",
            tone = deepTone,
        ),
        NightMetric(
            label = "REM balance",
            value = "$remPct%",
            status = if (remMin > 0) formatStageMinutes(remMin) else "No stage data",
            supporting = "REM supports mental recovery and memory processing. Too little REM can follow short or irregular nights.",
            tone = remTone,
        ),
        NightMetric(
            label = "Sleeping HR",
            value = avgSleepingHrBpm?.let { "$it bpm" } ?: "--",
            status = when {
                avgSleepingHrBpm == null -> "Unavailable"
                avgSleepingHrBpm <= 56 -> "Calm"
                avgSleepingHrBpm <= 65 -> "Elevated"
                else -> "High"
            },
            supporting = if (avgSleepingHrBpm == null) {
                "Heart-rate data was not recorded for this night."
            } else {
                "A lower sleeping heart rate often points to calmer recovery, assuming the value is typical for you."
            },
            tone = hrTone,
        ),
    )
}

private fun formatStageMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours == 0) "${mins}m" else if (mins == 0) "${hours}h" else "${hours}h ${mins}m"
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
            score = dto.score ?: computeSleepScore(
                durationMin = dto.duration,
                deepMin = dto.deepSleepMin ?: 0,
                remMin = dto.remSleepMin ?: 0,
                awakeMin = dto.awakeMin ?: 0,
            ),
        )
    }
    val scoreAvg = points.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    val stageStacks = days.map { dto ->
        DayStageStack(
            date = parseHealthDate(dto.date) ?: LocalDate.now(),
            deepMin = dto.deepSleepMin ?: 0,
            lightMin = dto.lightSleepMin ?: 0,
            remMin = dto.remSleepMin ?: 0,
            awakeMin = dto.awakeMin ?: 0,
        )
    }
    val idealAvg = days.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    return SleepWeekView(
        rangeLabel = rangeLabel,
        scorePoints = points,
        scoreAvg = scoreAvg,
        scoreDeltaVsPrevWeek = prevWeekAvgScore?.let { scoreAvg - it },
        schedule = buildSchedule(days),
        idealDuration = stageStacks,
        idealAvgMin = idealAvg,
        idealDeltaVsPrevWeekMin = prevWeekAvgDurationMin?.let { idealAvg - it },
    )
}

fun buildSleepMonthView(
    days: List<SleepDayDto>,
    baselineScore: Int?,
    prevMonthAvgDurationMin: Int?,
    prevMonthAvgScore: Int?,
    monthLabel: String,
): SleepMonthView {
    if (days.isEmpty()) {
        return SleepMonthView(
            monthLabel = monthLabel,
            scorePoints = emptyList(),
            scoreAvg = 0,
            scoreBaseline = baselineScore,
            scoreDeltaVsPrevMonth = prevMonthAvgScore?.let { -it },
            schedule = SleepScheduleAggregate(
                avgFallAsleepMinOfDay = null,
                latestFallAsleepMinOfDay = null,
                avgWakeMinOfDay = null,
                earliestWakeMinOfDay = null,
                fallAsleepSeries = emptyList(),
                wakeSeries = emptyList(),
                fallAsleepRegularity = Regularity.UNKNOWN,
                wakeRegularity = Regularity.UNKNOWN,
            ),
            idealDuration = emptyList(),
            idealAvgMin = 0,
            idealDeltaVsPrevMonthMin = prevMonthAvgDurationMin?.let { -it },
        )
    }

    val datedDays = days.mapNotNull { dto -> parseHealthDate(dto.date)?.let { it to dto } }
    val monthStart = datedDays.minOf { it.first }.withDayOfMonth(1)
    val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
    val bucketStarts = listOf(1, 8, 15, 22, 29)

    val buckets = bucketStarts.map { startDay ->
        val date = monthStart.withDayOfMonth(startDay.coerceAtMost(monthEnd.dayOfMonth))
        val bucketDays = datedDays.filter { (dateValue, _) ->
            dateValue.dayOfMonth in startDay..min(startDay + 6, monthEnd.dayOfMonth)
        }.map { it.second }
        date to bucketDays
    }

    val scorePoints = buckets.map { (date, bucketDays) ->
        DayScorePoint(
            date = date,
            score = bucketDays.map {
                it.score ?: computeSleepScore(
                    durationMin = it.duration,
                    deepMin = it.deepSleepMin ?: 0,
                    remMin = it.remSleepMin ?: 0,
                    awakeMin = it.awakeMin ?: 0,
                )
            }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        )
    }
    val scoreAvg = scorePoints.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

    fun bucketAverage(bucketDays: List<SleepDayDto>, selector: (SleepDayDto) -> Int?): Int =
        bucketDays.mapNotNull(selector).takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

    val idealDuration = buckets.map { (date, bucketDays) ->
        DayStageStack(
            date = date,
            deepMin = bucketAverage(bucketDays) { it.deepSleepMin },
            lightMin = bucketAverage(bucketDays) { it.lightSleepMin },
            remMin = bucketAverage(bucketDays) { it.remSleepMin },
            awakeMin = bucketAverage(bucketDays) { it.awakeMin },
        )
    }

    val allBedtimes = datedDays.mapNotNull { clockMinutesOrNull(it.second.bedtime) }
    val allWakeTimes = datedDays.mapNotNull { clockMinutesOrNull(it.second.wakeTime) }

    val schedule = SleepScheduleAggregate(
        avgFallAsleepMinOfDay = allBedtimes.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        latestFallAsleepMinOfDay = allBedtimes.maxOrNull(),
        avgWakeMinOfDay = allWakeTimes.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        earliestWakeMinOfDay = allWakeTimes.minOrNull(),
        fallAsleepSeries = buckets.map { (date, bucketDays) ->
            DayClockPoint(
                date = date,
                minOfDay = bucketDays.mapNotNull { clockMinutesOrNull(it.bedtime) }
                    .takeIf { it.isNotEmpty() }?.average()?.toInt(),
            )
        },
        wakeSeries = buckets.map { (date, bucketDays) ->
            DayClockPoint(
                date = date,
                minOfDay = bucketDays.mapNotNull { clockMinutesOrNull(it.wakeTime) }
                    .takeIf { it.isNotEmpty() }?.average()?.toInt(),
            )
        },
        fallAsleepRegularity = regularityOf(allBedtimes),
        wakeRegularity = regularityOf(allWakeTimes),
    )

    val idealAvg = days.map { it.duration }.average().toInt()
    return SleepMonthView(
        monthLabel = monthLabel,
        scorePoints = scorePoints,
        scoreAvg = scoreAvg,
        scoreBaseline = baselineScore,
        scoreDeltaVsPrevMonth = prevMonthAvgScore?.let { scoreAvg - it },
        schedule = schedule,
        idealDuration = idealDuration,
        idealAvgMin = idealAvg,
        idealDeltaVsPrevMonthMin = prevMonthAvgDurationMin?.let { idealAvg - it },
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
