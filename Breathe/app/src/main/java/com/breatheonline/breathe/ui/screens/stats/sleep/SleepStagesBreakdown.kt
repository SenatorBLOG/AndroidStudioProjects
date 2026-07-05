package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepDeep
import com.breatheonline.breathe.ui.theme.SleepLight
import com.breatheonline.breathe.ui.theme.SleepRem
import com.breatheonline.breathe.viewmodel.StageTotals

@Composable
internal fun SleepStagesBreakdown(
    totals: StageTotals,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    val total = totals.totalMin.takeIf { it > 0 } ?: return
    Column(modifier = modifier.fillMaxWidth()) {
        StageRow("REM", SleepRem, totals.remMin, total, "10-30%", colors)
        Spacer(Modifier.height(10.dp))
        StageRow("Light", SleepLight, totals.lightMin, total, "20-60%", colors)
        Spacer(Modifier.height(10.dp))
        StageRow("Deep", SleepDeep, totals.deepMin, total, "20-40%", colors)
    }
}

@Composable
private fun StageRow(
    name: String,
    accent: Color,
    minutes: Int,
    total: Int,
    reference: String,
    colors: AppColors,
) {
    val percent = ((minutes.toFloat() / total) * 100).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(accent, RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "$percent%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = name, color = colors.title, fontWeight = FontWeight.SemiBold)
                Text(text = "Reference $reference", color = colors.subtitle, fontSize = 12.sp)
            }
        }
        Text(
            text = "${minutes / 60}h ${minutes % 60}m",
            color = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
