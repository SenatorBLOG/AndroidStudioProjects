package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import com.breatheonline.breathe.ui.components.ShimmerCardList
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.JournalSession
import com.breatheonline.breathe.data.models.NlpData
import com.breatheonline.breathe.data.models.NlpInsights
import com.breatheonline.breathe.data.models.SentimentDist
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.JournalViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Sentiment helpers ─────────────────────────────────────────────────────────

private val NegativeRed  = Color(0xFFFF8A8A)
private val NeutralBlue  = Color(0xFF7AAEC8)

private fun sentimentColor(s: String?, accent: Color): Color = when (s) {
    "positive" -> accent
    "negative" -> NegativeRed
    else       -> NeutralBlue
}

private fun sentimentEmoji(s: String?) = when (s) {
    "positive" -> "😌"
    "negative" -> "😟"
    else       -> "😐"
}

@Composable
private fun avgLabel(score: Double?) = when {
    score == null        -> null
    score > 0.2          -> stringResource(R.string.journal_mood_generally_positive)
    score < -0.2         -> stringResource(R.string.journal_mood_challenging)
    else                 -> stringResource(R.string.journal_mood_balanced)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun JournalScreen(
    colors: AppColors,
    navController: NavController,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.subtitle, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.journal_title), style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.journal_subtitle), style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
            }
            if (!state.isLoading) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                        .clickable { viewModel.load() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Refresh, null, tint = colors.subtitle, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── Scrollable body ───────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val capturedInsights = state.insights
            when {
                state.isLoading -> {
                    ShimmerCardList(rows = 5, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }

                state.error != null && capturedInsights == null -> {
                    ErrorCard(colors = colors, onRetry = { viewModel.load() })
                }

                capturedInsights != null -> {
                    val insights = capturedInsights
                    val sessions = insights.sessions

                    // Overview card
                    if (insights.totalAnalyzed > 0) {
                        OverviewCard(insights = insights, colors = colors)
                    }

                    // Empty state
                    if (sessions.isEmpty()) {
                        EmptyJournalCard(colors = colors, navController = navController)
                    } else {
                        // Grouped entries
                        val grouped = remember(sessions) { groupByMonth(sessions) }
                        grouped.forEach { (month, entries) ->
                            MonthGroup(
                                month   = month,
                                entries = entries,
                                colors  = colors,
                                navController = navController,
                            )
                        }

                        // Tip
                        TipRow(colors = colors)
                    }
                }

                else -> {
                    EmptyJournalCard(colors = colors, navController = navController)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Overview card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewCard(insights: NlpInsights, colors: AppColors) {
    val label = avgLabel(insights.avgScore)
    val sentScore = insights.avgScore
    val sentStr = when {
        sentScore == null  -> null
        sentScore > 0.2    -> "positive"
        sentScore < -0.2   -> "negative"
        else               -> "neutral"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.AutoAwesome, null, tint = colors.primary, modifier = Modifier.size(14.dp))
            Text(
                stringResource(R.string.journal_30_day_overview),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 1.sp,
            )
        }

        // 3 metrics
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCell("${insights.totalAnalyzed}", stringResource(R.string.journal_analyzed), colors, Modifier.weight(1f))
            if (label != null) {
                MetricCell(label, stringResource(R.string.journal_overall_mood), colors, Modifier.weight(1.4f),
                    valueColor = sentimentColor(sentStr, colors.primary))
            }
            MetricCell("${insights.sentimentDist.positive}", stringResource(R.string.journal_positive_pct), colors, Modifier.weight(1f),
                valueColor = colors.primary)
        }

        // Top themes
        if (insights.topThemes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.journal_recurring_themes), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 0.5.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    insights.topThemes.forEach { (theme, count) ->
                        ThemeChip(text = "$theme · $count", accent = colors.primary)
                    }
                }
            }
        }

        // Sentiment distribution bar
        if (insights.totalAnalyzed > 0) {
            SentimentBar(dist = insights.sentimentDist, accent = colors.primary)
        }
    }
}

