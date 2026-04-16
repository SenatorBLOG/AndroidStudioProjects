package com.breatheonline.breathe.utils

import kotlin.math.min

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
