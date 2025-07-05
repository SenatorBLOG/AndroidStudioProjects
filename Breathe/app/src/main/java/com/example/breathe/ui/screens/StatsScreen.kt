package com.example.breathe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.data.models.MeditationSession
import com.example.breathe.ui.theme.*
import com.example.breathe.viewmodel.StatsState
import com.example.breathe.viewmodel.StatsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Constants
private const val PERIOD_7_DAYS = "7 days"
private const val PERIOD_30_DAYS = "30 days"
private const val MAX_DEFAULT_DAYS = 90
private const val BAR_SPACING_RATIO = 1.5f
private const val GRAPH_CONTENT_HEIGHT_SCALE = 0.8f
private val HorizontalPadding = 48.dp
private val GraphVerticalPadding = 16.dp
private val GraphBottomLabelHeight = 32.dp

data class DailyMeditationSummary(
    val date: LocalDate,
    val totalDurationMinutes: Float,
    val firstSessionEpochMilli: Long?,
    val lastSessionEndEpochMilli: Long?
) {
    val formattedDate: String by lazy { date.format(DateTimeFormatter.ofPattern("MMM d")) }

    fun formatStartTime(pattern: String = "HH:mm"): String =
        firstSessionEpochMilli?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
                .format(DateTimeFormatter.ofPattern(pattern))
        } ?: "N/A"

    fun formatEndTime(pattern: String = "HH:mm"): String =
        lastSessionEndEpochMilli?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
                .format(DateTimeFormatter.ofPattern(pattern))
        } ?: "N/A"
}

fun toLocalDate(timestamp: Long): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * Displays the statistics screen with a graph, KPI cards, and session history.
 * @param colors Theme colors for UI customization.
 * @param navController Navigation controller for screen transitions.
 */
