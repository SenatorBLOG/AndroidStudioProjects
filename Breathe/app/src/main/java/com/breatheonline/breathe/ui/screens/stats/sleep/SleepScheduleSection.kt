package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent
import com.breatheonline.breathe.utils.Regularity
import com.breatheonline.breathe.viewmodel.DayClockPoint
import com.breatheonline.breathe.viewmodel.SleepScheduleAggregate

@Composable
internal fun SleepScheduleSection(
    schedule: SleepScheduleAggregate,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ScheduleCard(
            title = "Avg fall asleep time",
            bigValue = formatMinutes(schedule.avgFallAsleepMinOfDay),
            regularity = schedule.fallAsleepRegularity,
            points = schedule.fallAsleepSeries,
            colors = colors,
            yMinMinutes = 20 * 60,
            yMaxMinutes = 26 * 60,
        )
        Spacer(Modifier.height(16.dp))
        ScheduleCard(
            title = "Avg wake-up time",
            bigValue = formatMinutes(schedule.avgWakeMinOfDay),
            regularity = schedule.wakeRegularity,
            points = schedule.wakeSeries,
            colors = colors,
            yMinMinutes = 5 * 60,
            yMaxMinutes = 10 * 60,
        )
    }
}

@Composable
private fun ScheduleCard(
    title: String,
    bigValue: String,
    regularity: Regularity,
    points: List<DayClockPoint>,
    colors: AppColors,
    yMinMinutes: Int,
    yMaxMinutes: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Text(text = bigValue, color = colors.title)
        Text(text = title, color = colors.subtitle)
        Text(text = regularity.label(), color = SleepAccent)
        Spacer(Modifier.height(12.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val validPoints = points.withIndex().filter { it.value.minOfDay != null }
            if (validPoints.size < 2) return@Canvas
            val span = (yMaxMinutes - yMinMinutes).toFloat()
            val step = size.width / (points.size - 1).coerceAtLeast(1)
            var prev: Offset? = null
            validPoints.forEach { (i, p) ->
                val x = step * i
                val y = size.height * (1f - ((p.minOfDay!! - yMinMinutes).coerceIn(0, yMaxMinutes - yMinMinutes)) / span)
                val cur = Offset(x, y)
                if (prev != null) {
                    drawLine(color = SleepAccent, start = prev!!, end = cur, strokeWidth = 3f)
                }
                drawCircle(color = SleepAccent, radius = 5f, center = cur)
                prev = cur
            }
        }
    }
}

private fun formatMinutes(m: Int?): String =
    if (m == null) "--:--" else "%02d:%02d".format((m / 60) % 24, m % 60)

private fun Regularity.label(): String = when (this) {
    Regularity.REGULAR -> "Regular"
    Regularity.IRREGULAR -> "Irregular"
    Regularity.UNKNOWN -> "—"
}
