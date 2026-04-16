package com.breatheonline.breathe.ui.screens.stats.sleep

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.SleepDayView

@Composable
internal fun SleepDayContent(
    view: SleepDayView,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        if (view.stages.isNotEmpty()) {
            SleepHypnogram(view.stages, view.bedtime, view.wakeTime, colors)
        } else {
            FallbackStageBar(view, colors)
        }
        Spacer(Modifier.height(16.dp))
        SleepStagesDonut(
            totals = view.stageTotals,
            centerText = "${view.durationMin / 60}h ${view.durationMin % 60}m",
            colors = colors,
        )
        Spacer(Modifier.height(16.dp))
        SleepStagesBreakdown(view.stageTotals, colors)
        Spacer(Modifier.height(16.dp))
        MetricRow(
            label = "Avg sleeping HR",
            value = view.avgSleepingHrBpm?.let { "$it bpm" } ?: "No data",
            enabled = view.avgSleepingHrBpm != null,
            onClick = {
                if (view.avgSleepingHrBpm != null) {
                    Toast.makeText(ctx, "Avg HR across ${view.bedtime}–${view.wakeTime}", Toast.LENGTH_SHORT).show()
                }
            },
            colors = colors,
        )
        Spacer(Modifier.height(8.dp))
        MetricRow(
            label = "Average blood oxygen",
            value = "Coming soon",
            enabled = false,
            onClick = { Toast.makeText(ctx, "Coming soon", Toast.LENGTH_SHORT).show() },
            colors = colors,
        )
        Spacer(Modifier.height(8.dp))
        MetricRow(
            label = "Breathing score",
            value = "Coming soon",
            enabled = false,
            onClick = { Toast.makeText(ctx, "Coming soon", Toast.LENGTH_SHORT).show() },
            colors = colors,
        )
    }
}

@Composable
private fun FallbackStageBar(view: SleepDayView, colors: AppColors) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp)
    ) {
        Text("Detailed stages not available", color = colors.subtitle)
        Spacer(Modifier.height(6.dp))
        Text("Duration ${view.durationMin / 60}h ${view.durationMin % 60}m", color = colors.title)
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.title)
        Text(value, color = if (enabled) colors.title else colors.subtitle)
    }
}
