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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.screens.stats.common.drawHorizontalGrid
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Text("Ideal sleep duration", color = colors.title, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(text = "Avg: $avgLabel", color = colors.title)
        if (deltaLine != null) Text(text = deltaLine, color = colors.subtitle)
        Spacer(Modifier.height(12.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            drawHorizontalGrid(steps = 5, color = colors.subtitle.copy(alpha = 0.2f))
            if (stacks.isEmpty()) return@Canvas
            val maxMin = 600f
            val barW = size.width / (stacks.size * 2f)
            stacks.forEachIndexed { i, s ->
                val xCenter = size.width * ((i + 0.5f) / stacks.size)
                var yCursor = size.height
                fun drawSeg(mins: Int, color: Color) {
                    if (mins <= 0) return
                    val h = size.height * (mins / maxMin).coerceAtMost(1f)
                    drawRect(
                        color = color,
                        topLeft = Offset(xCenter - barW / 2f, yCursor - h),
                        size = Size(barW, h),
                    )
                    yCursor -= h
                }
                drawSeg(s.deepMin,  SleepDeep)
                drawSeg(s.lightMin, SleepLight)
                drawSeg(s.remMin,   SleepRem)
                drawSeg(s.awakeMin, SleepAwake)
            }
        }
    }
}
