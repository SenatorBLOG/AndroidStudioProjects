package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.SleepWeekView

@Composable
internal fun SleepWeekContent(
    view: SleepWeekView,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SleepScoreChart(
            points = view.scorePoints,
            avg = view.scoreAvg,
            deltaLine = view.scoreDeltaVsPrevWeek?.let { d ->
                if (d == 0) null else if (d > 0) "↑ $d vs last week" else "↓ ${-d} vs last week"
            },
            baseline = null,
            colors = colors,
        )
        Spacer(Modifier.height(16.dp))
        SleepScheduleSection(view.schedule, colors)
        Spacer(Modifier.height(16.dp))
        IdealSleepDurationChart(
            stacks = view.idealDuration,
            avgLabel = "${view.idealAvgMin / 60} hrs ${view.idealAvgMin % 60} m",
            deltaLine = view.idealDeltaVsPrevWeekMin?.let { d ->
                if (d == 0) null else if (d > 0) "↑ $d m vs last week" else "↓ ${-d} m vs last week"
            },
            colors = colors,
        )
    }
}
