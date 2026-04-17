package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import com.breatheonline.breathe.ui.screens.stats.common.drawHorizontalGrid
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
        // Big avg number
        Text(
            text = "Avg: $avg",
            color = colors.title,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 46.sp,
        )
        if (deltaLine != null) {
            Spacer(Modifier.height(8.dp))
            SleepDeltaBanner(text = deltaLine, colors = colors)
        }
        if (baseline != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Short dashed indicator
                Canvas(modifier = Modifier.height(1.dp).weight(0.06f)) {
                    drawLine(
                        color = SleepAccent,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    )
                }
                Spacer(Modifier.weight(0.01f))
                Text(
                    "Average for users your age",
                    color = colors.subtitle,
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            drawHorizontalGrid(steps = 5, color = colors.subtitle.copy(alpha = 0.15f))
            baseline?.let { bl ->
                val y = size.height * (1f - bl / 100f)
                drawLine(
                    color = SleepAccent.copy(alpha = 0.55f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                )
            }
            if (points.isEmpty()) return@Canvas
            val barWidth = (size.width / points.size) * 0.45f
            points.forEachIndexed { i, p ->
                val score = p.score ?: return@forEachIndexed
                val xCenter = size.width * ((i + 0.5f) / points.size)
                val h = size.height * (score / 100f).coerceIn(0.02f, 1f)
                drawRoundRect(
                    color = SleepAccent,
                    topLeft = Offset(xCenter - barWidth / 2f, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }
        }
        // X-axis date labels
        if (points.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                points.forEach { p ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
}
