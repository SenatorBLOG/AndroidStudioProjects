package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent
import com.breatheonline.breathe.utils.Regularity
import com.breatheonline.breathe.viewmodel.DayClockPoint
import com.breatheonline.breathe.viewmodel.SleepScheduleAggregate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun SleepScheduleSection(
    schedule: SleepScheduleAggregate,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    SleepSectionCard(
        icon = Icons.Filled.Bedtime,
        title = stringResource(R.string.sleep_schedule_title),
        colors = colors,
        modifier = modifier,
    ) {
        Spacer(Modifier.height(16.dp))
        ScheduleChartBlock(
            value = formatMinutes(schedule.avgFallAsleepMinOfDay),
            valueLabel = stringResource(R.string.sleep_avg_fall_asleep_label),
            regularity = regularityLabel(schedule.fallAsleepRegularity),
            regularityLabel = stringResource(R.string.sleep_sleep_regularity_label),
            bannerText = schedule.latestFallAsleepMinOfDay?.let { stringResource(R.string.sleep_latest_fall_asleep_time, formatMinutes(it)) },
            points = schedule.fallAsleepSeries,
            yMinMinutes = 20 * 60,
            yMaxMinutes = 26 * 60,
            colors = colors,
        )
        Spacer(Modifier.height(18.dp))
        ScheduleChartBlock(
            value = formatMinutes(schedule.avgWakeMinOfDay),
            valueLabel = stringResource(R.string.sleep_avg_wake_up_label),
            regularity = regularityLabel(schedule.wakeRegularity),
            regularityLabel = stringResource(R.string.sleep_wake_up_regularity_label),
            bannerText = schedule.earliestWakeMinOfDay?.let { stringResource(R.string.sleep_earliest_wake_up_time, formatMinutes(it)) },
            points = schedule.wakeSeries,
            yMinMinutes = 5 * 60,
            yMaxMinutes = 10 * 60,
            colors = colors,
        )
    }
}

@Composable
private fun ScheduleChartBlock(
    value: String,
    valueLabel: String,
    regularity: String,
    regularityLabel: String,
    bannerText: String?,
    points: List<DayClockPoint>,
    yMinMinutes: Int,
    yMaxMinutes: Int,
    colors: AppColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                color = colors.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(text = valueLabel, color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = regularity,
                color = colors.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(text = regularityLabel, color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (bannerText != null) {
        Spacer(Modifier.height(10.dp))
        SleepDeltaBanner(text = bannerText, colors = colors)
    }

    Spacer(Modifier.height(12.dp))
    ChartShell {
        Canvas(modifier = Modifier.fillMaxWidth().height(155.dp)) {
            repeat(5) { step ->
                val y = size.height * step / 4f
                drawLine(
                    color = colors.subtitle.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f), 0f),
                )
            }

            val validPoints = points.withIndex().filter { it.value.minOfDay != null }
            if (validPoints.size >= 2) {
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                val range = (yMaxMinutes - yMinMinutes).toFloat().coerceAtLeast(1f)
                var previous: Offset? = null
                validPoints.forEach { (index, point) ->
                    val x = stepX * index
                    val normalized = ((point.minOfDay!! - yMinMinutes).coerceIn(0, yMaxMinutes - yMinMinutes)) / range
                    val y = size.height * (1f - normalized)
                    val current = Offset(x, y)
                    previous?.let {
                        drawLine(color = SleepAccent, start = it, end = current, strokeWidth = 4f)
                    }
                    drawCircle(color = SleepAccent, radius = 6f, center = current)
                    drawCircle(color = colors.surface, radius = 2.5f, center = current)
                    previous = current
                }
            }
        }
        if (points.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                points.forEach { point ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (points.size == 7) point.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            else "${point.date.monthValue.toString().padStart(2, '0')}/${point.date.dayOfMonth.toString().padStart(2, '0')}",
                            color = colors.subtitle,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartShell(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        content = content,
    )
}

private fun formatMinutes(minutes: Int?): String =
    if (minutes == null) "--:--" else "%02d:%02d".format((minutes / 60) % 24, minutes % 60)

@Composable
private fun regularityLabel(regularity: Regularity): String = when (regularity) {
    Regularity.REGULAR -> stringResource(R.string.sleep_regular)
    Regularity.IRREGULAR -> stringResource(R.string.sleep_irregular)
    Regularity.UNKNOWN -> stringResource(R.string.sleep_unknown)
}
