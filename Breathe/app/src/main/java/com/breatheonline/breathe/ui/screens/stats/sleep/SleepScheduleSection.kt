package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    SleepSectionCard(
        icon = Icons.Filled.Bedtime,
        title = "Sleep schedule",
        colors = colors,
        modifier = modifier,
    ) {
        Spacer(Modifier.height(16.dp))

        // Fall asleep subsection
        ScheduleSubSection(
            bigValue = formatMinutes(schedule.avgFallAsleepMinOfDay),
            bigLabel = "Avg fall asleep time",
            regularity = schedule.fallAsleepRegularity,
            regularityLabel = "Sleep regularity",
            bannerText = schedule.fallAsleepSeries.mapNotNull { it.minOfDay }.lastOrNull()
                ?.let { "Latest fall-asleep time: ${formatMinutes(it)}" },
            points = schedule.fallAsleepSeries,
            colors = colors,
            yMinMinutes = 20 * 60,
            yMaxMinutes = 26 * 60,
        )

        Spacer(Modifier.height(24.dp))

        // Wake-up subsection
        ScheduleSubSection(
            bigValue = formatMinutes(schedule.avgWakeMinOfDay),
            bigLabel = "Avg wake-up time",
            regularity = schedule.wakeRegularity,
            regularityLabel = "Wake-up time regularity",
            bannerText = schedule.wakeSeries.mapNotNull { it.minOfDay }.minOrNull()
                ?.let { "Earliest wake-up time: ${formatMinutes(it)}" },
            points = schedule.wakeSeries,
            colors = colors,
            yMinMinutes = 5 * 60,
            yMaxMinutes = 10 * 60,
        )
    }
}

@Composable
private fun ScheduleSubSection(
    bigValue: String,
    bigLabel: String,
    regularity: Regularity,
    regularityLabel: String,
    bannerText: String?,
    points: List<DayClockPoint>,
    colors: AppColors,
    yMinMinutes: Int,
    yMaxMinutes: Int,
) {
    // Two-column header: big time | Regularity label
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = bigValue,
                color = colors.title,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = bigLabel,
                color = colors.subtitle,
                fontSize = 12.sp,
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(
                text = regularity.label(),
                color = SleepAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = regularityLabel,
                color = colors.subtitle,
                fontSize = 12.sp,
            )
        }
    }

    if (bannerText != null) {
        Spacer(Modifier.height(10.dp))
        SleepDeltaBanner(text = bannerText, colors = colors)
    }

    Spacer(Modifier.height(12.dp))

    // Line chart
    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
        // Faint horizontal grid
        val gridSteps = 4
        repeat(gridSteps + 1) { step ->
            val y = size.height * (step.toFloat() / gridSteps)
            drawLine(
                color = colors.subtitle.copy(alpha = 0.12f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
            )
        }
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
            drawCircle(color = colors.surface, radius = 2.5f, center = cur) // inner dot hollow
            prev = cur
        }
    }

    // X-axis date labels
    if (points.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            points.forEach { p ->
                Box(Modifier.weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        text = "${p.date.monthValue}/${p.date.dayOfMonth}",
                        color = colors.subtitle,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

private fun formatMinutes(m: Int?): String =
    if (m == null) "--:--" else "%02d:%02d".format((m / 60) % 24, m % 60)

private fun Regularity.label(): String = when (this) {
    Regularity.REGULAR   -> "Regular"
    Regularity.IRREGULAR -> "Irregular"
    Regularity.UNKNOWN   -> "—"
}