@Composable
fun StatsScreen(colors: AppColors, navController: NavController, modifier: Modifier) {
    val viewModel: StatsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    var selectedPeriod by remember { mutableStateOf(PERIOD_7_DAYS) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showKpiDialog by remember { mutableStateOf<String?>(null) }
    var graphScale by remember { mutableStateOf(1f) }
    var graphOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var selectedBar by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Statistics",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Meditation Progress",
                color = colors.subtitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Period Selector
            Spacer(modifier = Modifier.height(16.dp))
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                colors = colors
            )

            // Interactive Graph
            Spacer(modifier = Modifier.height(16.dp))
            MeditationGraph(
                sessions = sessions,
                period = selectedPeriod,
                colors = colors,
                scale = graphScale,
                offset = graphOffset,
                selectedBar = selectedBar,
                onScaleChange = { scale, offset ->
                    graphScale = scale.coerceIn(0.5f, 3f)
                    graphOffset = offset
                },
                onBarSelected = { selectedBar = it },
                navController = navController
            )

            // KPI Cards
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp).weight(1f, fill = true)
            ) {
                StatCard(
                    title = "Total\nMeditation",
                    value = viewModel.formatMinutesToClock(state.totalMeditationMinutes),
                    onClick = { showKpiDialog = "Total Meditation" },
                    colors = colors
                )
                StatCard(
                    title = "Best\nStreak",
                    value = "${state.bestStreakDays} days",
                    onClick = { showKpiDialog = "Best Streak" },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            StatCard(
                title = "Sessions\nThis Week",
                value = "${state.sessionsThisWeek} sessions",
                onClick = { showKpiDialog = "Sessions This Week" },
                colors = colors
            )

            // Session History
            Spacer(modifier = Modifier.height(24.dp))
            SessionHistory(
                sessions = sessions,
                colors = colors,
                period = selectedPeriod
            )

            // Add Session Button
            FloatingActionButton(
                onClick = { showAddSessionDialog = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
                containerColor = colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session", tint = colors.title)
            }
        }

        // Dialogs
        if (showAddSessionDialog) {
            AddSessionDialog(
                onDismiss = { showAddSessionDialog = false },
                onConfirm = { duration ->
                    val nowMillis = ZonedDateTime.now(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli()
                    viewModel.saveSession(duration * 60, nowMillis)
                    showAddSessionDialog = false
                },
                colors = colors
            )
        }
        showKpiDialog?.let { metric ->
            KpiDetailDialog(
                metric = metric,
                state = state,
                colors = colors,
                onDismiss = { showKpiDialog = null }
            )
        }
    }
}

/**
 * Dropdown menu for selecting the time period for statistics.
 */
@Composable
fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    colors: AppColors
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.title)
        ) {
            Text(selectedPeriod, style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(PERIOD_7_DAYS, PERIOD_30_DAYS, "All time").forEach { period ->
                DropdownMenuItem(
                    text = { Text(period, color = colors.text) },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}



/**
 * Interactive graph displaying daily meditation minutes.
 * [START GRAPH SECTION]
 */
@Composable
fun MeditationGraph(
    sessions: List<MeditationSession>,
    period: String,
    colors: AppColors,
    scale: Float,
    offset: Offset,
    selectedBar: Int?,
    onScaleChange: (Float, Offset) -> Unit,
    onBarSelected: (Int?) -> Unit,
    navController: NavController
) {
    val numberOfDays = when (period) {
        PERIOD_7_DAYS -> 7
        PERIOD_30_DAYS -> 30
        else -> sessions.size.coerceAtMost(MAX_DEFAULT_DAYS).coerceAtLeast(1)
    }

    val now = ZonedDateTime.now(ZoneId.of("America/Los_Angeles")).toLocalDate()

    val dateRange = remember(numberOfDays) {
        (0 until numberOfDays).map { now.minusDays(it.toLong()) }.reversed()
    }


    val dailySummaries = remember(sessions, dateRange) {
        dateRange.map { date ->
            val sessionsOnDate = sessions.filter { session ->
                val sessionDate = toLocalDate(session.date)
                android.util.Log.d("MeditationGraph", "Session date: $sessionDate, Target date: $date")
                sessionDate == date
            }
            val totalDuration = sessionsOnDate.sumOf { it.duration.toLong() }.toFloat() / 60f
            val firstSession = sessionsOnDate.minByOrNull { it.date }
            val lastSession = sessionsOnDate.maxByOrNull { it.date }
            val firstSessionEpochMilli = firstSession?.date
            val lastSessionEndEpochMilli = lastSession?.let { it.date + (it.duration * 1000L) }
            DailyMeditationSummary(date, totalDuration, firstSessionEpochMilli, lastSessionEndEpochMilli).also {
                android.util.Log.d("MeditationGraph", "Date: ${it.date}, Duration: ${it.totalDurationMinutes}, Sessions: ${sessionsOnDate.size}")
            }
        }
    }

    val maxDurationDisplay = remember(dailySummaries) {
        (dailySummaries.maxOfOrNull { it.totalDurationMinutes } ?: 0f).coerceAtLeast(1f)
    }

    val density = LocalDensity.current
    var graphCanvasSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.5f, 3f)
                    onScaleChange(newScale, offset + pan)
                }
            }
    ) {
        // Time scale on the left
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .padding(vertical = GraphVerticalPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val timeSteps = listOf(60, 45, 30, 15, 0)
            timeSteps.forEach { time ->
                Text(
                    text = "$time",
                    color = colors.text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Определяем переменные до Canvas
        val barAreaWidthPx = remember(graphCanvasSize.width, numberOfDays) {
            if (graphCanvasSize.width > 0f && numberOfDays > 0) graphCanvasSize.width / numberOfDays else 0f
        }
        val canvasActualHeight = graphCanvasSize.height // Используем сохраненный размер

        // Graph Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = HorizontalPadding, end = GraphVerticalPadding, top = GraphVerticalPadding, bottom = GraphBottomLabelHeight)
                .onSizeChanged { graphCanvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
        ) {
            val canvasActualWidth = size.width
            val barWidthPx = barAreaWidthPx / BAR_SPACING_RATIO
            val barSpacingPx = barAreaWidthPx - barWidthPx

            dailySummaries.forEachIndexed { index, summary ->
                val barHeight = (summary.totalDurationMinutes / maxDurationDisplay * canvasActualHeight * GRAPH_CONTENT_HEIGHT_SCALE) * scale
                val x = index * barAreaWidthPx + (barSpacingPx / 2) + offset.x

                if (x + barWidthPx > 0 && x < canvasActualWidth) {
                    drawPath(
                        path = Path().apply {
                            moveTo(x, size.height)
                            lineTo(x, size.height - barHeight)
                            lineTo(x + barWidthPx, size.height - barHeight)
                            lineTo(x + barWidthPx, size.height)
                            close()
                        },
                        color = if (selectedBar == index) colors.primary.copy(alpha = 0.8f) else colors.primary
                    )
                }
            }
        }

        // Day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HorizontalPadding, end = GraphVerticalPadding, bottom = 8.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val barAreaWidthDp = with(density) { (graphCanvasSize.width / numberOfDays).toDp() }
            dailySummaries.forEach { summary ->
                val dayLabel = if (period == PERIOD_7_DAYS) {
                    summary.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                } else {
                    summary.date.dayOfMonth.toString()
                }
                Text(
                    text = dayLabel,
                    color = colors.text,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(barAreaWidthDp)
                )
            }
        }

        // Tooltip for selected bar
        selectedBar?.let { index ->
            if (index < dailySummaries.size) {
                val summary = dailySummaries[index]
                val barCenterX = (index * barAreaWidthPx + barAreaWidthPx / 2 + offset.x)
                    .coerceIn(0f, graphCanvasSize.width - 100f)
                val barTopY = canvasActualHeight - (summary.totalDurationMinutes / maxDurationDisplay * canvasActualHeight * GRAPH_CONTENT_HEIGHT_SCALE) * scale
                val tooltipY = (barTopY - 40f).coerceIn(0f, canvasActualHeight - 50f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset((barCenterX - 50).toInt(), tooltipY.toInt()) }
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "${summary.formattedDate}: ${"%.1f".format(summary.totalDurationMinutes)} min",
                            color = colors.text,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Start: ${summary.formatStartTime()}",
                            color = colors.text,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "End: ${summary.formatEndTime()}",
                            color = colors.text,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Link to Meditation Regularity
        Text(
            text = "View Meditation Regularity",
            color = colors.primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clickable { navController.navigate("meditation_regularity") }
        )
    }
}
/* [END GRAPH SECTION] */

/**
 * Reusable card for displaying KPI statistics.
 */
@Composable
fun StatCard(title: String, value: String, onClick: () -> Unit, colors: AppColors) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = colors.value,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * LazyColumn displaying meditation session history.
 * [START TABLE SECTION]
 */
@Composable
fun SessionHistory(sessions: List<MeditationSession>, colors: AppColors, period: String) {
    val filteredSessions = when (period) {
        PERIOD_7_DAYS -> sessions.filter { toLocalDate(it.date) >= LocalDate.now().minusDays(7) }
        PERIOD_30_DAYS -> sessions.filter { toLocalDate(it.date) >= LocalDate.now().minusDays(30) }
        else -> sessions
    }
    Column {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Date",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Duration",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filteredSessions) { session ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SessionCard(session, colors)
                }
            }
        }
    }
}
/* [END TABLE SECTION] */

