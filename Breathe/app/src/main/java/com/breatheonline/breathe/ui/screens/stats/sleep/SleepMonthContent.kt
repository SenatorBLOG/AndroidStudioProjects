package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.SleepMonthView

@Composable
internal fun SleepMonthContent(
    view: SleepMonthView,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SleepScoreChart(
            points = view.scorePoints,
            avg = view.scoreAvg,
            deltaLine = view.scoreDeltaVsPrevMonth?.let { d ->
                if (d == 0) null else if (d > 0) "↑ $d vs last month" else "↓ ${-d} vs last month"
            },
            baseline = view.scoreBaseline,
            colors = colors,
        )
        Spacer(Modifier.height(16.dp))
        SleepScheduleSection(view.schedule, colors)
        Spacer(Modifier.height(16.dp))
        IdealSleepDurationChart(
            stacks = view.idealDuration,
            avgLabel = "${view.idealAvgMin / 60} hrs ${view.idealAvgMin % 60} m",
            deltaLine = view.idealDeltaVsPrevMonthMin?.let { d ->
                if (d == 0) null else if (d > 0) "↑ $d m vs last month" else "↓ ${-d} m vs last month"
            },
            colors = colors,
        )
    }
}
