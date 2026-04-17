package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.screens.stats.common.PeriodTabs
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.utils.Regularity
import com.breatheonline.breathe.utils.buildLocalInsight
import com.breatheonline.breathe.viewmodel.SleepView
import com.breatheonline.breathe.viewmodel.StatsState

@Composable
internal fun SleepStatsContent(
    state: StatsState,
    onViewChange: (SleepView) -> Unit,
    onConnect: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        val day = state.sleepDayView
        when {
            day != null -> SleepHeroCard(
                title = "${day.durationMin / 60} hrs ${day.durationMin % 60} mins",
                subtitle = day.date.toString(),
                qualityLabel = day.qualityLabel,
                deltaLine = day.deltaVsAvg7dMin?.let { d ->
                    if (d == 0) null else if (d > 0) "↑ $d m vs 7-day avg" else "↓ ${-d} m vs 7-day avg"
                },
                colors = colors,
            )
            else -> EmptySleepCard(onConnect = onConnect, colors = colors)
        }
        Spacer(Modifier.height(16.dp))
        PeriodTabs(
            labels = listOf("D", "W", "M"),
            selected = state.sleepView.ordinal,
            onSelect = { onViewChange(SleepView.values()[it]) },
            colors = colors,
        )
        Spacer(Modifier.height(16.dp))
        when (state.sleepView) {
            SleepView.DAY   -> day?.let { SleepDayContent(it, colors) }
            SleepView.WEEK  -> state.sleepWeekView?.let { SleepWeekContent(it, colors) }
            SleepView.MONTH -> state.sleepMonthView?.let { SleepMonthContent(it, colors) }
        }
        Spacer(Modifier.height(16.dp))
        if (day != null) {
            SleepInsightBlock(
                paragraph = buildLocalInsight(
                    day = day,
                    wakeRegularity = state.sleepWeekView?.schedule?.wakeRegularity ?: Regularity.UNKNOWN,
                ),
                colors = colors,
            )
        }
    }
}

@Composable
private fun EmptySleepCard(onConnect: () -> Unit, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(24.dp),
    ) {
        Text("No sleep data yet", color = colors.title)
        Spacer(Modifier.height(8.dp))
        Text("Connect Health Connect in Profile → Devices to start tracking.", color = colors.subtitle)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onConnect) {
            Text("Open Profile")
        }
    }
}
