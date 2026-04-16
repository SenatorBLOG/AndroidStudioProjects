package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAwake
import com.breatheonline.breathe.ui.theme.SleepDeep
import com.breatheonline.breathe.ui.theme.SleepLight
import com.breatheonline.breathe.ui.theme.SleepRem
import com.breatheonline.breathe.viewmodel.StageTotals

@Composable
internal fun SleepStagesDonut(
    totals: StageTotals,
    centerText: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    val total = totals.totalMin.takeIf { it > 0 } ?: return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)) {
            val side = minOf(size.width, size.height)
            val topLeft = Offset((size.width - side) / 2f, (size.height - side) / 2f)
            val ringSize = Size(side, side)
            val stroke = Stroke(width = side * 0.12f)
            var start = -90f
            val segs = listOf(
                totals.deepMin to SleepDeep,
                totals.lightMin to SleepLight,
                totals.remMin to SleepRem,
                totals.awakeMin to SleepAwake,
            )
            segs.forEach { (mins, color) ->
                val sweep = 360f * (mins.toFloat() / total)
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = ringSize,
                    style = stroke,
                )
                start += sweep
            }
        }
        Text(text = centerText, color = colors.title)
    }
}
