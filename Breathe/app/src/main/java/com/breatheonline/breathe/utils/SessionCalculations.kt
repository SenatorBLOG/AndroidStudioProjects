package com.breatheonline.breathe.utils

import java.time.LocalDate

/**
 * Pure functions for meditation session calculations.
 * Extracted here so they can be unit-tested without Android dependencies.
 * ViewModels delegate to these instead of duplicating logic.
 */
object SessionCalculations {

    /**
     * Returns the length of the user's current streak ending on [today].
     * Counts consecutive days going backwards from today (or yesterday if today has no session).
     */
    fun computeCurrentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        val start = when {
            dates.contains(today)              -> today
            dates.contains(today.minusDays(1)) -> today.minusDays(1)
            else                               -> return 0
        }
        var count = 0
        var day   = start
        while (dates.contains(day)) { count++; day = day.minusDays(1) }
        return count
    }

    /**
     * Returns the longest consecutive-day streak ever recorded in [dates].
     */
    fun computeLongestStreak(dates: Set<LocalDate>): Int {
        val sorted = dates.sorted()
        if (sorted.isEmpty()) return 0
        var best = 0; var cur = 0; var prev: LocalDate? = null
        for (date in sorted) {
            cur  = if (prev == null || date == prev.plusDays(1)) cur + 1 else 1
            best = maxOf(best, cur)
            prev = date
        }
        return best
    }

    /**
     * Formats elapsed seconds as "m:ss" (e.g. 0:00, 5:03, 12:45).
     */
    fun formatElapsed(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    /**
     * Computes BMI from height in cm and weight in kg.
     * Returns null if either value is non-positive.
     */
    fun computeBmi(heightCm: Float, weightKg: Float): Float? {
        if (heightCm <= 0f || weightKg <= 0f) return null
        val h = heightCm / 100f
        return weightKg / (h * h)
    }

    /**
     * Returns a human-readable BMI category label.
     */
    fun bmiLabel(bmi: Float): String = when {
        bmi < 18.5f -> "Underweight"
        bmi < 25f   -> "Normal"
        bmi < 30f   -> "Overweight"
        else        -> "Obese"
    }
}
