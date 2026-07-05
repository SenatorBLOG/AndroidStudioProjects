package com.breatheonline.breathe.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.HeatCell
import com.breatheonline.breathe.viewmodel.StatsViewModel
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun MeditationRegularityScreen(colors: AppColors, navController: NavController) {
    val viewModel: StatsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint               = colors.primary,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        text  = stringResource(R.string.regularity_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.title,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text  = stringResource(R.string.regularity_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.subtitle,
                    )
                }
            }
        }

        // ── Streak cards ─────────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StreakCard(
                    icon    = Icons.Filled.LocalFireDepartment,
                    iconColor = Color(0xFFFF6F00),
                    label   = stringResource(R.string.stat_current_streak),
                    value   = "${state.currentStreak}",
                    unit    = stringResource(R.string.stat_unit_days),
                    colors  = colors,
                    mod     = Modifier.weight(1f),
                )
                StreakCard(
                    icon    = Icons.Filled.Star,
                    iconColor = Color(0xFFFFB300),
                    label   = stringResource(R.string.regularity_best_streak_label),
                    value   = "${state.bestStreakDays}",
                    unit    = stringResource(R.string.stat_unit_days),
                    colors  = colors,
                    mod     = Modifier.weight(1f),
                )
            }
        }

        // ── Consistency + total time row ──────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StreakCard(
                    icon    = Icons.Filled.TrendingUp,
                    iconColor = colors.primary,
                    label   = stringResource(R.string.regularity_this_week_label),
                    value   = "${state.consistencyPct}%",
                    unit    = stringResource(R.string.regularity_consistency_unit),
                    colors  = colors,
                    mod     = Modifier.weight(1f),
                )
                StreakCard(
                    icon    = Icons.Filled.Timer,
                    iconColor = Color(0xFF26C6DA),
                    label   = stringResource(R.string.regularity_total_time_label),
                    value   = viewModel.formatMinutesToClock(state.totalMeditationMinutes),
                    unit    = "hh:mm",
                    colors  = colors,
                    mod     = Modifier.weight(1f),
                )
            }
        }

        // ── Sessions this week ────────────────────────────────────────────────
        item {
            SectionCard(
                title  = stringResource(R.string.regularity_weekly_summary),
                colors = colors,
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text       = "${state.sessionsThisWeek}",
                            style      = MaterialTheme.typography.displaySmall,
                            color      = colors.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text  = stringResource(R.string.regularity_sessions_this_week),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "${state.totalSessions}",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = colors.title,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text  = stringResource(R.string.regularity_total_sessions),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text       = "${state.todayMinutes} min",
                            style      = MaterialTheme.typography.titleMedium,
                            color      = colors.primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text  = stringResource(R.string.regularity_today),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                    }
                }
            }
        }

        // ── 28-day heatmap ────────────────────────────────────────────────────
        if (state.heatmap28.isNotEmpty()) {
            item {
                SectionCard(title = stringResource(R.string.regularity_28_day_activity), colors = colors) {
                    HeatmapGrid(cells = state.heatmap28, accent = colors.primary, colors = colors)
                }
            }
        }

        // ── Weekday patterns ──────────────────────────────────────────────────
        if (state.weekdayAverages.any { it > 0f }) {
            item {
                SectionCard(title = stringResource(R.string.regularity_weekday_patterns), colors = colors) {
                    WeekdayBars(averages = state.weekdayAverages, accent = colors.primary, colors = colors)
                }
            }
        }

        // ── Average session duration ──────────────────────────────────────────
        item {
            SectionCard(title = stringResource(R.string.regularity_session_average), colors = colors) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        val avgMin = state.averageSessionDuration / 60
                        val avgSec = state.averageSessionDuration % 60
                        Text(
                            text       = if (avgMin > 0) "${avgMin}m ${avgSec}s" else "${avgSec}s",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = colors.title,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text  = stringResource(R.string.regularity_avg_per_session),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val delta = state.avgSessionMin7d - state.avgSessionMinPrev7d
                        val sign  = if (delta >= 0) "+" else ""
                        Text(
                            text       = "${sign}${delta} min",
                            style      = MaterialTheme.typography.titleMedium,
                            color      = if (delta >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text  = stringResource(R.string.regularity_vs_prior_7_days),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.subtitle,
                        )
                    }
                }
            }
        }
    }
}

// ── Streak card ───────────────────────────────────────────────────────────────

@Composable
private fun StreakCard(
    icon:      ImageVector,
    iconColor: Color,
    label:     String,
    value:     String,
    unit:      String,
    colors:    AppColors,
    mod:       Modifier,
) {
    Column(
        modifier = mod
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconColor,
                modifier           = Modifier.size(18.dp),
            )
        }
        Text(
            text          = label,
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            color      = colors.title,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = unit,
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle.copy(alpha = 0.7f),
        )
    }
}

// ── Section card ──────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title:   String,
    colors:  AppColors,
    content: @Composable () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            content()
        }
    }
}

// ── 28-day heatmap grid ───────────────────────────────────────────────────────

private val sundayFirstDays = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

@Composable
private fun HeatmapGrid(cells: List<HeatCell>, accent: Color, colors: AppColors) {
    val locale = Locale.getDefault()
    val dayLabels = sundayFirstDays.map { it.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, locale) }
    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Day labels
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dayLabels.forEach { day ->
                Text(
                    text      = day,
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color     = colors.subtitle.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
            }
        }
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { cell ->
                    val alpha = if (cell.intensity == 0f) 0.08f else 0.2f + cell.intensity * 0.8f
                    Box(
                        modifier         = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (cell.minutes > 0) accent.copy(alpha = alpha) else colors.subtitle.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (cell.minutes > 0) {
                            Text(
                                text  = "${cell.minutes}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = accent.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
                // Pad incomplete last row
                repeat(7 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        Text(
            text  = stringResource(R.string.regularity_heatmap_hint),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = colors.subtitle.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// ── Weekday bars ──────────────────────────────────────────────────────────────

@Composable
private fun WeekdayBars(averages: List<Float>, accent: Color, colors: AppColors) {
    val locale = Locale.getDefault()
    val days   = sundayFirstDays.map { it.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, locale) }
    val maxVal = averages.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        averages.forEachIndexed { i, avg ->
            val fraction = avg / maxVal
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text     = days[i],
                    style    = MaterialTheme.typography.labelSmall,
                    color    = colors.subtitle,
                    modifier = Modifier.width(28.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.subtitle.copy(alpha = 0.1f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent.copy(alpha = 0.7f)),
                    )
                }
                Text(
                    text     = if (avg > 0f) "${"%.0f".format(avg)}m" else "—",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (avg > 0f) colors.primary else colors.subtitle.copy(alpha = 0.4f),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
