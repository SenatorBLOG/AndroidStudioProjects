package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.screens.stats.common.PeriodTabs
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.utils.Regularity
import com.breatheonline.breathe.utils.buildLocalInsight
import com.breatheonline.breathe.viewmodel.SleepView
import com.breatheonline.breathe.viewmodel.StatsState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
internal fun SleepStatsContent(
    state: StatsState,
    onViewChange: (SleepView) -> Unit,
    onMoveSelection: (Int) -> Unit,
    onJumpToLatest: () -> Unit,
    onInsightFeedback: (Boolean) -> Unit,
    onConnect: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    val day = state.sleepDayView
    if (day == null) {
        EmptySleepCard(onConnect = onConnect, colors = colors, modifier = modifier)
        return
    }

    val summary = buildHeroSummary(state)
    val prompts = buildSleepPrompts(state)
    val selectionLabel = buildSelectionLabel(state)
    val canGoBack = state.earliestSleepDate?.let { day.date > it } == true
    val canGoForward = state.latestSleepDate?.let { day.date < it } == true

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SleepHeroCard(
                eyebrow = summary.eyebrow,
                title = summary.title,
                qualityLabel = summary.qualityLabel,
                score = summary.score,
                metrics = summary.metrics,
                deltaLine = summary.deltaLine,
                colors = colors,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PeriodTabs(
                    labels = listOf("D", "W", "M"),
                    selected = state.sleepView.ordinal,
                    onSelect = { onViewChange(SleepView.values()[it]) },
                    colors = colors,
                )
                SleepSelectionNavigator(
                    label = selectionLabel,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    showLatestAction = canGoForward,
                    onBack = { onMoveSelection(-1) },
                    onForward = { onMoveSelection(1) },
                    onLatest = onJumpToLatest,
                    colors = colors,
                )
                SleepHighlightsRow(
                    state = state,
                    colors = colors,
                )
            }
        }
        item {
            when (state.sleepView) {
                SleepView.DAY -> SleepDayContent(day, colors)
                SleepView.WEEK -> state.sleepWeekView?.let { SleepWeekContent(it, colors) }
                SleepView.MONTH -> state.sleepMonthView?.let { SleepMonthContent(it, colors) }
            }
        }
        item {
            SleepInsightBlock(
                paragraph = buildLocalInsight(
                    day = day,
                    wakeRegularity = state.sleepWeekView?.schedule?.wakeRegularity ?: Regularity.UNKNOWN,
                ),
                prompts = prompts,
                feedback = state.sleepInsightFeedback,
                onFeedback = onInsightFeedback,
                colors = colors,
            )
        }
    }
}

@Composable
private fun EmptySleepCard(
    onConnect: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sleep_no_data_yet),
            style = MaterialTheme.typography.titleLarge,
            color = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.sleep_connect_hc_unlock),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.subtitle,
        )
        SleepEmptyChecklist(
            items = listOf(
                stringResource(R.string.sleep_checklist_consistency),
                stringResource(R.string.sleep_checklist_stages),
                stringResource(R.string.sleep_checklist_local),
            ),
            colors = colors,
        )
        TextButton(
            onClick = onConnect,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.primary.copy(alpha = 0.10f)),
        ) {
            Text(stringResource(R.string.sleep_open_profile))
        }
    }
}

@Composable
private fun SleepSelectionNavigator(
    label: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    showLatestAction: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onLatest: () -> Unit,
    colors: AppColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack, enabled = canGoBack, contentPadding = PaddingValues(horizontal = 8.dp)) {
            Text(stringResource(R.string.btn_prev))
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.sleep_viewing),
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.title,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (showLatestAction) {
            TextButton(onClick = onLatest, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(stringResource(R.string.btn_latest))
            }
            Spacer(Modifier.width(4.dp))
        }
        TextButton(onClick = onForward, enabled = canGoForward, contentPadding = PaddingValues(horizontal = 8.dp)) {
            Text(stringResource(R.string.btn_next))
        }
    }
}

