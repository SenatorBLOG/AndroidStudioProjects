package com.breatheonline.breathe.ui.screens.stats.meditation

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.NlpInsights
import com.breatheonline.breathe.data.models.NlpTimelinePoint
import com.breatheonline.breathe.ui.screens.AchievementHighlightsSection
import com.breatheonline.breathe.ui.screens.Route
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.InsightCard
import com.breatheonline.breathe.viewmodel.StatsState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Meditation tab root composable ────────────────────────────────────────────

@Composable
internal fun MeditationStatsContent(
    state: StatsState,
    period: Int,
    onPeriodSelect: (Int) -> Unit,
    colors: AppColors,
    navController: NavController,
) {
    val chartData = when (period) {
        1    -> state.monthlyData
        2    -> state.yearlyData
        else -> state.weeklyData
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Period tabs ───────────────────────────────────────────────────
        item {
            MeditationPeriodTabs(selected = period, onSelect = onPeriodSelect, colors = colors)
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
                MeditationSectionLabel("HEALTH DATA", colors)
                HealthPanel(health = health, colors = colors)
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            AchievementHighlightsSection(
                colors = colors,
                navController = navController,
            )
            Spacer(Modifier.height(20.dp))
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
            item { MeditationSectionLabel("AI INSIGHTS", colors) }
            items(state.insights) { card ->
                InsightCardRow(card = card, colors = colors)
                Spacer(Modifier.height(10.dp))
            }
        }

        // ── Emotional Intelligence ────────────────────────────────────────
        val nlp = state.nlpInsights
        if (nlp != null && nlp.totalAnalyzed > 0) {
            item {
                MeditationSectionLabel("EMOTIONAL INTELLIGENCE", colors)
                NlpSection(nlp = nlp, colors = colors, navController = navController)
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Quick links ───────────────────────────────────────────────────
        item {
            MeditationSectionLabel("EXPLORE", colors)
            QuickLinkRow(
                label    = stringResource(R.string.stats_session_history),
                onClick  = { navController.navigate(Route.SESSION_HISTORY) },
                colors   = colors,
            )
            Spacer(Modifier.height(10.dp))
            QuickLinkRow(
                label    = stringResource(R.string.stats_meditation_regularity),
                onClick  = { navController.navigate(Route.MEDITATION_REGULARITY) },
                colors   = colors,
            )
            Spacer(Modifier.height(10.dp))
            QuickLinkRow(
                label    = stringResource(R.string.stats_health_data),
                onClick  = { navController.navigate(Route.HEALTH_STATS) },
                colors   = colors,
            )
            Spacer(Modifier.height(10.dp))
            QuickLinkRow(
                label    = stringResource(R.string.stats_achievements),
                onClick  = { navController.navigate(Route.ACHIEVEMENTS) },
                colors   = colors,
            )
            Spacer(Modifier.height(10.dp))
            QuickLinkRow(
                label    = stringResource(R.string.stats_emotional_journal),
                onClick  = { navController.navigate(Route.JOURNAL) },
                colors   = colors,
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Period tabs (meditation-specific) ─────────────────────────────────────────

@Composable
internal fun MeditationPeriodTabs(selected: Int, onSelect: (Int) -> Unit, colors: AppColors) {
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold.takeIf { active }
                        ?: androidx.compose.ui.text.font.FontWeight.Normal,
                )
            }
        }
    }
}

// ── AI Insight Card Row ───────────────────────────────────────────────────────

@Composable
internal fun InsightCardRow(card: InsightCard, colors: AppColors) {
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
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
internal fun QuickLinkRow(label: String, onClick: () -> Unit, colors: AppColors) {
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

// ── NLP Section ───────────────────────────────────────────────────────────────

@Composable
internal fun NlpSection(
    nlp: NlpInsights,
    colors: AppColors,
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
        Text("Emotional score", style = MaterialTheme.typography.labelMedium, color = colors.title,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Text("−1 negative  →  +1 positive", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)

        Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            val n    = timeline.size
            val midY = size.height / 2f
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, r * size.height), androidx.compose.ui.geometry.Offset(size.width, r * size.height), 1.dp.toPx())
            }
            drawLine(
                color       = accent.copy(alpha = 0.25f),
                start       = androidx.compose.ui.geometry.Offset(0f, midY),
                end         = androidx.compose.ui.geometry.Offset(size.width, midY),
                strokeWidth = 1.dp.toPx(),
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
            )
            val pts = timeline.mapIndexed { i, pt ->
                androidx.compose.ui.geometry.Offset(
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
        Text("Recurring themes", style = MaterialTheme.typography.labelMedium, color = colors.title,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)

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
