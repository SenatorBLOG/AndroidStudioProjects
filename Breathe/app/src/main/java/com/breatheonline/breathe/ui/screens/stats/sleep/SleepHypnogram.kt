package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.SleepStage
import com.breatheonline.breathe.data.models.SleepStageSegment
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAwake
import com.breatheonline.breathe.ui.theme.SleepDeep
import com.breatheonline.breathe.ui.theme.SleepLight
import com.breatheonline.breathe.ui.theme.SleepRem

@Composable
internal fun SleepHypnogram(
    stages: List<SleepStageSegment>,
    bedtime: String,
    wakeTime: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    val total = stages.maxOfOrNull { it.endMin }?.coerceAtLeast(1) ?: 1
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot("Deep", SleepDeep, colors)
            LegendDot("Light", SleepLight, colors)
            LegendDot("REM", SleepRem, colors)
            LegendDot("Awake", SleepAwake, colors)
        }
        Spacer(Modifier.height(14.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            val gridColor = colors.subtitle.copy(alpha = 0.18f)
            repeat(4) { index ->
                val y = size.height * (index / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                )
            }

            val rows = mapOf(
                SleepStage.AWAKE to 0.10f,
                SleepStage.REM to 0.30f,
                SleepStage.LIGHT to 0.52f,
                SleepStage.DEEP to 0.74f,
            )
            val segmentHeight = size.height * 0.17f

            stages.forEach { segment ->
                val startX = size.width * segment.startMin / total.toFloat()
                val endX = size.width * segment.endMin / total.toFloat()
                val width = (endX - startX).coerceAtLeast(4f)
                val top = size.height * (rows[segment.stage] ?: 0.5f)
                val color = when (segment.stage) {
                    SleepStage.DEEP -> SleepDeep
                    SleepStage.LIGHT -> SleepLight
                    SleepStage.REM -> SleepRem
                    SleepStage.AWAKE -> SleepAwake
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(startX, top),
                    size = Size(width, segmentHeight),
                    cornerRadius = CornerRadius(14f, 14f),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.sleep_fell_asleep, bedtime), color = colors.subtitle, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.sleep_woke_up, wakeTime), color = colors.subtitle, fontSize = 12.sp)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, colors: AppColors) {
    Row {
        Canvas(
            modifier = Modifier
                .padding(top = 5.dp)
                .height(10.dp)
                .width(10.dp),
        ) {
            drawCircle(color = color, radius = size.minDimension / 2f)
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.subtitle, fontSize = 12.sp)
    }
}
