package com.breatheonline.breathe.utils

import com.breatheonline.breathe.viewmodel.SleepDayView

fun buildLocalInsight(day: SleepDayView, wakeRegularity: Regularity): String {
    return when {
        day.durationMin < 360 ->
            "You slept less than 6 hours. Try moving bedtime earlier tonight."
        day.stageTotals.deepMin < (day.durationMin * 0.12).toInt() ->
            "Deep sleep was limited. Reduce screens and stimulation during the last hour before bed."
        wakeRegularity == Regularity.IRREGULAR ->
            "Your sleep schedule looks irregular. A more consistent wake-up time should help recovery."
        day.score >= 80 ->
            "Sleep quality looks strong. Keep the same sleep window and pre-bed routine."
        else ->
            "Sleep was decent overall. A slightly earlier wind-down could improve recovery."
    }
}