@Composable
private fun SleepHighlightsRow(
    state: StatsState,
    colors: AppColors,
) {
    val items = when (state.sleepView) {
        SleepView.DAY -> listOf(
            SleepHighlight("Sleep debt", state.sleepDayView?.deltaVsAvg7dMin?.let(::sleepDebtLabel) ?: "No trend"),
            SleepHighlight("Deep sleep", state.sleepDayView?.stageTotals?.deepMin?.let(::formatCompactDuration) ?: "--"),
            SleepHighlight("Recovery", state.sleepDayView?.qualityLabel ?: "--"),
        )
        SleepView.WEEK -> listOf(
            SleepHighlight("Avg duration", formatCompactDuration(state.sleepWeekView?.idealAvgMin ?: 0)),
            SleepHighlight("Avg score", (state.sleepWeekView?.scoreAvg ?: 0).toString()),
            SleepHighlight("Wake regularity", regularityLabel(state.sleepWeekView?.schedule?.wakeRegularity ?: Regularity.UNKNOWN)),
        )
        SleepView.MONTH -> listOf(
            SleepHighlight("Avg duration", formatCompactDuration(state.sleepMonthView?.idealAvgMin ?: 0)),
            SleepHighlight("Avg score", (state.sleepMonthView?.scoreAvg ?: 0).toString()),
            SleepHighlight("Bedtime regularity", regularityLabel(state.sleepMonthView?.schedule?.fallAsleepRegularity ?: Regularity.UNKNOWN)),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.title,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SleepEmptyChecklist(
    items: List<String>,
    colors: AppColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "•",
                    color = colors.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item,
                    color = colors.title,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private data class SleepHeroSummary(
    val eyebrow: String,
    val title: String,
    val qualityLabel: String,
    val score: Int,
    val metrics: List<SleepHeroMetric>,
    val deltaLine: String?,
)

private data class SleepHighlight(
    val label: String,
    val value: String,
)

private fun buildHeroSummary(state: StatsState): SleepHeroSummary {
    val day = state.sleepDayView ?: error("sleep day view required")
    val dayDate = day.date.format(DateTimeFormatter.ofPattern("MMM d"))
    return when (state.sleepView) {
        SleepView.DAY -> SleepHeroSummary(
            eyebrow = "Night overview - $dayDate",
            title = formatDuration(day.durationMin),
            qualityLabel = day.qualityLabel,
            score = day.score,
            metrics = listOf(
                SleepHeroMetric("Bedtime", day.bedtime),
                SleepHeroMetric("Wake-up", day.wakeTime),
                SleepHeroMetric("Sleeping HR", day.avgSleepingHrBpm?.let { "$it bpm" } ?: "No data"),
            ),
            deltaLine = day.deltaVsAvg7dMin?.let { deltaLabel(it, "7-day avg") },
        )

        SleepView.WEEK -> {
            val week = state.sleepWeekView
            SleepHeroSummary(
                eyebrow = week?.rangeLabel ?: "Weekly sleep",
                title = formatDuration(week?.idealAvgMin ?: day.durationMin),
                qualityLabel = qualityLabelForScore(week?.scoreAvg ?: day.score),
                score = week?.scoreAvg ?: day.score,
                metrics = listOf(
                    SleepHeroMetric("Avg bedtime", formatClock(week?.schedule?.avgFallAsleepMinOfDay)),
                    SleepHeroMetric("Avg wake-up", formatClock(week?.schedule?.avgWakeMinOfDay)),
                    SleepHeroMetric("Regularity", regularityLabel(week?.schedule?.wakeRegularity ?: Regularity.UNKNOWN)),
                ),
                deltaLine = week?.scoreDeltaVsPrevWeek?.let { deltaLabel(it, "last week", unit = "") },
            )
        }

        SleepView.MONTH -> {
            val month = state.sleepMonthView
            SleepHeroSummary(
                eyebrow = month?.monthLabel ?: "Monthly sleep",
                title = formatDuration(month?.idealAvgMin ?: day.durationMin),
                qualityLabel = qualityLabelForScore(month?.scoreAvg ?: day.score),
                score = month?.scoreAvg ?: day.score,
                metrics = listOf(
                    SleepHeroMetric("Avg bedtime", formatClock(month?.schedule?.avgFallAsleepMinOfDay)),
                    SleepHeroMetric("Avg wake-up", formatClock(month?.schedule?.avgWakeMinOfDay)),
                    SleepHeroMetric("Regularity", regularityLabel(month?.schedule?.wakeRegularity ?: Regularity.UNKNOWN)),
                ),
                deltaLine = month?.idealDeltaVsPrevMonthMin?.let { deltaLabel(it, "last month") },
            )
        }
    }
}

private fun buildSleepPrompts(state: StatsState): List<String> {
    val day = state.sleepDayView ?: return emptyList()
    val prompts = mutableListOf<String>()
    if (day.durationMin < 420) prompts += "Help me build an earlier bedtime routine"
    if (day.stageTotals.deepMin < (day.durationMin * 0.15f).toInt()) prompts += "How can I increase deep sleep?"
    if (state.sleepWeekView?.schedule?.wakeRegularity == Regularity.IRREGULAR) {
        prompts += "How do I stabilize my wake-up time?"
    }
    if (prompts.size < 3) prompts += "Suggest a breathing technique before sleep"
    if (prompts.size < 3) prompts += "Why is my sleep quality dropping?"
    return prompts.distinct().take(3)
}

private fun buildSelectionLabel(state: StatsState): String = when (state.sleepView) {
    SleepView.DAY -> state.sleepDayView?.date?.format(DateTimeFormatter.ofPattern("EEEE, MMM d")) ?: "--"
    SleepView.WEEK -> state.sleepWeekView?.rangeLabel ?: "--"
    SleepView.MONTH -> state.sleepMonthView?.monthLabel ?: "--"
}

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (mins == 0) "$hours hrs" else "$hours hrs $mins mins"
}

private fun formatCompactDuration(minutes: Int): String {
    if (minutes <= 0) return "--"
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours == 0) "${mins}m" else if (mins == 0) "${hours}h" else "${hours}h ${mins}m"
}

private fun deltaLabel(delta: Int, target: String, unit: String = "m"): String {
    if (delta == 0) return "On par with $target"
    val amount = if (unit.isBlank()) abs(delta).toString() else "${abs(delta)}$unit"
    return if (delta > 0) "+$amount vs $target" else "-$amount vs $target"
}

private fun sleepDebtLabel(delta: Int): String = when {
    delta >= 30 -> "Ahead by ${delta}m"
    delta <= -30 -> "Behind by ${abs(delta)}m"
    else -> "Near baseline"
}

private fun formatClock(minutes: Int?): String =
    if (minutes == null) "--:--" else "%02d:%02d".format((minutes / 60) % 24, minutes % 60)

private fun regularityLabel(regularity: Regularity): String = when (regularity) {
    Regularity.REGULAR -> "Regular"
    Regularity.IRREGULAR -> "Irregular"
    Regularity.UNKNOWN -> "Unknown"
}

private fun qualityLabelForScore(score: Int): String = when {
    score >= 80 -> "Excellent"
    score >= 60 -> "Good"
    score >= 40 -> "Fair"
    else -> "Poor"
}
