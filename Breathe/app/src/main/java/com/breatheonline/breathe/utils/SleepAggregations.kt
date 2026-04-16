package com.breatheonline.breathe.utils

import java.time.Instant
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
