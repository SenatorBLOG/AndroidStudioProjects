package com.breatheonline.breathe.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.ShimmerStatScreen
import com.breatheonline.breathe.ui.screens.stats.meditation.MeditationStatsContent
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.StatsState
import com.breatheonline.breathe.viewmodel.StatsViewModel
import java.time.format.TextStyle
import java.util.Locale

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun StatsScreen(
    colors: AppColors,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var period by remember { mutableStateOf(0) }  // 0=Week 1=Month 2=Year
    var topTab by remember { mutableStateOf(0) }  // 0=Meditation 1=Sleep

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text  = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.title,
            )
            Text(
                text     = stringResource(R.string.stats_subtitle),
                style    = MaterialTheme.typography.titleMedium,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(16.dp))
            StatsModeTabs(selected = topTab, onSelect = { topTab = it }, colors = colors)
        }

        if (state.isLoading) {
            ShimmerStatScreen(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
            return@Column
        }

        if (topTab == 0) {
            MeditationStatsContent(
                state         = state,
                period        = period,
                onPeriodSelect = { period = it },
                colors        = colors,
                navController = navController,
            )
        } else {
            SleepStatsContent(
                state = state,
                colors = colors,
                onSleepInsightFeedback = viewModel::setSleepInsightFeedback,
            )
        }
    }
}

// ── Mode tabs ──────────────────────────────────────────────────────────────────

@Composable
private fun StatsModeTabs(selected: Int, onSelect: (Int) -> Unit, colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("Meditation statistics", "Sleep statistics").forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) colors.onPrimary else colors.subtitle,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Sleep section ──────────────────────────────────────────────────────────────

@Composable
private fun SleepStatsContent(
    state: StatsState,
    colors: AppColors,
    onSleepInsightFeedback: (Boolean) -> Unit,
) {
    val sleep = state.sleepStats
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            SleepHeroCard(sleep = sleep, colors = colors)
            Spacer(Modifier.height(20.dp))
        }

        if (sleep.timeline.isNotEmpty()) {
            item {
                SleepSectionLabel("SLEEP DURATION · LAST 7 NIGHTS", colors)
                val chartValues = sleep.timeline.map { it.durationMin.toFloat() }
                val chartLabels = sleep.timeline.map {
                    it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                }
                val chartSubLabels = sleep.timeline.map { formatHoursLabel(it.durationMin) }
                StatBarChart(
                    values    = chartValues,
                    labels    = chartLabels,
                    subLabels = chartSubLabels,
                    colors    = colors,
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            SleepSectionLabel("SLEEP BREAKDOWN", colors)
            SleepBreakdownGrid(sleep, colors)
            Spacer(Modifier.height(20.dp))
        }

        item {
            SleepInsightCard(
                insight  = state.sleepInsight,
                feedback = state.sleepInsightFeedback,
                colors   = colors,
                onFeedback = onSleepInsightFeedback,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SleepSectionLabel(text: String, colors: AppColors) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = colors.subtitle,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp),
    )
}

@Composable
private fun SleepInsightCard(
    insight: String,
    feedback: Int?,
    colors: AppColors,
    onFeedback: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.primary)
            Text("DAILY SLEEP INSIGHT", style = MaterialTheme.typography.labelMedium, color = colors.subtitle)
        }
        Text(
            text = insight.ifBlank { "Daily sleep insight: connect sleep data to start building your nightly baseline." },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.title,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeedbackChip("Like", feedback == 1, Icons.Default.ThumbUp, colors) { onFeedback(true) }
            FeedbackChip("Dislike", feedback == -1, Icons.Default.ThumbDown, colors) { onFeedback(false) }
        }
    }
}

