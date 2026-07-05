package com.breatheonline.breathe.ui.screens.stats.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.DurationBuckets
import com.breatheonline.breathe.viewmodel.MoodPoint
import com.breatheonline.breathe.viewmodel.MoodStats
import com.breatheonline.breathe.viewmodel.StatsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp

// ── Duration Donut Chart ──────────────────────────────────────────────────────

@Composable
internal fun DurationDonutSection(buckets: DurationBuckets, colors: AppColors) {
    if (buckets.total == 0) return

    val bucketColors = listOf(
        colors.primary,
        colors.primary.copy(alpha = 0.7f),
        Color(0xFF4AE8A0),
        Color(0xFF4AE8A0).copy(alpha = 0.55f),
    )
    val labels   = listOf("Short ≤5m", "Medium 6-15m", "Long 16-30m", "Extended 30+m")
    val counts   = listOf(buckets.short, buckets.medium, buckets.long, buckets.extended)
    val total    = buckets.total.toFloat()
    // Use precomputed actual average; fall back to bucket-midpoint estimate only if unavailable
    val avgMins  = if (buckets.avgMinutes > 0f) buckets.avgMinutes
    else listOf(2.5f, 10f, 22.5f, 45f).zip(counts) { mid, cnt -> mid * cnt }.sum() / total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Session duration",
            style = MaterialTheme.typography.titleSmall,
            color = colors.title,
        )
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
            Text(stringResource(R.string.meditation_sessions_total, buckets.total),
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

// ── Stats Cards Grid ──────────────────────────────────────────────────────────

@Composable
internal fun StatsCardsGrid(state: StatsState, colors: AppColors) {
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
        Text(
            text = "This week",
            style = MaterialTheme.typography.titleSmall,
            color = colors.title,
            modifier = Modifier.padding(bottom = 2.dp),
        )
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
internal fun MiniStatCard(
    value:    String,
    label:    String,
    colors: AppColors,
    modifier: Modifier = Modifier,
    content:  @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
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
internal fun StatDelta(pct: Float, colors: AppColors) {
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

// ── Mood Grid ─────────────────────────────────────────────────────────────────

@Composable
internal fun MoodGrid(points: List<MoodPoint>, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Mood lift",
            style = MaterialTheme.typography.titleSmall,
            color = colors.title,
            modifier = Modifier.padding(bottom = 10.dp),
        )

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
            Text("  ${stringResource(R.string.meditation_trend_improved)}  ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
            LegendDot(lerp(Color(0xFFD32F2F), Color(0xFFEF9A9A), 0.3f))
            Text("  ${stringResource(R.string.meditation_trend_decreased)}  ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
            LegendDot(colors.subtitle.copy(alpha = 0.20f))
            Text("  ${stringResource(R.string.meditation_no_data_label)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.subtitle)
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

// ── Mood Breakdown Section ────────────────────────────────────────────────────

@Composable
internal fun MoodBreakdownSection(mood: MoodStats, colors: AppColors) {
    val green = Color(0xFF4AE8A0)
    val blue  = Color(0xFF4A9EFF)
    val red   = Color(0xFFFF8A8A)

    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Mood overview",
            style = MaterialTheme.typography.titleSmall,
            color = colors.title,
            modifier = Modifier.padding(bottom = 2.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
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
                    Text(stringResource(R.string.meditation_positive_label),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
                Text(stringResource(R.string.meditation_entries_count, mood.total),
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

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${"%.1f".format(mood.avgScore)}/10",
                    style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.Bold)
                if (mood.topScoreCount > 0) {
                    Text(stringResource(R.string.meditation_most_common, mood.topScore, mood.topScoreCount),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                }
                Text(stringResource(R.string.meditation_avg_mood),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = colors.subtitle)
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${mood.uniqueScores}/10",
                    style = MaterialTheme.typography.titleLarge, color = colors.title, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.meditation_unique_scores),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
                Text(stringResource(R.string.meditation_score_variety),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = colors.subtitle)
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
