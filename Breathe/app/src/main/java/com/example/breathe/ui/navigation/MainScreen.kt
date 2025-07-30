package com.example.breathe.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.breathe.ui.theme.*
import com.example.breathe.viewmodel.StatsViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Модели данных для дыхания
data class BreathingPattern(
    val inhale: Long,
    val holdAfterInhale: Long,
    val exhale: Long,
    val holdAfterExhale: Long
)

enum class Phase {
    INHALE, HOLD1, EXHALE, HOLD2
}

@Composable
fun MainScreen(colors: AppColors,
               onThemeChange: (String) -> Unit,
               modifier: Modifier) {
    var duration by remember { mutableStateOf("10 min") }
    var breathingPattern by remember { mutableStateOf("4-7-8") }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(0L) }
    val scale = remember { Animatable(1.0f) }
    var currentPhase by remember { mutableStateOf(Phase.INHALE) }
    val viewModel: StatsViewModel = hiltViewModel()
    val scrollState = rememberScrollState()

    val patterns = mapOf(
        "4-7-8" to BreathingPattern(4000, 7000, 8000, 0),
        "Box Breathing\n4-4-4-4" to BreathingPattern(4000, 4000, 4000, 4000)
    )

    val pattern = patterns[breathingPattern] ?: BreathingPattern(4000, 0, 4000, 0)

    // Анимация дыхания
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                currentPhase = Phase.INHALE
                scale.animateTo(1.5f, animationSpec = tween(pattern.inhale.toInt(), easing = LinearEasing))
                if (!isRunning) break
                currentPhase = Phase.HOLD1
                delay(pattern.holdAfterInhale)
                if (!isRunning) break
                currentPhase = Phase.EXHALE
                scale.animateTo(1.0f, animationSpec = tween(pattern.exhale.toInt(), easing = LinearEasing))
                if (!isRunning) break
                if (pattern.holdAfterExhale > 0) {
                    currentPhase = Phase.HOLD2
                    delay(pattern.holdAfterExhale)
                    if (!isRunning) break
                }
            }
        } else {
            scale.snapTo(1.0f)
            currentPhase = Phase.INHALE
        }
    }

    // Таймер длительности
    LaunchedEffect(isRunning) {
        if (isRunning) {
            val totalDurationMillis = when (duration) {
                "5 min" -> 5 * 60 * 1000L
                "10 min" -> 10 * 60 * 1000L
                "15 min" -> 15 * 60 * 1000L
                "20 min" -> 20 * 60 * 1000L
                else -> 10 * 60 * 1000L
            }
            remainingTime = totalDurationMillis
            val startTimeMillis = System.currentTimeMillis() // Capture start time

            while (remainingTime > 0 && isRunning) {
                val elapsedTime = System.currentTimeMillis() - startTimeMillis
                remainingTime = totalDurationMillis - elapsedTime
                if (remainingTime <= 0) {
                    isRunning = false
                    // Save session when timer ends
                    viewModel.saveSession(totalDurationMillis / 1000, System.currentTimeMillis())
                }
                delay(1000) // Update every second
            }
        } else {
            // When stopping manually
            if (remainingTime > 0) { // Only save if a session was actually running
                val completedDurationSeconds = (
                        when (duration) {
                            "5 min" -> 5 * 60 * 1000L
                            "10 min" -> 10 * 60 * 1000L
                            "15 min" -> 15 * 60 * 1000L
                            "20 min" -> 20 * 60 * 1000L
                            else -> 10 * 60 * 1000L
                        } - remainingTime
                        ) / 1000
                if (completedDurationSeconds > 0) { // Ensure positive duration
                    viewModel.saveSession(completedDurationSeconds, System.currentTimeMillis())
                }
            }
            remainingTime = 0L // Reset remaining time on stop
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Breathe Better",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Meditation for Sleep & Relaxation",
                color = colors.subtitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 0.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))

            // Круг дыхания
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .scale(scale.value)
                    .drawBehind {
                        val outerGlowRadius = size.width / 2 + 64.dp.toPx()
                        val outerGradient = Brush.radialGradient(
                            0f to colors.glowOuter,
                            1f to Color.Transparent,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = outerGlowRadius
                        )
                        drawCircle(brush = outerGradient, radius = outerGlowRadius)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(color = colors.background, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = colors.glowBackground, shape = CircleShape)
                            .drawBehind {
                                val innerGradient = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0f to colors.glowInner.copy(alpha = 0.05f),
                                        0.99f to colors.glowInner.copy(alpha = 0.6f),
                                        1f to colors.glowInner.copy(alpha = 0.85f)
                                    ),
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width / 2
                                )
                                drawCircle(brush = innerGradient, radius = size.width / 2)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentPhase) {
                                Phase.INHALE -> "Inhale"
                                Phase.HOLD1 -> "Hold"
                                Phase.EXHALE -> "Exhale"
                                Phase.HOLD2 -> "Hold"
                            },
                            color = colors.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            //Коробки
            Spacer(modifier = Modifier.height(64.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(100.dp)
            ) {
                SettingItem(
                    title = "Duration",
                    value = duration,
                    onClick = { showDurationDialog = true },
                    colors = colors
                )
                SettingItem(
                    title = "Breathing\nPattern",
                    value = breathingPattern,
                    onClick = { showPatternDialog = true },
                    colors = colors
                )
                SettingItem(
                    title = "Sound",
                    value = "Voice",
                    onClick = { /* TODO */ },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                    } else {
                        isRunning = true
                        remainingTime = when (duration) {
                            "5 min" -> 5 * 60 * 1000L
                            "10 min" -> 10 * 60 * 1000L
                            "15 min" -> 15 * 60 * 1000L
                            "20 min" -> 20 * 60 * 1000L
                            else -> 10 * 60 * 1000L
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) colors.primary else colors.surface
                )
            ) {
                Text(
                    text = if (isRunning) "Stop" else "Start",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.title
                )
            }
        }

        // Иконка настроек
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(end = 18.dp, top = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colors.title
            )
        }

        // Диалоги
        if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false },
                onThemeClick = { showThemeDialog = true }
            )
        }
        if (showThemeDialog) {
            ThemeDialog(
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { theme -> onThemeChange(theme) }
            )
        }
        if (showDurationDialog) {
            DurationDialog(
                onDismiss = { showDurationDialog = false },
                onDurationSelected = { selectedDuration -> duration = selectedDuration }
            )
        }
        if (showPatternDialog) {
            PatternDialog(
                onDismiss = { showPatternDialog = false },
                onPatternSelected = { selectedPattern -> breathingPattern = selectedPattern }
            )
        }
    }
}

// Отдельные функции для диалогов
@Composable
fun SettingsDialog(onDismiss: () -> Unit, onThemeClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column {
                Text(
                    text = "Themes",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeClick() }
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
fun ThemeDialog(onDismiss: () -> Unit, onThemeSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column {
                listOf("Ocean", "Forest", "Sunset").forEach { theme ->
                    Text(
                        text = theme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeSelected(theme)
                                onDismiss()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
fun DurationDialog(onDismiss: () -> Unit, onDurationSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Duration", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column {
                listOf("5 min", "10 min", "15 min", "20 min").forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDurationSelected(option)
                                onDismiss()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
fun PatternDialog(onDismiss: () -> Unit, onPatternSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Breathing Pattern", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column {
                listOf("4-7-8", "Box Breathing").forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPatternSelected(option)
                                onDismiss()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
fun SettingItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    colors: AppColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(colors.surface, shape = RoundedCornerShape(14.dp))
            .width(100.dp)
            .height(120.dp)
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.uppercase(),
                color = colors.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = colors.value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