@Composable
private fun FeedbackChip(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.18f) else colors.background)
            .border(1.dp, if (selected) colors.primary.copy(alpha = 0.35f) else colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.primary else colors.subtitle, modifier = Modifier.size(16.dp))
        Text(label, color = if (selected) colors.primary else colors.subtitle, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SleepHeroCard(sleep: com.breatheonline.breathe.viewmodel.SleepStats, colors: AppColors) {
    val latest = sleep.timeline.lastOrNull()
    val latestMin = latest?.durationMin ?: sleep.latestSleepMin
    val avg7d = sleep.avgSleep7dMin

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF5E81FF).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = Color(0xFF5E81FF), modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (latestMin != null) formatHoursLabel(latestMin) else "No data",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.title,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (latest != null)
                        latest.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " night"
                    else "Last tracked night",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle,
                )
            }
            if (avg7d != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatHoursLabel(avg7d), style = MaterialTheme.typography.titleSmall, color = colors.title, fontWeight = FontWeight.SemiBold)
                    Text("7-day avg", style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                }
            }
        }

        if (latestMin != null) {
            val target = 480
            val fraction = (latestMin.toFloat() / target).coerceIn(0f, 1f)
            val barColor = when {
                latestMin >= 420 -> Color(0xFF5E81FF)
                latestMin >= 360 -> Color(0xFFF5B700)
                else             -> Color(0xFFE85D75)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("vs. 8h target", style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                    Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = barColor, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(999.dp)).background(colors.background),
                ) {
                    Box(Modifier.fillMaxWidth(fraction).height(6.dp)
                        .clip(RoundedCornerShape(999.dp)).background(barColor))
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sleep.avgHrv7d != null) {
                SleepHeroPill("HRV ${sleep.avgHrv7d}ms avg", colors, Modifier.weight(1f))
            }
            if (sleep.sleepHeartRate != null) {
                SleepHeroPill("${sleep.sleepHeartRate} bpm HR", colors, Modifier.weight(1f))
            }
            if (sleep.deepSleepMin != null) {
                SleepHeroPill("Deep ${formatHoursLabel(sleep.deepSleepMin)}", colors, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SleepHeroPill(text: String, colors: AppColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = colors.subtitle, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SleepBreakdownGrid(
    sleep: com.breatheonline.breathe.viewmodel.SleepStats,
    colors: AppColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SleepDataCard("Deep sleep", sleep.deepSleepMin?.let(::formatHoursLabel) ?: "No data", colors, Modifier.weight(1f))
            SleepDataCard("REM sleep", sleep.remSleepMin?.let(::formatHoursLabel) ?: "No data", colors, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SleepDataCard("Awake time", sleep.awakeCount?.let { formatHoursLabel(it) } ?: "No data", colors, Modifier.weight(1f))
            SleepDataCard("Sleep HR", sleep.sleepHeartRate?.let { "$it bpm" } ?: "No data", colors, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SleepDataCard("HRV", sleep.avgHrv7d?.let { "$it ms" } ?: "No data", colors, Modifier.weight(1f))
            SleepDataCard("Avg temperature", sleep.avgTemperatureC?.let { "${"%.1f".format(it)}°C" } ?: "No data", colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SleepDataCard(label: String, value: String, colors: AppColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = colors.title, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp), color = colors.subtitle)
    }
}

// ── Shared bar chart (sleep uses this) ────────────────────────────────────────

@Composable
internal fun StatBarChart(
    values:    List<Float>,
    labels:    List<String>,
    subLabels: List<String>? = null,
    colors:    AppColors,
    modifier:  Modifier = Modifier,
) {
    if (values.isEmpty()) return
    val maxVal  = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val hasSubLabels = subLabels != null && subLabels.size == values.size
    val bottomPad = if (hasSubLabels) 52.dp else 28.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(if (hasSubLabels) 220.dp else 190.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(bottom = bottomPad)) {
            val n        = values.size.coerceAtLeast(1)
            val areaW    = size.width / n
            val barW     = (areaW * 0.45f).coerceAtLeast(6f)
            val chartH   = size.height
            val gridColor = colors.subtitle.copy(alpha = 0.12f)

            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, chartH * (1f - r)), androidx.compose.ui.geometry.Offset(size.width, chartH * (1f - r)), 1.dp.toPx())
            }
            values.forEachIndexed { i, v ->
                if (v > 0f) {
                    val barH = (v / maxVal) * chartH * 0.88f
                    val x    = i * areaW + (areaW - barW) / 2f
                    drawRoundRect(
                        color        = colors.primary,
                        topLeft      = androidx.compose.ui.geometry.Offset(x, chartH - barH),
                        size         = androidx.compose.ui.geometry.Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEachIndexed { i, lbl ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(lbl, style = MaterialTheme.typography.labelSmall, color = colors.subtitle, textAlign = TextAlign.Center)
                    if (hasSubLabels) {
                        Text(
                            text = subLabels!![i],
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (values[i] > 0f) colors.title else colors.subtitle.copy(alpha = 0.4f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private fun formatHoursLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}