/**
 * Card displaying a single meditation session.
 */
@Composable
fun SessionCard(session: MeditationSession, colors: AppColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = toLocalDate(session.date).format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${session.duration / 60} min",
                color = colors.value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Dialog for adding a new meditation session.
 */
@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    colors: AppColors
) {
    var duration by remember { mutableStateOf("5") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Meditation Session", style = MaterialTheme.typography.titleSmall, color = colors.title) },
        text = {
            Column {
                Text("Duration (minutes)", color = colors.text)
                TextField(
                    value = duration,
                    onValueChange = { duration = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                duration.toLongOrNull()?.let { if (it > 0) onConfirm(it) }
            }) {
                Text("Confirm", color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.text)
            }
        },
        containerColor = colors.background
    )
}

/**
 * Dialog displaying detailed KPI statistics.
 */
@Composable
fun KpiDetailDialog(
    metric: String,
    state: StatsState,
    colors: AppColors,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(metric, style = MaterialTheme.typography.titleSmall, color = colors.title) },
        text = {
            when (metric) {
                "Total Meditation" -> {
                    Text(
                        "Total: ${state.totalMeditationMinutes} minutes",
                        color = colors.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                "Best Streak" -> {
                    Text(
                        "Best Streak: ${state.bestStreakDays} days",
                        color = colors.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                "Sessions This Week" -> {
                    Text(
                        "This Week: ${state.sessionsThisWeek} sessions",
                        color = colors.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.text)
            }
        },
        containerColor = colors.background
    )
}