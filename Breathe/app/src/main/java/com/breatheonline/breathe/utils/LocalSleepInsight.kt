package com.breatheonline.breathe.utils

import com.breatheonline.breathe.viewmodel.SleepDayView

fun buildLocalInsight(day: SleepDayView, wakeRegularity: Regularity): String {
    return when {
        day.durationMin < 360 ->
            "Ты спал меньше 6 часов — попробуй лечь пораньше сегодня."
        day.stageTotals.deepMin < (day.durationMin * 0.12).toInt() ->
            "Мало глубокого сна. Снизь экран за час до сна."
        wakeRegularity == Regularity.IRREGULAR ->
            "Сон нерегулярный — попробуй фиксированное время отбоя."
        day.score >= 80 ->
            "Отличный сон — удерживай текущий режим."
        else ->
            "Хороший сон. Продолжай в том же духе."
    }
}
