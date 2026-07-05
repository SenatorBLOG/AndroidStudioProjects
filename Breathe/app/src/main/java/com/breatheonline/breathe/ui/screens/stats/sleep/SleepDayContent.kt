package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAwake
import com.breatheonline.breathe.ui.theme.SleepDeep
import com.breatheonline.breathe.ui.theme.SleepLight
import com.breatheonline.breathe.ui.theme.SleepRem
import com.breatheonline.breathe.viewmodel.MetricTone
import com.breatheonline.breathe.viewmodel.NightMetric
import com.breatheonline.breathe.viewmodel.SleepDayView

@Composable
internal fun SleepDayContent(
    view: SleepDayView,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SleepSectionCard(
            icon = Icons.Filled.Hotel,
            title = "Night timeline",
            colors = colors,
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (view.stages.isNotEmpty()) {
                    "Track when you moved through light, deep, REM, and awake phases."
                } else {
                    "Detailed segments are missing, but the nightly schedule and stage totals are still usable."
                },
                color = colors.subtitle,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))
            SleepMetaRow(
                leftLabel = "Fell asleep",
                leftValue = view.bedtime,
                rightLabel = "Woke up",
                rightValue = view.wakeTime,
                colors = colors,
            )
            Spacer(Modifier.height(16.dp))
            if (view.stages.isNotEmpty()) {
                SleepHypnogram(
                    stages = view.stages,
                    bedtime = view.bedtime,
                    wakeTime = view.wakeTime,
                    colors = colors,
                )
            } else {
                SleepStageFallbackBar(view = view, colors = colors)
            }
        }

        SleepSectionCard(
            icon = Icons.Filled.PieChart,
            title = "Sleep stages",
            colors = colors,
        ) {
            Spacer(Modifier.height(10.dp))
            SleepStagesDonut(
                totals = view.stageTotals,
                centerText = "${view.durationMin / 60}h ${view.durationMin % 60}m",
                colors = colors,
            )
            Spacer(Modifier.height(12.dp))
            SleepStagesBreakdown(
                totals = view.stageTotals,
                colors = colors,
            )
            Spacer(Modifier.height(12.dp))
            SleepRecoveryCallout(view = view, colors = colors)
        }

        SleepSectionCard(
            icon = Icons.Filled.MonitorHeart,
            title = "Night metrics",
            colors = colors,
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Modeled from tonight's duration, stage balance, awake time, and sleeping HR.",
                color = colors.subtitle,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(14.dp))
            NightMetricsSummary(view.nightMetrics, colors)
            Spacer(Modifier.height(12.dp))
            NightMetricsGrid(view.nightMetrics, colors)
        }
    }
}

@Composable
private fun SleepMetaRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    colors: AppColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetaPill(
            label = leftLabel,
            value = leftValue,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        MetaPill(
            label = rightLabel,
            value = rightValue,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetaPill(
    label: String,
    value: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = label, color = colors.subtitle, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = colors.title, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SleepStageFallbackBar(
    view: SleepDayView,
    colors: AppColors,
) {
    val total = view.stageTotals.totalMin.coerceAtLeast(1)
    val segments = listOf(
        Triple("Deep", view.stageTotals.deepMin, SleepDeep),
        Triple("Light", view.stageTotals.lightMin, SleepLight),
        Triple("REM", view.stageTotals.remMin, SleepRem),
        Triple("Awake", view.stageTotals.awakeMin, SleepAwake),
    ).filter { it.second > 0 }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(999.dp)),
        ) {
            segments.forEach { (_, minutes, color) ->
                Box(
                    modifier = Modifier
                        .weight(minutes.toFloat() / total)
                        .height(20.dp)
                        .background(color),
                )
            }
        }
        Text(
            text = "Stage summary fallback. Connect a provider with detailed sleep segments to see the full hypnogram.",
            color = colors.subtitle,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SleepRecoveryCallout(
    view: SleepDayView,
    colors: AppColors,
) {
    val total = view.durationMin.coerceAtLeast(1)
    val dominantStage = listOf(
        "Deep sleep" to view.stageTotals.deepMin,
        "Light sleep" to view.stageTotals.lightMin,
        "REM sleep" to view.stageTotals.remMin,
    ).maxByOrNull { it.second }

    val message = when {
        view.stageTotals.deepMin < (total * 0.12f).toInt() ->
            "Deep sleep ran short tonight. Ease off stimulation late in the evening and protect the first sleep cycle."
        view.stageTotals.remMin < (total * 0.18f).toInt() ->
            "REM looked compressed. A steadier sleep window can help cognitive recovery and dream-rich sleep."
        dominantStage != null ->
            "${dominantStage.first} dominated the night. That usually means your body had enough uninterrupted time to settle."
        else ->
            "Not enough stage detail to interpret the recovery pattern yet."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "Recovery note",
            color = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            color = colors.subtitle,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun NightMetricsSummary(
    metrics: List<NightMetric>,
    colors: AppColors,
) {
    val best = metrics.filter { it.tone == MetricTone.POSITIVE }.maxByOrNull { metricPriority(it) }
    val watch = metrics.filter { it.tone == MetricTone.CAUTION }.maxByOrNull { metricPriority(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (best != null) {
            SummaryMetricLine(
                prefix = "Strongest signal",
                metric = best,
                colors = colors,
            )
        }
        if (watch != null) {
            SummaryMetricLine(
                prefix = "Worth watching",
                metric = watch,
                colors = colors,
            )
        }
    }
}

@Composable
private fun SummaryMetricLine(
    prefix: String,
    metric: NightMetric,
    colors: AppColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prefix,
                color = colors.subtitle,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${metric.label}: ${metric.status}",
                color = colors.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TonePill(metric.tone, metric.value)
    }
}

@Composable
private fun NightMetricsGrid(
    metrics: List<NightMetric>,
    colors: AppColors,
) {
    metrics.chunked(2).forEachIndexed { rowIndex, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { metric ->
                ExpandableMetricCard(
                    metric = metric,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) {
                Spacer(Modifier.weight(1f))
            }
        }
        if (rowIndex != metrics.lastIndex / 2) {
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ExpandableMetricCard(
    metric: NightMetric,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val accent = toneColor(metric.tone)

    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.label,
                    color = colors.subtitle,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = metric.value,
                    color = colors.title,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = metric.status,
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            ToneDot(accent)
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = metric.supporting,
                color = colors.subtitle,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun TonePill(
    tone: MetricTone,
    value: String,
) {
    val accent = toneColor(tone)
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = value,
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ToneDot(accent: Color) {
    Box(
        modifier = Modifier
            .width(10.dp)
            .height(10.dp)
            .background(accent, RoundedCornerShape(999.dp)),
    )
}

private fun toneColor(tone: MetricTone): Color = when (tone) {
    MetricTone.POSITIVE -> SleepRem
    MetricTone.NEUTRAL -> SleepLight
    MetricTone.CAUTION -> Color(0xFFFFB74D)
    MetricTone.MUTED -> Color(0xFF9AA4B2)
}

private fun metricPriority(metric: NightMetric): Int = when (metric.label) {
    "Recovery" -> 7
    "Sleep efficiency" -> 6
    "Duration vs target" -> 5
    "Continuity" -> 4
    "Deep sleep" -> 3
    "REM balance" -> 2
    "Sleeping HR" -> 1
    else -> 0
}
