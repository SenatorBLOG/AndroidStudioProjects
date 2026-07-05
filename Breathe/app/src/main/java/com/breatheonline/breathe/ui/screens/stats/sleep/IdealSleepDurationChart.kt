package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAwake
import com.breatheonline.breathe.ui.theme.SleepDeep
import com.breatheonline.breathe.ui.theme.SleepLight
import com.breatheonline.breathe.ui.theme.SleepRem
import com.breatheonline.breathe.viewmodel.DayStageStack

@Composable
internal fun IdealSleepDurationChart(
    stacks: List<DayStageStack>,
    avgLabel: String,
    deltaLine: String?,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    SleepSectionCard(
        icon = Icons.Filled.Hotel,
        title = "Ideal sleep duration",
        colors = colors,
        modifier = modifier,
    ) {
        Spacer(Modifier.height(12.dp))
        Text(text = "Average", color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
        Text(
            text = avgLabel,
            color = colors.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (deltaLine != null) {
            Spacer(Modifier.height(10.dp))
            SleepDeltaBanner(text = deltaLine, colors = colors)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StageLegend("Deep", SleepDeep, colors)
            StageLegend("Light", SleepLight, colors)
            StageLegend("REM", SleepRem, colors)
            StageLegend("Awake", SleepAwake, colors)
        }
        Spacer(Modifier.height(14.dp))
        if (stacks.any { it.deepMin + it.lightMin + it.remMin + it.awakeMin > 0 }) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                repeat(6) { step ->
                    val y = size.height * step / 5f
                    drawLine(
                        color = colors.subtitle.copy(alpha = 0.12f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f), 0f),
                    )
                }
                if (stacks.isNotEmpty()) {
                    val barWidth = (size.width / stacks.size) * 0.42f
                    val maxMinutes = (stacks.maxOfOrNull { it.deepMin + it.lightMin + it.remMin + it.awakeMin } ?: 1).toFloat()
                    stacks.forEachIndexed { index, stack ->
                        val x = size.width * ((index + 0.5f) / stacks.size) - barWidth / 2f
                        var yCursor = size.height

                        fun segment(minutes: Int, color: Color, rounded: Boolean = false) {
                            if (minutes <= 0) return
                            val height = size.height * (minutes / maxMinutes)
                            val top = yCursor - height
                            if (rounded) {
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(x, top),
                                    size = Size(barWidth, height),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                                )
                            } else {
                                drawRect(
                                    color = color,
                                    topLeft = Offset(x, top),
                                    size = Size(barWidth, height),
                                )
                            }
                            yCursor = top
                        }

                        val segments = listOf(
                            stack.deepMin to SleepDeep,
                            stack.lightMin to SleepLight,
                            stack.remMin to SleepRem,
                            stack.awakeMin to SleepAwake,
                        ).filter { it.first > 0 }

                        segments.forEachIndexed { segmentIndex, (minutes, color) ->
                            segment(minutes, color, rounded = segmentIndex == segments.lastIndex)
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Stage totals for this period are still too sparse to estimate an ideal duration pattern.",
                color = colors.subtitle,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (stacks.any { it.deepMin + it.lightMin + it.remMin + it.awakeMin > 0 }) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                stacks.forEach { stack ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${stack.date.monthValue.toString().padStart(2, '0')}/${stack.date.dayOfMonth.toString().padStart(2, '0')}",
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
private fun StageLegend(label: String, color: Color, colors: AppColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = colors.subtitle, fontSize = 12.sp)
    }
}
