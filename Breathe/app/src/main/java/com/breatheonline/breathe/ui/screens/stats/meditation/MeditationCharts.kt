package com.breatheonline.breathe.ui.screens.stats.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.DayMinutes
import com.breatheonline.breathe.viewmodel.StatsState
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Activity chart ─────────────────────────────────────────────────────────────

@Composable
internal fun ActivityChart(
    data:   List<DayMinutes>,
    period: Int,
    colors: AppColors,
) {
    if (data.isEmpty()) return

    val labels = data.mapIndexed { i, dm ->
        when (period) {
            2    -> dm.date.format(DateTimeFormatter.ofPattern("MMM"))
            1    -> if (i % 5 == 0) dm.date.dayOfMonth.toString() else ""
            else -> dm.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
        }
    }
    val subLabels = if (period == 0) data.map { dm -> if (dm.minutes > 0) "${dm.minutes}m" else "" } else null

    MeditationBarChart(
        values    = data.map { it.minutes.toFloat() },
        labels    = labels,
        subLabels = subLabels,
        colors    = colors,
    )
}

// ── Bar chart (meditation-specific, mirrors StatBarChart) ─────────────────────

@Composable
internal fun MeditationBarChart(
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
        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = bottomPad)) {
            val n        = values.size.coerceAtLeast(1)
            val areaW    = size.width / n
            val barW     = (areaW * 0.45f).coerceAtLeast(6f)
            val chartH   = size.height
            val gridColor = colors.subtitle.copy(alpha = 0.12f)

            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(gridColor, Offset(0f, chartH * (1f - r)), Offset(size.width, chartH * (1f - r)), 1.dp.toPx())
            }
            values.forEachIndexed { i, v ->
                if (v > 0f) {
                    val barH = (v / maxVal) * chartH * 0.88f
                    val x    = i * areaW + (areaW - barW) / 2f
                    drawRoundRect(
                        color        = colors.primary,
                        topLeft      = Offset(x, chartH - barH),
                        size         = Size(barW, barH),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                    )
                }
            }
        }
        // X-axis labels
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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ── Sparkline ─────────────────────────────────────────────────────────────────

@Composable
internal fun Sparkline(
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

// ── Annual Progress Chart ─────────────────────────────────────────────────────

@Composable
internal fun AnnualProgressSection(state: StatsState, colors: AppColors) {
    var byYear by remember { mutableStateOf(false) }
    val data   = if (byYear) state.allYearsData else state.annualMonthData

    MeditationSectionLabel("ANNUAL PROGRESS", colors)
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
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 22.dp)) {
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
internal fun AnnualViewToggle(byYear: Boolean, onToggle: (Boolean) -> Unit, colors: AppColors) {
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

// ── Section label (internal helper) ───────────────────────────────────────────

@Composable
internal fun MeditationSectionLabel(text: String, colors: AppColors) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = colors.subtitle,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp),
    )
}
