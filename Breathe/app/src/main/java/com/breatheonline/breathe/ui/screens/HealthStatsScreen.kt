package com.breatheonline.breathe.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.HrDayDto
import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.utils.parseHealthDate
import com.breatheonline.breathe.viewmodel.HealthState
import com.breatheonline.breathe.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun HealthStatsScreen(
    colors:        AppColors,
    navController: NavController,
    viewModel:     HealthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = colors.primary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.health_stats_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.title,
                )
                val sourcesText = if (state.sources.isNotEmpty())
                    state.sources.joinToString(" · ") { sourceLabel(it) }
                else if (!state.isLoading)
                    stringResource(R.string.health_no_sources)
                else null
                if (sourcesText != null) {
                    Text(
                        text  = sourcesText,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle.copy(alpha = 0.7f),
                    )
                }
            }
            if (!state.isLoading) {
                IconButton(onClick = viewModel::refresh) {
                    Icon(
                        imageVector        = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.health_stats_refresh),
                        tint               = colors.subtitle,
                    )
                }
            }
        }

        when {
            state.isLoading -> LoadingView(colors)
            state.error != null -> ErrorView(state.error!!, colors)
            !state.hasData -> EmptyView(colors)
            else -> HealthContent(state = state, colors = colors)
        }
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
private fun HealthContent(state: HealthState, colors: AppColors) {
    val accentSleep = Color(0xFF7C4DFF)
    val accentHr    = Color(0xFFE53935)

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding      = androidx.compose.foundation.layout.PaddingValues(
            start  = 0.dp,
            end    = 0.dp,
            top    = 8.dp,
            bottom = 40.dp,
        ),
    ) {

        // ── Aggregate tiles ───────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.avgSleep7dMin?.let { mins ->
                    val h = mins / 60; val m = mins % 60
                    AggregateTile(
                        icon   = Icons.Default.Hotel,
                        label  = stringResource(R.string.health_avg_sleep),
                        value  = if (m > 0) "${h}h ${m}m" else "${h}h",
                        sub    = stringResource(R.string.health_last_7_days),
                        color  = accentSleep,
                        colors = colors,
                        mod    = Modifier.weight(1f),
                    )
                } ?: Spacer(Modifier.weight(1f))

                state.restingHr?.let { hr ->
                    AggregateTile(
                        icon   = Icons.Default.Favorite,
                        label  = stringResource(R.string.health_resting_hr),
                        value  = "$hr bpm",
                        sub    = stringResource(R.string.health_latest_reading),
                        color  = accentHr,
                        colors = colors,
                        mod    = Modifier.weight(1f),
                    )
                } ?: Spacer(Modifier.weight(1f))
            }
        }

        // Recovery tile (full width)
        if (state.recoveryScore != null) {
            item {
                RecoveryTile(
                    score  = state.recoveryScore,
                    colors = colors,
                    mod    = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                )
            }
        }

        // ── Sleep chart ───────────────────────────────────────────────────────
        if (state.sleepDays.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(R.string.health_sleep_duration_chart), colors = colors) {
                    SleepBarChart(days = state.sleepDays, accent = accentSleep, colors = colors)
                }
            }
        }

        // ── Heart rate chart ──────────────────────────────────────────────────
        val hrWithRate = state.heartRateDays.filter { (it.restingRate ?: it.avgRate) != null }
        if (hrWithRate.isNotEmpty()) {
            item {
                ChartSection(title = stringResource(R.string.health_resting_hr_chart), colors = colors) {
                    HrLineChart(days = hrWithRate, accent = accentHr, colors = colors)
                }
            }
        }
    }
}

// ── Sleep bar chart ────────────────────────────────────────────────────────────

