package com.breatheonline.breathe.utils

import com.breatheonline.breathe.data.models.HrDayDto
import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.data.models.SleepStageSegment
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

fun parseHealthDate(raw: String, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate? {
    return runCatching { LocalDate.parse(raw) }.getOrNull()
        ?: runCatching { Instant.parse(raw).atZone(zoneId).toLocalDate() }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(zoneId).toLocalDate() }.getOrNull()
        ?: runCatching { ZonedDateTime.parse(raw).withZoneSameInstant(zoneId).toLocalDate() }.getOrNull()
}

fun formatHealthDate(raw: String, zoneId: ZoneId = ZoneId.systemDefault()): String {
    return parseHealthDate(raw, zoneId)?.toString() ?: raw
}

fun mergeSleepDays(days: List<SleepDayDto>, zoneId: ZoneId = ZoneId.systemDefault()): List<SleepDayDto> {
    return days
        .groupBy { formatHealthDate(it.date, zoneId) }
        .map { (date, items) ->
            SleepDayDto(
                date          = date,
                duration      = items.maxOf { it.duration },
                deepSleepMin  = items.mapNotNull { it.deepSleepMin }.takeIf { it.isNotEmpty() }?.sum(),
                remSleepMin   = items.mapNotNull { it.remSleepMin }.takeIf { it.isNotEmpty() }?.sum(),
                lightSleepMin = items.mapNotNull { it.lightSleepMin }.takeIf { it.isNotEmpty() }?.sum(),
                awakeMin      = items.mapNotNull { it.awakeMin }.takeIf { it.isNotEmpty() }?.sum(),
                bedtime       = items.mapNotNull { it.bedtime }.minOrNull(),
                wakeTime      = items.mapNotNull { it.wakeTime }.maxOrNull(),
                stages        = items.mapNotNull { it.stages }.maxByOrNull { it.size },
                score         = items.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            )
        }
        .sortedWith(compareBy({ parseHealthDate(it.date, zoneId) ?: LocalDate.MIN }, { it.date }))
}

fun mergeHeartRateDays(days: List<HrDayDto>, zoneId: ZoneId = ZoneId.systemDefault()): List<HrDayDto> {
    return days
        .groupBy { formatHealthDate(it.date, zoneId) }
        .map { (date, items) ->
            val avgSamples = items.mapNotNull { it.avgRate }
            val restingSamples = items.mapNotNull { it.restingRate }
            HrDayDto(
                date = date,
                restingRate = restingSamples.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
                avgRate = avgSamples.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
            )
        }
        .sortedWith(compareBy({ parseHealthDate(it.date, zoneId) ?: LocalDate.MIN }, { it.date }))
}
