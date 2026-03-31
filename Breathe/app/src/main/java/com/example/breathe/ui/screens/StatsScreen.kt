package com.example.breathe.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.R
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.DayMinutes
import com.example.breathe.viewmodel.DurationBuckets
import com.example.breathe.viewmodel.HeatCell
import com.example.breathe.viewmodel.InsightCard
import com.example.breathe.viewmodel.MonthPoint
import com.example.breathe.viewmodel.MoodPoint
import com.example.breathe.viewmodel.MoodStats
import com.example.breathe.viewmodel.StatsState
import com.example.breathe.viewmodel.StatsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.breathe.data.models.NlpInsights
import com.example.breathe.data.models.NlpTimelinePoint
import com.example.breathe.viewmodel.HealthSummary

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun StatsScreen(
    colors:        AppColors,
    navController: NavController,
    modifier:      Modifier = Modifier,
    viewModel:     StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var period by remember { mutableStateOf(0) }  // 0=Week 1=Month 2=Year

    val chartData = when (period) {
        1    -> state.monthlyData
        2    -> state.yearlyData
        else -> state.weeklyData
    }

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
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(40.dp))
            }
            return@Column
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Period tabs ───────────────────────────────────────────────────
            item {
                PeriodTabs(selected = period, onSelect = { period = it }, colors = colors)
                Spacer(Modifier.height(16.dp))
            }

            // ── Activity chart ────────────────────────────────────────────────
            item {
                ActivityChart(data = chartData, period = period, colors = colors)
                Spacer(Modifier.height(20.dp))
            }

            // ── Annual progress chart ─────────────────────────────────────────
            item {
                AnnualProgressSection(state = state, colors = colors)
                Spacer(Modifier.height(20.dp))
            }

            // ── Stats cards grid ──────────────────────────────────────────────
            item {
                StatsCardsGrid(state = state, colors = colors)
                Spacer(Modifier.height(20.dp))
            }

            // ── Duration donut ────────────────────────────────────────────────
            item {
                DurationDonutSection(buckets = state.durationBuckets, colors = colors)
                Spacer(Modifier.height(20.dp))
            }

            // ── Health data panel ─────────────────────────────────────────────
            val health = state.health
            if (health != null) {
                item {
                    SectionLabel("HEALTH DATA", colors)
                    HealthPanel(health = health, colors = colors)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── Mood grid ─────────────────────────────────────────────────────
            if (state.moodPoints.any { it.delta != null }) {
                item {
                    MoodGrid(points = state.moodPoints, colors = colors)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── Mood breakdown ────────────────────────────────────────────────
            if (state.moodStats.total > 0) {
                item {
                    MoodBreakdownSection(mood = state.moodStats, colors = colors)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── AI Insights ───────────────────────────────────────────────────
            if (state.insights.isNotEmpty()) {
                item { SectionLabel("AI INSIGHTS", colors) }
                items(state.insights) { card ->
                    InsightCardRow(card = card, colors = colors)
                    Spacer(Modifier.height(10.dp))
                }
            }

            // ── Emotional Intelligence ────────────────────────────────────────
            val nlp = state.nlpInsights
            if (nlp != null && nlp.totalAnalyzed > 0) {
                item {
                    SectionLabel("EMOTIONAL INTELLIGENCE", colors)
                    NlpSection(nlp = nlp, colors = colors, navController = navController)
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── Quick links ───────────────────────────────────────────────────
            item {
                SectionLabel("EXPLORE", colors)
                QuickLinkRow(
                    label    = "Session History",
                    onClick  = { navController.navigate(Route.SESSION_HISTORY) },
                    colors   = colors,
                )
                Spacer(Modifier.height(10.dp))
                QuickLinkRow(
                    label    = "Meditation Regularity",
                    onClick  = { navController.navigate(Route.MEDITATION_REGULARITY) },
                    colors   = colors,
                )
                Spacer(Modifier.height(10.dp))
                QuickLinkRow(
                    label    = "Emotional Journal",
                    onClick  = { navController.navigate(Route.JOURNAL) },
                    colors   = colors,
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ── Period tabs ────────────────────────────────────────────────────────────────

@Composable
private fun PeriodTabs(selected: Int, onSelect: (Int) -> Unit, colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf("Week", "Month", "Year").forEachIndexed { i, label ->
            val active = i == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (active) colors.onPrimary else colors.subtitle,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Activity chart ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityChart(
    data:   List<DayMinutes>,
    period: Int,
    colors: AppColors,
) {
    if (data.isEmpty()) return

    val maxMin    = data.maxOf { it.minutes }.coerceAtLeast(1)
    val gridColor = colors.subtitle.copy(alpha = 0.15f)

    val labels = data.mapIndexed { i, dm ->
        when (period) {
            2    -> dm.date.format(DateTimeFormatter.ofPattern("MMM"))
            1    -> if (i % 5 == 0) dm.date.dayOfMonth.toString() else ""
            else -> dm.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface),
        ) {
            val barColor = colors.primary
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 28.dp),
            ) {
                val n        = data.size.coerceAtLeast(1)
                val areaW    = size.width / n
                val barW     = (areaW * 0.5f).coerceAtLeast(4f)
                val chartH   = size.height

                // Grid
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                    drawLine(
                        color       = gridColor,
                        start       = Offset(0f, chartH * (1f - r)),
                        end         = Offset(size.width, chartH * (1f - r)),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                data.forEachIndexed { i, dm ->
                    if (dm.minutes > 0) {
                        val barH = (dm.minutes.toFloat() / maxMin) * chartH * 0.85f
                        val x    = i * areaW + (areaW - barW) / 2
                        drawRoundRect(
                            color        = barColor,
                            topLeft      = Offset(x, chartH - barH),
                            size         = Size(barW, barH),
                            cornerRadius = CornerRadius(4.dp.toPx()),
                        )
                    }
                }
            }

            // X-axis labels
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                labels.forEach { lbl ->
                    Text(
                        text      = lbl,
                        style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color     = colors.subtitle,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.width(26.dp),
                    )
                }
            }
        }
    }
}

// ── Milestone badges ───────────────────────────────────────────────────────────

@Composable
private fun MilestoneBadges(state: StatsState, colors: AppColors) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BadgeCard("🔥", "STREAK",     "${state.currentStreak} days",       colors, Modifier.weight(1f))
            BadgeCard("⏱",  "TOTAL TIME", "${state.totalMeditationMinutes} min", colors, Modifier.weight(1f))
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BadgeCard("🔄", "SESSIONS",     "${state.totalSessions}",                               colors, Modifier.weight(1f))
            BadgeCard("🧘", "AVG CALMNESS", if (state.avgCalmness > 0f) "${"%.1f".format(state.avgCalmness)}/10" else "—", colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BadgeCard(
    emoji:    String,
    label:    String,
    value:    String,
    colors:   AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle,
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            color      = colors.title,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Mood grid ──────────────────────────────────────────────────────────────────

@Composable
private fun MoodGrid(points: List<MoodPoint>, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        SectionLabel("MOOD LIFT", colors)

        val rows = points.take(35).chunked(7)
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { pt ->
                    MoodCell(delta = pt.delta, colors = colors, modifier = Modifier.weight(1f))
                }
                repeat(7 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(lerp(Color(0xFF388E3C), Color(0xFFA5D6A7), 0.3f))
            Text("  Improved  ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
            LegendDot(lerp(Color(0xFFD32F2F), Color(0xFFEF9A9A), 0.3f))
            Text("  Decreased  ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
            LegendDot(colors.subtitle.copy(alpha = 0.20f))
            Text("  No data", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
        }
    }
}

@Composable
private fun MoodCell(delta: Int?, colors: AppColors, modifier: Modifier = Modifier) {
    val color = when {
        delta == null -> colors.subtitle.copy(alpha = 0.18f)
        delta > 0     -> lerp(Color(0xFF388E3C), Color(0xFFA5D6A7), 1f - (delta.coerceIn(1, 5) / 5f) * 0.7f)
        delta < 0     -> lerp(Color(0xFFD32F2F), Color(0xFFEF9A9A), 1f - ((-delta).coerceIn(1, 5) / 5f) * 0.7f)
        else          -> colors.subtitle.copy(alpha = 0.25f)
    }
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
    )
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

// ── AI Insights ────────────────────────────────────────────────────────────────

@Composable
private fun InsightCardRow(card: InsightCard, colors: AppColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = card.emoji, fontSize = 26.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text       = card.title,
                style      = MaterialTheme.typography.bodyMedium,
                color      = colors.title,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = card.body,
                style    = MaterialTheme.typography.bodySmall,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

// ── Quick link row ─────────────────────────────────────────────────────────────

@Composable
private fun QuickLinkRow(label: String, onClick: () -> Unit, colors: AppColors) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            color      = colors.title,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint               = colors.subtitle,
            modifier           = Modifier.size(18.dp),
        )
    }
}

// ── Section label ──────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, colors: AppColors) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = colors.subtitle,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp),
    )
}

// ── Health data panel ──────────────────────────────────────────────────────────

@Composable
private fun HealthPanel(health: HealthSummary, colors: AppColors) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (health.avgSleep7dMin != null) {
                val h = health.avgSleep7dMin / 60; val m = health.avgSleep7dMin % 60
                HealthTile(Icons.Default.Hotel, "AVG SLEEP", if (m > 0) "${h}h ${m}m" else "${h}h", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
            if (health.avgHrv7d != null) {
                HealthTile(Icons.Default.TrendingUp, "AVG HRV", "${health.avgHrv7d}ms", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (health.restingHr != null) {
                HealthTile(Icons.Default.Favorite, "RESTING HR", "${health.restingHr} bpm", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
            if (health.recoveryScore != null) {
                HealthTile(Icons.Default.AutoAwesome, "RECOVERY", "${health.recoveryScore}/100", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
        }
        if (health.sources.isNotEmpty()) {
            Text(
                text  = "From: ${health.sources.joinToString(", ") { healthSourceLabel(it) }}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.subtitle.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}

@Composable
private fun HealthTile(
    icon:     ImageVector,
    label:    String,
    value:    String,
    colors:   AppColors,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(14.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
    }
}

private fun healthSourceLabel(s: String) = when (s) {
    "apple_health" -> "Apple Health"
    "google_fit"   -> "Google Fit"
    "fitbit"       -> "Fitbit"
    else           -> s.replaceFirstChar { it.uppercase() }
}

// ── NLP Emotional Intelligence ────────────────────────────────────────────────

@Composable
private fun NlpSection(
    nlp:           NlpInsights,
    colors:        AppColors,
    navController: NavController,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${nlp.totalAnalyzed} sessions analyzed",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.subtitle,
            )
            Text(
                "View Journal →",
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.primary,
                modifier = Modifier.clickable { navController.navigate(Route.JOURNAL) },
            )
        }

        if (nlp.timeline.size >= 2) {
            EmotionalScoreChart(timeline = nlp.timeline, colors = colors)
        }

        ThemesSentimentCard(nlp = nlp, colors = colors)
    }
}

@Composable
private fun EmotionalScoreChart(timeline: List<NlpTimelinePoint>, colors: AppColors) {
    val accent    = colors.primary
    val gridColor = colors.subtitle.copy(alpha = 0.12f)
    val bgColor   = colors.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Emotional score", style = MaterialTheme.typography.labelMedium, color = colors.title, fontWeight = FontWeight.SemiBold)
        Text("−1 negative  →  +1 positive", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)

        Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            val n    = timeline.size
            val midY = size.height / 2f

            // Horizontal grid lines
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(gridColor, Offset(0f, r * size.height), Offset(size.width, r * size.height), 1.dp.toPx())
            }
            // Zero reference
            drawLine(
                color       = accent.copy(alpha = 0.25f),
                start       = Offset(0f, midY),
                end         = Offset(size.width, midY),
                strokeWidth = 1.dp.toPx(),
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
            )

            val pts = timeline.mapIndexed { i, pt ->
                Offset(
                    x = i.toFloat() / (n - 1).coerceAtLeast(1) * size.width,
                    y = ((1.0 - pt.score) / 2.0 * size.height).toFloat().coerceIn(2f, size.height - 2f),
                )
            }

            if (pts.size >= 2) {
                val path = Path()
                path.moveTo(pts[0].x, pts[0].y)
                pts.drop(1).forEach { path.lineTo(it.x, it.y) }
                drawPath(path, accent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            pts.forEach {
                drawCircle(accent, radius = 3.5.dp.toPx(), center = it)
                drawCircle(bgColor, radius = 1.5.dp.toPx(), center = it)
            }
        }

        // X-axis date labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            timeline.forEach { pt ->
                Text(
                    text = runCatching {
                        Instant.parse(pt.date).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("M/d"))
                    }.getOrDefault(""),
                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color    = colors.subtitle,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ThemesSentimentCard(nlp: NlpInsights, colors: AppColors) {
    val neutralBlue  = Color(0xFF7AAEC8)
    val negativeRed  = Color(0xFFFF8A8A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Recurring themes", style = MaterialTheme.typography.labelMedium, color = colors.title, fontWeight = FontWeight.SemiBold)

        if (nlp.topThemes.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                nlp.topThemes.take(5).forEach { (theme, count) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("$theme · $count", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("SENTIMENT SPLIT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
            val dist = nlp.sentimentDist
            Row(
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (dist.positive > 0) Box(Modifier.weight(dist.positive.toFloat()).fillMaxWidth().height(6.dp).background(colors.primary))
                if (dist.neutral  > 0) Box(Modifier.weight(dist.neutral.toFloat()).fillMaxWidth().height(6.dp).background(neutralBlue))
                if (dist.negative > 0) Box(Modifier.weight(dist.negative.toFloat()).fillMaxWidth().height(6.dp).background(negativeRed))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SentLegend("Positive ${dist.positive}", colors.primary)
                SentLegend("Neutral ${dist.neutral}",   neutralBlue)
                SentLegend("Difficult ${dist.negative}", negativeRed)
            }
        }
    }
}

@Composable
private fun SentLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = color.copy(alpha = 0.9f))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── Annual Progress Chart ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AnnualProgressSection(state: StatsState, colors: AppColors) {
    var byYear by remember { mutableStateOf(false) }
    val data   = if (byYear) state.allYearsData else state.annualMonthData

    SectionLabel("ANNUAL PROGRESS", colors)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.width(12.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.primary))
                    Text("Minutes", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.width(12.dp).height(2.dp).background(colors.primary.copy(alpha = 0.6f)))
                    Text("Sessions", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
            }
            AnnualViewToggle(byYear = byYear, onToggle = { byYear = it }, colors = colors)
        }

        if (data.isEmpty() || data.all { it.totalMinutes == 0 && it.sessions == 0 }) {
            Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                Text("No data yet", style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
            }
        } else {
            val maxMins  = data.maxOf { it.totalMinutes }.coerceAtLeast(1)
            val maxSess  = data.maxOf { it.sessions }.coerceAtLeast(1)
            val barColor = colors.primary
            val lineCol  = colors.primary.copy(alpha = 0.65f)
            val dotGreen = Color(0xFF4AE8A0)
            val gridCol  = colors.subtitle.copy(alpha = 0.12f)

            Box(Modifier.fillMaxWidth().height(160.dp)) {
                Canvas(
                    modifier = Modifier.fillMaxSize().padding(bottom = 22.dp),
                ) {
                    val n     = data.size.coerceAtLeast(1)
                    val areaW = size.width / n
                    val barW  = (areaW * 0.45f).coerceAtLeast(4f)
                    val h     = size.height

                    listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                        drawLine(gridCol, Offset(0f, h * (1f - r)), Offset(size.width, h * (1f - r)), 1.dp.toPx())
                    }
                    data.forEachIndexed { i, pt ->
                        if (pt.totalMinutes > 0) {
                            val barH = (pt.totalMinutes.toFloat() / maxMins) * h * 0.82f
                            val x    = i * areaW + (areaW - barW) / 2f
                            drawRoundRect(
                                color        = barColor.copy(alpha = if (pt.isCurrent) 1f else 0.55f),
                                topLeft      = Offset(x, h - barH),
                                size         = Size(barW, barH),
                                cornerRadius = CornerRadius(4.dp.toPx()),
                            )
                        }
                    }
                    val pts = data.mapIndexed { i, pt ->
                        Offset(i * areaW + areaW / 2f, h - (pt.sessions.toFloat() / maxSess) * h * 0.82f)
                    }
                    if (pts.size >= 2) {
                        val linePath = Path()
                        linePath.moveTo(pts[0].x, pts[0].y)
                        pts.drop(1).forEach { linePath.lineTo(it.x, it.y) }
                        drawPath(linePath, lineCol, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    }
                    pts.forEachIndexed { i, pt ->
                        if (data[i].sessions > 0) {
                            val cur = data[i].isCurrent
                            if (cur) drawCircle(dotGreen.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = pt)
                            drawCircle(if (cur) dotGreen else lineCol, radius = if (cur) 5.dp.toPx() else 3.dp.toPx(), center = pt)
                        }
                    }
                }
                // X-axis labels
                Row(
                    modifier              = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    data.forEachIndexed { i, pt ->
                        val skipLabel = data.size > 8 && i % 2 != 0
                        Text(
                            text      = if (skipLabel) "" else pt.name,
                            style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color     = if (pt.isCurrent) colors.primary else colors.subtitle,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // Divider
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.subtitle.copy(alpha = 0.12f)))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${data.sumOf { it.sessions }} sessions · ${data.sumOf { it.totalMinutes }}m total",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.subtitle,
            )
            Text(
                "● this ${if (byYear) "year" else "month"}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun AnnualViewToggle(byYear: Boolean, onToggle: (Boolean) -> Unit, colors: AppColors) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(false to "12M", true to "YEAR").forEach { (isYear, label) ->
            val active = isYear == byYear
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onToggle(isYear) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (active) colors.onPrimary else colors.subtitle,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── Duration Donut Chart ──────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DurationDonutSection(buckets: DurationBuckets, colors: AppColors) {
    if (buckets.total == 0) return
    SectionLabel("SESSION DURATION", colors)

    val bucketColors = listOf(
        colors.primary,
        colors.primary.copy(alpha = 0.7f),
        Color(0xFF4AE8A0),
        Color(0xFF4AE8A0).copy(alpha = 0.55f),
    )
    val labels   = listOf("Short ≤5m", "Medium 6-15m", "Long 16-30m", "Extended 30+m")
    val counts   = listOf(buckets.short, buckets.medium, buckets.long, buckets.extended)
    val total    = buckets.total.toFloat()
    val avgMins  = listOf(2.5f, 10f, 22.5f, 45f)
        .zip(counts) { mid, cnt -> mid * cnt }
        .sum() / total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Donut
            Box(
                modifier         = Modifier.size(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                val strokeW  = 18.dp
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sw       = strokeW.toPx()
                    val tlOffset = Offset(sw / 2f, sw / 2f)
                    val arcSize  = Size(size.width - sw, size.height - sw)
                    var startAngle = -90f
                    counts.forEachIndexed { i, cnt ->
                        val sweep = cnt / total * 360f
                        if (sweep > 1f) {
                            drawArc(
                                color      = bucketColors[i],
                                startAngle = startAngle + 1f,
                                sweepAngle = sweep - 2f,
                                useCenter  = false,
                                topLeft    = tlOffset,
                                size       = arcSize,
                                style      = Stroke(width = sw, cap = StrokeCap.Round),
                            )
                        }
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${"%.1f".format(avgMins)}",
                        style      = MaterialTheme.typography.titleMedium,
                        color      = colors.title,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "avg min",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = colors.subtitle,
                    )
                }
            }

            // Legend
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                counts.forEachIndexed { i, cnt ->
                    if (cnt == 0) return@forEachIndexed
                    val pct = (cnt / total * 100).toInt()
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(bucketColors[i]))
                        Text(
                            labels[i].substringBefore(" "),
                            style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color    = colors.subtitle,
                            modifier = Modifier.width(44.dp),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.subtitle.copy(alpha = 0.18f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(pct / 100f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(bucketColors[i]),
                            )
                        }
                        Text(
                            "$cnt× · $pct%",
                            style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color    = colors.subtitle,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.subtitle.copy(alpha = 0.12f)))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text("${buckets.total} sessions total",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = colors.subtitle)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                counts.forEachIndexed { i, cnt ->
                    if (cnt == 0) return@forEachIndexed
                    val chipLabel = labels[i].substringAfter(" ")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, bucketColors[i].copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .background(bucketColors[i].copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(chipLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = bucketColors[i])
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── Stats Cards Grid ──────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StatsCardsGrid(state: StatsState, colors: AppColors) {
    SectionLabel("THIS WEEK", colors)

    val sparkMins   = state.weeklyData.map { it.minutes.toFloat() }
    val sparkActive = state.weeklyData.map { if (it.minutes > 0) 1f else 0f }

    val sessDelta = if (state.sessionsPrev7d == 0 && state.sessionsThisWeek == 0) 0f
        else if (state.sessionsPrev7d == 0) 100f
        else (state.sessionsThisWeek - state.sessionsPrev7d).toFloat() / state.sessionsPrev7d * 100f

    val avgDelta = if (state.avgSessionMinPrev7d == 0 && state.avgSessionMin7d == 0) 0f
        else if (state.avgSessionMinPrev7d == 0) 100f
        else (state.avgSessionMin7d - state.avgSessionMinPrev7d).toFloat() / state.avgSessionMinPrev7d * 100f

    val wdNames      = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    val bestWdIdx    = state.weekdayAverages.indices.maxByOrNull { state.weekdayAverages[it] } ?: 0
    val maxWdAvg     = state.weekdayAverages.maxOrNull()?.coerceAtLeast(0.01f) ?: 1f

    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Row 1: Sessions · Consistency
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatCard(
                value    = "${state.sessionsThisWeek}",
                label    = "Sessions · 7 days",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                StatDelta(sessDelta, colors)
                Spacer(Modifier.height(6.dp))
                Sparkline(sparkMins, colors.primary, fillArea = true, modifier = Modifier.fillMaxWidth().height(40.dp))
            }
            MiniStatCard(
                value    = "${state.consistencyPct}%",
                label    = "Consistency",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                Sparkline(sparkActive, colors.primary, fillArea = false, modifier = Modifier.fillMaxWidth().height(48.dp))
            }
        }

        // Row 2: Avg session · Best weekday
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val avgLabel = if (state.avgSessionMin7d >= 60)
                "${state.avgSessionMin7d / 60}h ${state.avgSessionMin7d % 60}m"
            else "${state.avgSessionMin7d}m"

            MiniStatCard(
                value    = avgLabel,
                label    = "Avg session · 7 days",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                StatDelta(avgDelta, colors)
                Spacer(Modifier.height(6.dp))
                Sparkline(sparkMins, colors.primary, fillArea = true, modifier = Modifier.fillMaxWidth().height(40.dp))
            }
            MiniStatCard(
                value    = if (maxWdAvg > 0.01f) wdNames[bestWdIdx] else "—",
                label    = "Best day · avg ${state.weekdayAverages[bestWdIdx].toInt()}m",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    state.weekdayAverages.forEachIndexed { i, avg ->
                        val isBest = i == bestWdIdx
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                wdNames[i].take(2),
                                style    = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color    = if (isBest) colors.primary else colors.subtitle,
                                modifier = Modifier.width(18.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colors.subtitle.copy(alpha = 0.18f)),
                            ) {
                                val barPct = (avg / maxWdAvg).coerceIn(0f, 1f)
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(barPct)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isBest) colors.primary else colors.primary.copy(alpha = 0.35f)),
                                )
                            }
                            Text(
                                if (avg > 0f) "${avg.toInt()}m" else "—",
                                style    = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color    = if (isBest) colors.primary else colors.subtitle,
                                modifier = Modifier.width(22.dp),
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }

        // Row 3: Mood · Heatmap
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val ms = state.moodStats
            MiniStatCard(
                value    = if (ms.total > 0) "${ms.positivePct}%" else "—",
                label    = "Positive mood",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                Spacer(Modifier.height(4.dp))
                listOf(
                    Triple("Good",  ms.positivePct, Color(0xFF4AE8A0)),
                    Triple("Ok",    ms.neutralPct,  Color(0xFF4A9EFF)),
                    Triple("Tough", ms.negativePct, Color(0xFFFF8A8A)),
                ).forEach { (lbl, pct, col) ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(lbl, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = colors.subtitle, modifier = Modifier.width(26.dp))
                        Box(
                            modifier = Modifier.weight(1f).height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.subtitle.copy(alpha = 0.18f)),
                        ) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(pct / 100f)
                                .clip(RoundedCornerShape(2.dp)).background(col))
                        }
                        Text("$pct%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = col, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }

            MiniStatCard(
                value    = "${state.totalSessions} total",
                label    = "Activity · 28 days",
                colors   = colors,
                modifier = Modifier.weight(1f),
            ) {
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    state.heatmap28.chunked(7).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            row.forEach { cell ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (cell.intensity > 0f)
                                                colors.primary.copy(alpha = 0.18f + cell.intensity * 0.82f)
                                            else
                                                colors.subtitle.copy(alpha = 0.15f),
                                        ),
                                )
                            }
                        }
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        listOf("S","M","T","W","T","F","S").forEach { d ->
                            Text(d, style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                color = colors.subtitle.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    value:    String,
    label:    String,
    colors:   AppColors,
    modifier: Modifier = Modifier,
    content:  @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value,
            style      = MaterialTheme.typography.titleLarge,
            color      = colors.title,
            fontWeight = FontWeight.Bold,
        )
        content?.invoke()
        Spacer(Modifier.height(2.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
            color = colors.subtitle,
        )
    }
}

@Composable
private fun StatDelta(pct: Float, colors: AppColors) {
    val positive = pct >= 0
    val color    = if (positive) Color(0xFF4AE8A0) else Color(0xFFFF8A8A)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            "${if (positive) "+" else ""}${pct.toInt()}% vs 7d",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = color,
        )
    }
}

@Composable
private fun Sparkline(
    data:     List<Float>,
    color:    Color,
    fillArea: Boolean,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return
    val maxV = data.max().coerceAtLeast(0.01f)
    Canvas(modifier = modifier) {
        val pts = data.mapIndexed { i, v ->
            Offset(i.toFloat() / (data.size - 1) * size.width, size.height * (1f - v / maxV))
        }
        if (fillArea) {
            val fillPath = Path()
            fillPath.moveTo(pts.first().x, size.height)
            pts.forEach { fillPath.lineTo(it.x, it.y) }
            fillPath.lineTo(pts.last().x, size.height)
            fillPath.close()
            drawPath(fillPath, color.copy(alpha = 0.18f))
        }
        val linePath = Path()
        linePath.moveTo(pts.first().x, pts.first().y)
        pts.drop(1).forEach { linePath.lineTo(it.x, it.y) }
        drawPath(linePath, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── Mood Breakdown Section ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun MoodBreakdownSection(mood: MoodStats, colors: AppColors) {
    val green = Color(0xFF4AE8A0)
    val blue  = Color(0xFF4A9EFF)
    val red   = Color(0xFFFF8A8A)

    SectionLabel("MOOD OVERVIEW", colors)
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Card 1: Breakdown bars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom,
            ) {
                Column {
                    Text("${mood.positivePct}%",
                        style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.Bold)
                    Text("positive",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
                Text("${mood.total} entries",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
            }
            listOf(
                Triple("Positive", mood.positivePct to mood.positiveCount, green),
                Triple("Neutral",  mood.neutralPct  to mood.neutralCount,  blue),
                Triple("Tough",    mood.negativePct to mood.negativeCount,  red),
            ).forEach { (lbl, pctCount, col) ->
                val (pct, cnt) = pctCount
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(lbl,
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color    = col,
                        modifier = Modifier.width(48.dp))
                    Box(modifier = Modifier.weight(1f).height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.subtitle.copy(alpha = 0.18f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct / 100f)
                            .clip(RoundedCornerShape(3.dp)).background(col))
                    }
                    Text("$pct%",
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color    = col,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.End)
                    Text("($cnt)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = colors.subtitle,
                        modifier = Modifier.width(24.dp))
                }
            }
        }

        // Row: Avg score + Unique scores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Avg score card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${"%.1f".format(mood.avgScore)}/10",
                    style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.Bold)
                if (mood.topScoreCount > 0) {
                    Text("Most common: ${mood.topScore}/10 (${mood.topScoreCount}×)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
                Text("AVG MOOD",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = colors.subtitle)
                // Distribution mini bars
                val maxDist = mood.distribution.maxOrNull()?.coerceAtLeast(1) ?: 1
                Row(
                    modifier              = Modifier.fillMaxWidth().height(36.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment     = Alignment.Bottom,
                ) {
                    mood.distribution.forEachIndexed { i, cnt ->
                        val barColor = when {
                            i >= 7 -> green
                            i >= 3 -> blue
                            else   -> red
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(cnt.toFloat() / maxDist * 0.9f + 0.05f)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(barColor.copy(alpha = if (cnt == 0) 0.12f else 0.85f)),
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = colors.subtitle)
                    Text("10", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = colors.subtitle)
                }
            }

            // Unique scores card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${mood.uniqueScores}/10",
                    style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.Bold)
                Text("unique scores",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                Text("SCORE VARIETY",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = colors.subtitle)
                // Score dots 1-10
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1..5, 6..10).forEach { range ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            range.forEach { score ->
                                val has      = mood.distribution.getOrElse(score - 1) { 0 } > 0
                                val dotColor = when {
                                    score >= 8 -> green
                                    score >= 4 -> blue
                                    else       -> red
                                }
                                Box(
                                    modifier         = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (has) dotColor else colors.subtitle.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "$score",
                                        style     = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                        color     = if (has) Color.Black else colors.subtitle,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
