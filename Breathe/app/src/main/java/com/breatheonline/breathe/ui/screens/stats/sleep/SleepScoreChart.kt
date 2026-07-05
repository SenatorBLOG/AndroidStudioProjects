package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent
import com.breatheonline.breathe.viewmodel.DayScorePoint

@Composable
internal fun SleepScoreChart(
    points: List<DayScorePoint>,
    avg: Int,
    deltaLine: String?,
    baseline: Int?,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    SleepSectionCard(
        icon = Icons.Filled.Shield,
        title = "Sleep score",
        colors = colors,
        modifier = modifier,
    ) {
        Spacer(Modifier.height(12.dp))
        Text(text = "Avg", style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
        Text(
            text = avg.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = colors.title,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = when {
                avg >= 80 -> "Excellent recovery"
                avg >= 60 -> "Good rhythm"
                avg >= 40 -> "Mixed sleep"
                else -> "Poor sleep quality"
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.subtitle,
        )
        if (deltaLine != null) {
            Spacer(Modifier.height(10.dp))
            SleepDeltaBanner(text = deltaLine, colors = colors)
        }
        if (baseline != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.height(2.dp).weight(0.18f)) {
                    drawLine(
                        color = SleepAccent.copy(alpha = 0.65f),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                    )
                }
                Text(
                    text = "Average for users your age",
                    color = colors.subtitle,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        if (points.any { it.score != null }) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
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

                baseline?.let {
                    val baselineY = size.height * (1f - it / 100f)
                    drawLine(
                        color = SleepAccent.copy(alpha = 0.5f),
                        start = Offset(0f, baselineY),
                        end = Offset(size.width, baselineY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                    )
                }

                val validPoints = points.withIndex().filter { it.value.score != null }
                if (validPoints.isNotEmpty()) {
                    val stepX = size.width / points.size.coerceAtLeast(1)
                    val barWidth = stepX * 0.44f
                    validPoints.forEach { (index, point) ->
                        val scoreValue = point.score ?: return@forEach
                        val x = stepX * index + stepX * 0.28f
                        val barHeight = size.height * (scoreValue / 100f).coerceIn(0.02f, 1f)
                        drawRoundRect(
                            color = SleepAccent,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Not enough scored nights yet to draw a trend for this period.",
                color = colors.subtitle,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (points.any { it.score != null }) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                points.forEach { point ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${point.date.monthValue.toString().padStart(2, '0')}/${point.date.dayOfMonth.toString().padStart(2, '0')}",
                            color = colors.subtitle,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}
