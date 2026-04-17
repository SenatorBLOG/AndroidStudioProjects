package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

        // Big avg number
        Text(
            text = "Avg: $avgLabel",
            color = colors.title,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 42.sp,
        )

        if (deltaLine != null) {
            Spacer(Modifier.height(8.dp))
            SleepDeltaBanner(text = deltaLine, colors = colors)
        }

        Spacer(Modifier.height(12.dp))

        // Stage legend
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StageLegendDot("Deep", SleepDeep, colors)
            StageLegendDot("Light", SleepLight, colors)
            StageLegendDot("REM", SleepRem, colors)
            StageLegendDot("Sleep", SleepAwake, colors)
        }

        Spacer(Modifier.height(12.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            // Faint dashed horizontal grid
            val gridSteps = 5
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

            if (stacks.isEmpty()) return@Canvas
            val maxMin = 600f
            val barW = (size.width / stacks.size) * 0.45f
            val cornerR = CornerRadius(barW / 2f)

            stacks.forEachIndexed { i, s ->
                val xCenter = size.width * ((i + 0.5f) / stacks.size)
                var yCursor = size.height

                // Find top-most non-zero segment
                val topSegIndex = listOf(s.awakeMin, s.remMin, s.lightMin, s.deepMin)
                    .indexOfFirst { it > 0 }

                fun drawSeg(mins: Int, color: Color, isTop: Boolean) {
                    if (mins <= 0) return
                    val h = (size.height * (mins / maxMin)).coerceAtMost(yCursor)
                    val top = yCursor - h
                    if (isTop) {
                        // Rounded top corners: draw full rounded rect, then square off bottom half
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(xCenter - barW / 2f, top),
                            size = Size(barW, h),
                            cornerRadius = cornerR,
                        )
                        // Square off bottom portion (only if h > cornerR.x * 2)
                        if (h > cornerR.x * 2) {
                            drawRect(
                                color = color,
                                topLeft = Offset(xCenter - barW / 2f, top + cornerR.x),
                                size = Size(barW, h - cornerR.x),
                            )
                        }
                    } else {
                        drawRect(
                            color = color,
                            topLeft = Offset(xCenter - barW / 2f, top),
                            size = Size(barW, h),
                        )
                    }
                    yCursor -= h
                }

                // Draw from bottom to top: deep, light, rem, awake
                // topSegIndex: 0=awake is top, 1=rem, 2=light, 3=deep
                // Since we draw bottom-up, awake is drawn LAST (topmost)
                drawSeg(s.deepMin,  SleepDeep,  topSegIndex == 3)
                drawSeg(s.lightMin, SleepLight, topSegIndex == 2)
                drawSeg(s.remMin,   SleepRem,   topSegIndex == 1)
                drawSeg(s.awakeMin, SleepAwake, topSegIndex == 0)
            }
        }

        // X-axis date labels
        if (stacks.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                stacks.forEach { s ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${s.date.monthValue}/${s.date.dayOfMonth}",
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
private fun StageLegendDot(label: String, color: Color, colors: AppColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            color = colors.subtitle,
            fontSize = 12.sp,
        )
    }
}
