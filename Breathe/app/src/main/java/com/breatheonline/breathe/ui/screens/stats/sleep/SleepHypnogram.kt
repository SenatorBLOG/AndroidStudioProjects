package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val total = stages.maxOfOrNull { it.endMin } ?: 480
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Row {
            LegendDot("Deep", SleepDeep, colors)
            Spacer(Modifier.width(12.dp))
            LegendDot("Light", SleepLight, colors)
            Spacer(Modifier.width(12.dp))
            LegendDot("REM", SleepRem, colors)
        }
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            val w = size.width
            val h = size.height
            val rows = mapOf(
                SleepStage.AWAKE to 0.05f,
                SleepStage.REM   to 0.30f,
                SleepStage.LIGHT to 0.55f,
                SleepStage.DEEP  to 0.80f,
            )
            val palette = mapOf(
                SleepStage.DEEP  to SleepDeep,
                SleepStage.LIGHT to SleepLight,
                SleepStage.REM   to SleepRem,
                SleepStage.AWAKE to SleepAwake,
            )
            stages.forEach { seg ->
                val x0 = w * (seg.startMin.toFloat() / total)
                val x1 = w * (seg.endMin.toFloat() / total)
                val yTop = h * (rows[seg.stage] ?: 0.5f)
                val barH = h * 0.12f
                drawRect(
                    color = palette[seg.stage] ?: Color.Gray,
                    topLeft = Offset(x0, yTop),
                    size = Size((x1 - x0).coerceAtLeast(2f), barH),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("$bedtime Fell asleep", color = colors.subtitle)
            Spacer(Modifier.weight(1f))
            Text("$wakeTime Woke up", color = colors.subtitle)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, colors: AppColors) {
    Row {
        Canvas(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(10.dp)
                .width(10.dp)
        ) {
            drawCircle(color, radius = size.minDimension / 2f)
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.title)
    }
}