@Composable
private fun MetricCell(
    value:      String,
    label:      String,
    colors: AppColors,
    modifier:   Modifier,
    valueColor: Color = colors.title,
) {
    Column(modifier.padding(vertical = 2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor, fontWeight = FontWeight.Light)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun SentimentBar(dist: SentimentDist, accent: Color) {
    val total = (dist.positive + dist.neutral + dist.negative).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (dist.positive > 0) Box(Modifier.weight(dist.positive.toFloat()).fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(accent))
            if (dist.neutral  > 0) Box(Modifier.weight(dist.neutral.toFloat()).fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(NeutralBlue))
            if (dist.negative > 0) Box(Modifier.weight(dist.negative.toFloat()).fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(NegativeRed))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot("Positive ${dist.positive}", accent)
            LegendDot("Neutral ${dist.neutral}",   NeutralBlue)
            LegendDot("Difficult ${dist.negative}", NegativeRed)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.85f))
    }
}

// ── Monthly group ─────────────────────────────────────────────────────────────

@Composable
private fun MonthGroup(
    month:         String,
    entries:       List<JournalSession>,
    colors: AppColors,
    navController: NavController,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Month header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(month, style = MaterialTheme.typography.labelSmall, color = colors.subtitle, fontWeight = FontWeight.Medium)
            Box(Modifier.weight(1f).height(1.dp).background(colors.subtitle.copy(alpha = 0.15f)))
            Text(
                "${entries.size} ${if (entries.size == 1) "entry" else "entries"}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle.copy(alpha = 0.5f),
            )
        }
        entries.forEach { session ->
            JournalEntry(session = session, colors = colors, navController = navController)
        }
    }
}

// ── Journal entry (accordion) ─────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntry(
    session: JournalSession,
    colors: AppColors,
    navController: NavController,
) {
    var expanded by remember { mutableStateOf(false) }
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, tween(260), label = "chevron")
    val nlp = session.nlp
    val moodDelta = (session.moodAfter ?: 0) - (session.moodBefore ?: 0)

    val dateStr = remember(session.sessionDate) {
        runCatching {
            val inst = Instant.parse(session.sessionDate)
            val zdt  = inst.atZone(ZoneId.systemDefault())
            val date = zdt.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
            val time = zdt.format(DateTimeFormatter.ofPattern("HH:mm"))
            date to time
        }.getOrDefault("" to "")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (expanded) colors.primary.copy(alpha = 0.04f) else colors.surface)
            .border(1.dp, if (expanded) colors.primary.copy(alpha = 0.25f) else colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .animateContentSize(tween(280)),
    ) {
        // ── Header row ────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Sentiment emoji
            Text(sentimentEmoji(nlp?.sentiment), fontSize = 24.sp, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Date row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "${dateStr.first} · ",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = colors.title,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(dateStr.second, style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                    Spacer(Modifier.weight(1f))
                    if (session.sessionLength > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(stringResource(R.string.journal_session_minutes, session.sessionLength.toInt()), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                        }
                    }
                    if (moodDelta > 0) {
                        Text(stringResource(R.string.journal_mood_delta, moodDelta), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                    }
                }

                // Notes excerpt
                if (!session.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        session.notes,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = colors.text.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // One-line summary preview (when collapsed)
                if (!expanded && nlp?.oneLineSummary != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        nlp.oneLineSummary,
                        style     = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color     = sentimentColor(nlp.sentiment, colors.primary),
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ExpandMore,
                null,
                tint     = if (expanded) colors.primary else colors.subtitle.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp).rotate(chevron).padding(top = 2.dp),
            )
        }

        // ── Expanded NLP details ──────────────────────────────────────────────
        if (expanded && nlp != null) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.subtitle.copy(alpha = 0.10f)))
            Column(
                modifier            = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // One-line summary
                if (nlp.oneLineSummary != null) {
                    Text(
                        "\"${nlp.oneLineSummary}\"",
                        style  = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color  = sentimentColor(nlp.sentiment, colors.primary),
                    )
                }

                // Score bar
                if (nlp.score != null) {
                    ScoreRow(nlp = nlp, colors = colors)
                }

                // Themes + intensity
                if (nlp.themes.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        nlp.themes.forEach { ThemeChip(text = it.replaceFirstChar(Char::uppercase), accent = colors.primary) }
                        if (nlp.intensity != null) {
                            ThemeChip(text = "intensity ${nlp.intensity}/10", accent = colors.subtitle)
                        }
                    }
                }

                // Suggested technique
                if (nlp.suggestedTechnique != null) {
                    TechniqueRow(technique = nlp.suggestedTechnique, colors = colors, navController = navController)
                }

                // Full notes
                if (!session.notes.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.subtitle.copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(stringResource(R.string.journal_your_notes), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 1.sp)
                        Text(session.notes, style = MaterialTheme.typography.bodySmall, color = colors.text.copy(alpha = 0.8f), lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(nlp: NlpData, colors: AppColors) {
    val score = nlp.score ?: return
    val barFraction = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)
    val barColor = sentimentColor(nlp.sentiment, colors.primary)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.journal_emotional_score), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 0.8.sp)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.subtitle.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor),
            )
        }
        Text(
            text       = "${if (score >= 0) "+" else ""}${String.format("%.2f", score)}",
            style      = MaterialTheme.typography.labelSmall,
            color      = barColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TechniqueRow(technique: String, colors: AppColors, navController: NavController) {
    val label = techniqueLabel(technique)
    val exerciseType = TECHNIQUE_TYPES[technique]

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.journal_recommended), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.text, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (exerciseType != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.12f))
                    .border(1.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                    .clickable { navController.navigate(Route.breathe(exerciseType)) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(stringResource(R.string.journal_try_it), style = MaterialTheme.typography.labelSmall, color = colors.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Utility composables ───────────────────────────────────────────────────────

@Composable
private fun ThemeChip(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = accent)
    }
}

@Composable
private fun TipRow(colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.06f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.AutoAwesome, null, tint = colors.primary, modifier = Modifier.size(12.dp).padding(top = 1.dp))
        Text(
            "The more you journal, the better the AI understands your emotional patterns and can recommend the right technique at the right time.",
            style      = MaterialTheme.typography.bodySmall,
            color      = colors.subtitle,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun EmptyJournalCard(colors: AppColors, navController: NavController) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.MenuBook, null, tint = colors.subtitle.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Text(stringResource(R.string.journal_empty_title), color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(R.string.journal_empty_message),
            style     = MaterialTheme.typography.bodySmall,
            color     = colors.subtitle,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier  = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary)
                .clickable { navController.navigate(Route.breathe()) }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(stringResource(R.string.journal_start_session), style = MaterialTheme.typography.bodySmall, color = colors.onPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorCard(colors: AppColors, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.journal_could_not_load), color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.journal_check_connection), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface)
                .border(1.dp, colors.subtitle.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(stringResource(R.string.btn_retry), style = MaterialTheme.typography.bodySmall, color = colors.subtitle)
        }
    }
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

