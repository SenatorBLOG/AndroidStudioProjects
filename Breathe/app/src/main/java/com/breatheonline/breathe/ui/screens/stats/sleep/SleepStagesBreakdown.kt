package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        StageRow("REM",   SleepRem,   totals.remMin,   total, "10–30", colors)
        Spacer(Modifier.height(12.dp))
        StageRow("Light", SleepLight, totals.lightMin, total, "20–60", colors)
        Spacer(Modifier.height(12.dp))
        StageRow("Deep",  SleepDeep,  totals.deepMin,  total, "20–40", colors)
    }
}

@Composable
private fun StageRow(
    name: String,
    accent: Color,
    mins: Int,
    total: Int,
    reference: String,
    colors: AppColors,
) {
    val pct = ((mins.toFloat() / total) * 100).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = "$pct%", color = colors.title, fontWeight = FontWeight.Bold)
            Text(text = name, color = accent, fontWeight = FontWeight.SemiBold)
            Text(text = "Reference: $reference%", color = colors.subtitle)
        }
        Text(text = "${mins / 60} h ${mins % 60} m", color = colors.title)
    }
}