@Composable
private fun SleepBarChart(days: List<SleepDayDto>, accent: Color, colors: AppColors) {
    val maxMin    = days.maxOf { it.duration }.coerceAtLeast(1)
    val gridColor = colors.subtitle.copy(alpha = 0.12f)

    val labels = days.map { formatChartDateLabel(it.date) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 28.dp),
        ) {
            val n      = days.size.coerceAtLeast(1)
            val areaW  = size.width / n
            val barW   = (areaW * 0.55f).coerceAtLeast(4f)
            val chartH = size.height

            // Grid lines at 25%, 50%, 75%, 100%
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(
                    color       = gridColor,
                    start       = Offset(0f, chartH * (1f - r)),
                    end         = Offset(size.width, chartH * (1f - r)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            days.forEachIndexed { i, day ->
                val barH = (day.duration.toFloat() / maxMin) * chartH * 0.85f
                val x    = i * areaW + (areaW - barW) / 2f
                drawRoundRect(
                    color        = accent,
                    topLeft      = Offset(x, chartH - barH),
                    size         = Size(barW, barH),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
        }

        // Duration labels above bars
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEach { d ->
                val h = d.duration / 60
                Text(
                    text      = "${h}h",
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color     = accent.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.width(26.dp),
                )
            }
        }

        // X-axis day labels
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 4.dp),
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

// ── HR line chart ──────────────────────────────────────────────────────────────

@Composable
private fun HrLineChart(days: List<HrDayDto>, accent: Color, colors: AppColors) {
    val values    = days.map { (it.restingRate ?: it.avgRate)!!.toFloat() }
    val minVal    = values.min()
    val maxVal    = values.max().coerceAtLeast(minVal + 1f)
    val gridColor = colors.subtitle.copy(alpha = 0.12f)

    val labels = days.map { formatChartDateLabel(it.date) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface),
    ) {
        val dotColor  = accent
        val lineColor = accent.copy(alpha = 0.7f)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 28.dp),
        ) {
            val n      = values.size.coerceAtLeast(1)
            val chartH = size.height
            val chartW = size.width
            val range  = maxVal - minVal

            // Grid
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { r ->
                drawLine(
                    color       = gridColor,
                    start       = Offset(0f, chartH * (1f - r)),
                    end         = Offset(chartW, chartH * (1f - r)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Compute point positions
            val pts = values.mapIndexed { i, v ->
                val x = if (n == 1) chartW / 2f else i * chartW / (n - 1f)
                val y = chartH - ((v - minVal) / range) * chartH * 0.85f
                Offset(x, y)
            }

            // Line path
            if (pts.size >= 2) {
                val path = Path()
                path.moveTo(pts.first().x, pts.first().y)
                for (k in 1 until pts.size) {
                    val prev = pts[k - 1]
                    val cur  = pts[k]
                    val cx   = (prev.x + cur.x) / 2f
                    path.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                }
                drawPath(
                    path   = path,
                    color  = lineColor,
                    style  = Stroke(
                        width      = 2.5.dp.toPx(),
                        cap        = StrokeCap.Round,
                        pathEffect = PathEffect.cornerPathEffect(8.dp.toPx()),
                    ),
                )
            }

            // Dots
            pts.forEach { pt ->
                drawCircle(color = colors.surface, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = dotColor, radius = 4.dp.toPx(), center = pt)
            }
        }

        // BPM labels above dots (top row)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            values.forEach { v ->
                Text(
                    text      = "${v.toInt()}",
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color     = accent.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.width(26.dp),
                )
            }
        }

        // X-axis labels
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 4.dp),
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

// ── Section wrapper ────────────────────────────────────────────────────────────

@Composable
private fun ChartSection(
    title:   String,
    colors:  AppColors,
    content: @Composable () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 1.sp,
        )
        content()
    }
}

// ── Aggregate tile ─────────────────────────────────────────────────────────────

@Composable
private fun AggregateTile(
    icon:   ImageVector,
    label:  String,
    value:  String,
    sub:    String,
    color:  Color,
    colors: AppColors,
    mod:    Modifier,
) {
    Column(
        modifier = mod
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = color,
                    modifier           = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text          = label,
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 1.sp,
            )
        }
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            color      = color,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = sub,
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle.copy(alpha = 0.6f),
        )
    }
}

// ── Recovery tile ──────────────────────────────────────────────────────────────

@Composable
private fun RecoveryTile(score: Int, colors: AppColors, mod: Modifier) {
    val color = when {
        score >= 70 -> Color(0xFF388E3C)
        score >= 40 -> Color(0xFFF57C00)
        else        -> Color(0xFFD32F2F)
    }
    val label = when {
        score >= 70 -> stringResource(R.string.health_great_recovery)
        score >= 40 -> stringResource(R.string.health_moderate_recovery)
        else        -> stringResource(R.string.health_rest_recommended)
    }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier              = mod
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.size(48.dp),
        ) {
            CircularProgressIndicator(
                progress    = { score / 100f },
                modifier    = Modifier.size(48.dp),
                color       = color,
                trackColor  = color.copy(alpha = 0.15f),
                strokeWidth = 5.dp,
            )
            Text(
                text       = "$score",
                style      = MaterialTheme.typography.titleSmall,
                color      = color,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text          = stringResource(R.string.health_recovery),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 1.sp,
            )
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                color      = colors.title,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── States ─────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView(colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun ErrorView(error: String, colors: AppColors) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = error,
            style     = MaterialTheme.typography.bodyMedium,
            color     = colors.subtitle,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyView(colors: AppColors) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("❤️", fontSize = 48.sp)
            Text(
                text      = stringResource(R.string.health_no_data_title),
                style     = MaterialTheme.typography.titleMedium,
                color     = colors.title,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = stringResource(R.string.health_no_data_message),
                style     = MaterialTheme.typography.bodySmall,
                color     = colors.subtitle,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun sourceLabel(provider: String) = when (provider) {
    "apple_health", "health_connect" -> "Health Connect"
    "fitbit"                         -> "Fitbit"
    "google_fit", "google-fit"       -> "Google Fit"
    else                             -> provider.replaceFirstChar { it.uppercase() }
}

private fun formatChartDateLabel(raw: String): String {
    val parsed = parseHealthDate(raw) ?: return raw.takeLast(5)
    return parsed.format(DateTimeFormatter.ofPattern("M/d"))
}
