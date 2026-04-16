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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.screens.stats.common.drawBaseline
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Text(text = "Sleep score", color = colors.title, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(text = "Avg $avg", color = colors.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (deltaLine != null) {
            Text(text = deltaLine, color = colors.subtitle)
        }
        Spacer(Modifier.height(12.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            drawHorizontalGrid(steps = 5, color = colors.subtitle.copy(alpha = 0.2f))
            baseline?.let {
                val y = size.height * (1f - it / 100f)
                drawBaseline(y = y, color = SleepAccent)
            }
            if (points.isEmpty()) return@Canvas
            val barWidth = size.width / (points.size * 2f)
            points.forEachIndexed { i, p ->
                val score = p.score ?: return@forEachIndexed
                val xCenter = size.width * ((i + 0.5f) / points.size)
                val h = size.height * (score / 100f)
                drawRoundRect(
                    color = SleepAccent,
                    topLeft = Offset(xCenter - barWidth / 2f, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }
        }
    }
}