@Composable
private fun techniqueLabel(key: String): String = when (key) {
    "box-breathing", "box"        -> stringResource(R.string.history_session_box)
    "4-7-8"                       -> stringResource(R.string.history_session_4_7_8)
    "wim-hof", "wimhof"           -> stringResource(R.string.history_session_wim_hof)
    "coherent"                    -> stringResource(R.string.history_session_coherent)
    "belly"                       -> stringResource(R.string.history_session_belly)
    "morning-ritual", "morning"   -> stringResource(R.string.history_session_morning)
    else                          -> key
}

private val TECHNIQUE_TYPES = mapOf(
    "box-breathing"  to "box",
    "box"            to "box",
    "4-7-8"          to "4-7-8",
    "wim-hof"        to "wimhof",
    "wimhof"         to "wimhof",
    "coherent"       to "coherent",
    "belly"          to "belly",
    "morning-ritual" to "morning",
    "morning"        to "morning",
)

private fun groupByMonth(sessions: List<JournalSession>): LinkedHashMap<String, List<JournalSession>> {
    val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val zone = ZoneId.systemDefault()
    val result = LinkedHashMap<String, MutableList<JournalSession>>()
    sessions.forEach { s ->
        val key = runCatching {
            Instant.parse(s.sessionDate).atZone(zone).format(fmt)
        }.getOrDefault("Unknown")
        result.getOrPut(key) { mutableListOf() }.add(s)
    }
    return result as LinkedHashMap<String, List<JournalSession>>
}
