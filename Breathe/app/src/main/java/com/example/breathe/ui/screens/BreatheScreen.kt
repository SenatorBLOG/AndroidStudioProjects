package com.example.breathe.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.R
import com.example.breathe.ui.components.SessionFeedbackModal
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.BREATH_PRESETS
import com.example.breathe.viewmodel.BreathPhase
import com.example.breathe.viewmodel.BreatheState
import com.example.breathe.viewmodel.BreatheViewModel
import com.example.breathe.viewmodel.GuidanceMode
import com.example.breathe.viewmodel.VoiceGender

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun BreatheScreen(
    navController:  NavController,
    colors:         AppColors,
    exerciseType:   String,
    @Suppress("UNUSED_PARAMETER") onThemeChange: (String) -> Unit,
    viewModel:      BreatheViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    val isRunning = state.phase != BreathPhase.IDLE && state.phase != BreathPhase.DONE

    BackHandler(enabled = isRunning && !state.isCompleted) { showExitDialog = true }
    BackHandler(enabled = state.showFeedbackModal) { viewModel.skipFeedback() }

    // ── Circle scale animation ────────────────────────────────────────────────
    val circleScale = remember { Animatable(0.92f) }
    LaunchedEffect(state.phase, state.isPaused) {
        when {
            state.isPaused                    -> circleScale.animateTo(0.88f, tween(600))
            state.phase == BreathPhase.INHALE -> circleScale.animateTo(1.30f, tween(state.phaseDurationMs, easing = LinearEasing))
            state.phase == BreathPhase.EXHALE -> circleScale.animateTo(0.85f, tween(state.phaseDurationMs, easing = LinearEasing))
            state.phase == BreathPhase.DONE   -> circleScale.animateTo(0.85f, tween(500))
            state.phase == BreathPhase.IDLE   -> circleScale.animateTo(0.92f, tween(800))
            else                              -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                IconButton(onClick = {
                    when {
                        state.isCompleted -> navController.navigateUp()
                        isRunning         -> showExitDialog = true
                        else              -> navController.navigateUp()
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.title)
                }
                Text(
                    text     = state.exerciseName,
                    style    = MaterialTheme.typography.headlineSmall,
                    color    = colors.title,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
            }

            // ── Quiet header above circle (label + live time/cycles) ──────────
            SessionHeaderAbove(state = state, colors = colors)

            Spacer(Modifier.height(20.dp))

            // ── Breathing circle ─────────────────────────────────────────────
            BreathingCircle(state = state, scale = circleScale.value, colors = colors)

            Spacer(Modifier.height(24.dp))

            // ── Controls (start/pause/guidance) ──────────────────────────────
            SessionControls(
                state     = state,
                colors    = colors,
                onStart   = { viewModel.startSession() },
                onToggle  = { viewModel.togglePause() },
                onEnd     = { viewModel.endSession() },
                onGuidance= { m, g -> viewModel.setGuidance(m, g) },
            )

            Spacer(Modifier.height(16.dp))

            // ── All-time stats row (Streak / Total / Sessions) ────────────────
            SessionStatsRow(state = state, colors = colors)

            Spacer(Modifier.height(24.dp))

            // ── Pattern editor ────────────────────────────────────────────────
            PatternEditor(
                state    = state,
                colors   = colors,
                onChange = { i, h1, e, h2 -> viewModel.updatePattern(i, h1, e, h2) },
            )

            Spacer(Modifier.height(12.dp))

            // ── Presets ───────────────────────────────────────────────────────
            PresetsRow(
                selected = state.selectedPreset,
                enabled  = state.phase == BreathPhase.IDLE,
                colors   = colors,
                onSelect = { viewModel.applyPreset(it) },
            )

            Spacer(Modifier.height(28.dp))

            // ── Technique guide cards ─────────────────────────────────────────
            TechniqueGuideSection(
                colors        = colors,
                navController = navController,
                onApply       = { viewModel.applyPreset(it) },
                isIdle        = state.phase == BreathPhase.IDLE,
            )

            Spacer(Modifier.height(40.dp))
        }

        // ── Exit dialog ───────────────────────────────────────────────────────
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor   = colors.surface,
                title   = { Text("End session?", color = colors.title, style = MaterialTheme.typography.titleLarge) },
                text    = {
                    Text(
                        text  = if (state.elapsedSeconds >= 30) "You've been breathing for ${fmtElapsed(state.elapsedSeconds)}. Save your progress?" else "Discard this session?",
                        color = colors.subtitle,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        if (state.elapsedSeconds > 0) viewModel.endSession() else navController.navigateUp()
                    }) { Text("End", color = colors.primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("Continue", color = colors.subtitle) }
                },
            )
        }

        // ── Completion overlay ────────────────────────────────────────────────
        AnimatedVisibility(visible = state.isCompleted, enter = fadeIn(tween(500))) {
            CompletionOverlay(state = state, colors = colors, onDone = { navController.navigateUp() })
        }

        // ── Feedback modal ────────────────────────────────────────────────────
        SessionFeedbackModal(
            visible   = state.showFeedbackModal,
            colors    = colors,
            onSubmit  = { viewModel.submitFeedback(it) },
            onDismiss = { viewModel.skipFeedback() },
        )
    }
}

// ── Session header above circle ───────────────────────────────────────────────

@Composable
private fun SessionHeaderAbove(state: BreatheState, colors: AppColors) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val label = when {
            state.phase == BreathPhase.IDLE -> "Ready to breathe"
            state.isPaused                  -> "Session paused"
            else                            -> state.exerciseName
        }
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodySmall,
            color     = colors.subtitle.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
        )
        // Live time + cycles — shown only once session has started
        if (state.phase != BreathPhase.IDLE) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiniLiveStat(fmtElapsed(state.elapsedSeconds), "time",   colors)
                Box(Modifier.size(3.dp).background(colors.subtitle.copy(alpha = 0.20f), CircleShape))
                MiniLiveStat("${state.cyclesCompleted}",        "cycles", colors)
            }
        }
    }
}

@Composable
private fun MiniLiveStat(value: String, label: String, colors: AppColors) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.labelMedium,
            color      = colors.primary.copy(alpha = 0.90f),
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
        )
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = colors.subtitle.copy(alpha = 0.55f),
            fontSize = 10.sp,
        )
    }
}

// ── Breathing Circle ──────────────────────────────────────────────────────────

@Composable
private fun BreathingCircle(state: BreatheState, scale: Float, colors: AppColors) {
    val phaseText = when (state.phase) {
        BreathPhase.INHALE             -> "Inhale"
        BreathPhase.HOLD1, BreathPhase.HOLD2 -> "Hold"
        BreathPhase.EXHALE             -> "Exhale"
        else                           -> ""
    }

    Box(
        modifier         = Modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(scale)
                .drawBehind {
                    val r = size.width / 2 + 72.dp.toPx()
                    drawCircle(
                        brush  = Brush.radialGradient(
                            0f to colors.glowOuter,
                            1f to Color.Transparent,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = r,
                        ),
                        radius = r,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(colors.background, CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = colors.primary.copy(alpha = if (state.phase == BreathPhase.IDLE) 0.20f else 0.45f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colors.glowBackground, CircleShape)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0f    to colors.glowInner.copy(alpha = 0.04f),
                                        0.99f to colors.glowInner.copy(alpha = 0.55f),
                                        1f    to colors.glowInner.copy(alpha = 0.80f),
                                    ),
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width / 2,
                                ),
                                radius = size.width / 2,
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when {
                            state.isPaused -> {
                                Text("⏸", fontSize = 28.sp)
                                Text("Paused", style = MaterialTheme.typography.bodyMedium, color = colors.subtitle)
                            }
                            state.phase == BreathPhase.IDLE -> {
                                Text("✦", fontSize = 26.sp, color = colors.primary.copy(alpha = 0.55f))
                                Spacer(Modifier.height(4.dp))
                                Text("Ready", style = MaterialTheme.typography.bodyMedium, color = colors.subtitle)
                            }
                            else -> {
                                if (state.phaseSecondsLeft > 0) {
                                    Text(
                                        text  = "${state.phaseSecondsLeft}",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = colors.title,
                                    )
                                }
                                if (phaseText.isNotEmpty()) {
                                    Text(
                                        text  = phaseText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.title,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.cyclesCompleted > 0) {
            Row(
                modifier              = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(state.cyclesCompleted.coerceAtMost(8)) {
                    Box(Modifier.size(6.dp).background(colors.primary.copy(alpha = 0.65f), CircleShape))
                }
            }
        }
    }
}

// ── Session Stats Row (all-time: Streak / Total / Sessions) ───────────────────

@Composable
private fun SessionStatsRow(state: BreatheState, colors: AppColors) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill("${state.streakDays}d",   "Streak",   colors)
        StatPill("${state.totalMinutes}m", "Total",    colors)
        StatPill("${state.totalSessions}", "Sessions", colors)
        if (state.heartRate != null) {
            StatPill("${state.heartRate} bpm", "Heart rate", colors)
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, colors: AppColors) {
    Column(
        modifier = Modifier
            .background(colors.surface.copy(alpha = 0.70f), RoundedCornerShape(14.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.labelLarge, color = colors.primary.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.subtitle.copy(alpha = 0.60f), fontSize = 9.sp)
    }
}

// ── Session Controls ──────────────────────────────────────────────────────────

@Composable
private fun SessionControls(
    state:     BreatheState,
    colors:    AppColors,
    onStart:   () -> Unit,
    onToggle:  () -> Unit,
    onEnd:     () -> Unit,
    onGuidance:(GuidanceMode, VoiceGender) -> Unit,
) {
    val isIdle    = state.phase == BreathPhase.IDLE
    val isRunning = !isIdle && state.phase != BreathPhase.DONE

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isIdle) {
            Button(
                onClick  = onStart,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.onPrimary,
                ),
            ) {
                Text("▶  Start Breathing", style = MaterialTheme.typography.labelLarge)
            }
        } else if (isRunning) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick  = onToggle,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = colors.primary.copy(alpha = 0.15f),
                        contentColor   = colors.primary,
                    ),
                ) {
                    Text(
                        text  = if (state.isPaused) "▶  Resume" else "⏸  Pause",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                    )
                }
                Button(
                    onClick  = onEnd,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = colors.surface,
                        contentColor   = colors.subtitle,
                    ),
                ) {
                    Text("■  End", style = MaterialTheme.typography.labelLarge, color = colors.subtitle)
                }
            }
        }

        // Inline guidance row — full-width, organic
        InlineGuidanceRow(
            mode        = state.guidanceMode,
            voiceGender = state.voiceGender,
            colors      = colors,
            onChange    = onGuidance,
        )
    }
}

// ── Inline guidance mode row ──────────────────────────────────────────────────

@Composable
private fun InlineGuidanceRow(
    mode:        GuidanceMode,
    voiceGender: VoiceGender,
    colors:      AppColors,
    onChange:    (GuidanceMode, VoiceGender) -> Unit,
) {
    val modes = listOf(
        Triple(GuidanceMode.SILENT,    "Silent",  "○"),
        Triple(GuidanceMode.VIBRATION, "Vibrate", "≋"),
        Triple(GuidanceMode.VOICE,     "Voice",   "◉"),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        modes.forEach { (gMode, label, icon) ->
            val active = mode == gMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onChange(gMode, voiceGender) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text       = icon,
                        color      = if (active) colors.primary else colors.subtitle.copy(alpha = 0.50f),
                        fontSize   = 15.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(
                        text     = label,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (active) colors.primary else colors.subtitle.copy(alpha = 0.60f),
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

// ── Pattern Editor ────────────────────────────────────────────────────────────

@Composable
private fun PatternEditor(
    state:    BreatheState,
    colors:   AppColors,
    onChange: (inhale: Int, hold1: Int, exhale: Int, hold2: Int) -> Unit,
) {
    val editable = state.phase == BreathPhase.IDLE

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.07f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text          = "BREATHING PATTERN",
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.primary,
                letterSpacing = 1.sp,
                fontWeight    = FontWeight.SemiBold,
            )
            if (!editable) {
                Text(
                    text  = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary.copy(alpha = 0.65f),
                )
            }
        }

        PhaseBar(
            label      = "Inhale",
            value      = state.inhaleS,
            active     = state.phase == BreathPhase.INHALE,
            editable   = editable,
            canBeZero  = false,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS - 1, state.hold1S, state.exhaleS, state.hold2S) },
            onIncrement = { onChange(state.inhaleS + 1, state.hold1S, state.exhaleS, state.hold2S) },
        )
        PhaseBar(
            label      = "Hold",
            value      = state.hold1S,
            active     = state.phase == BreathPhase.HOLD1,
            editable   = editable,
            canBeZero  = true,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS, state.hold1S - 1, state.exhaleS, state.hold2S) },
            onIncrement = { onChange(state.inhaleS, state.hold1S + 1, state.exhaleS, state.hold2S) },
        )
        PhaseBar(
            label      = "Exhale",
            value      = state.exhaleS,
            active     = state.phase == BreathPhase.EXHALE,
            editable   = editable,
            canBeZero  = false,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS - 1, state.hold2S) },
            onIncrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS + 1, state.hold2S) },
        )
        PhaseBar(
            label      = "Pause",
            value      = state.hold2S,
            active     = state.phase == BreathPhase.HOLD2,
            editable   = editable,
            canBeZero  = true,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS, state.hold2S - 1) },
            onIncrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS, state.hold2S + 1) },
        )
    }
}

@Composable
private fun PhaseBar(
    label:      String,
    value:      Int,
    active:     Boolean,
    editable:   Boolean,
    canBeZero:  Boolean,
    colors:     AppColors,
    onDecrement:() -> Unit,
    onIncrement:() -> Unit,
) {
    val accentAlpha = if (active) 1f else 0.70f
    val bgColor     = if (active) colors.primary.copy(alpha = 0.10f) else Color.Transparent

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodySmall,
            color      = colors.primary.copy(alpha = accentAlpha),
            modifier   = Modifier.width(52.dp),
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / 10f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.65f),
                                colors.primary.copy(alpha = accentAlpha),
                            )
                        )
                    ),
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text      = "${value}s",
            style     = MaterialTheme.typography.labelMedium,
            color     = colors.primary.copy(alpha = accentAlpha),
            modifier  = Modifier.width(28.dp),
            textAlign = TextAlign.End,
        )

        if (editable) {
            Spacer(Modifier.width(6.dp))
            StepButton("−", enabled = value > (if (canBeZero) 0 else 1), colors = colors, onClick = onDecrement)
            Spacer(Modifier.width(4.dp))
            StepButton("+", enabled = value < 10, colors = colors, onClick = onIncrement)
        }
    }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, colors: AppColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.primary.copy(alpha = 0.12f) else colors.background)
            .let { if (enabled) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = label,
            color = if (enabled) colors.primary else colors.subtitle.copy(alpha = 0.3f),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

// ── Presets Row ───────────────────────────────────────────────────────────────

@Composable
private fun PresetsRow(
    selected: String,
    enabled:  Boolean,
    colors:   AppColors,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text          = "PRESETS",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(bottom = 10.dp),
        )
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BREATH_PRESETS.forEach { preset ->
                val isSelected = selected == preset.key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surface)
                        .border(
                            1.dp,
                            if (isSelected) colors.primary.copy(alpha = 0.50f)
                            else            colors.subtitle.copy(alpha = 0.12f),
                            RoundedCornerShape(14.dp),
                        )
                        .let { if (enabled) it.clickable { onSelect(preset.key) } else it }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = preset.label,
                            style      = MaterialTheme.typography.labelMedium,
                            color      = if (isSelected) colors.primary else colors.subtitle,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Text(
                            text     = "${preset.inhaleS}-${preset.hold1S}-${preset.exhaleS}-${preset.hold2S}",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (isSelected) colors.primary.copy(alpha = 0.75f) else colors.subtitle.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Technique Guide Cards ─────────────────────────────────────────────────────

private data class TechniqueCard(
    val icon:       String,
    val title:      String,
    val tag:        String,
    val desc:       String,
    val presetKey:  String,
    val articleUrl: String,
)

private val TECHNIQUE_CARDS = listOf(
    TechniqueCard("🔲", "Box Breathing",       "Calm · Focus",   "Equal 4-count phases used by Navy SEALs to reset the nervous system under stress.",      "box",      "https://breatheonline.app/articles/box-breathing"),
    TechniqueCard("😴", "4-7-8 for Sleep",     "Sleep · Relax",  "Long exhale activates the parasympathetic system, easing you into deep sleep.",          "4-7-8",    "https://breatheonline.app/articles/4-7-8-breathing"),
    TechniqueCard("⚡", "Wim Hof Energize",    "Energy · Focus", "Short rapid cycles followed by retention boost alertness and oxygen efficiency.",         "wimhof",   "https://breatheonline.app/articles/wim-hof"),
    TechniqueCard("🌊", "Coherent Breathing",  "Balance · HRV",  "5-second in/out cycle synchronises heart rate variability for lasting calm.",             "coherent", "https://breatheonline.app/articles/coherent-breathing"),
)

@Composable
private fun TechniqueGuideSection(
    colors:        AppColors,
    navController: NavController,
    onApply:       (String) -> Unit,
    isIdle:        Boolean,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text          = "TECHNIQUES & GUIDES",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TECHNIQUE_CARDS.forEach { card ->
                TechniqueCardItem(
                    card          = card,
                    colors        = colors,
                    navController = navController,
                    onApply       = onApply,
                    isIdle        = isIdle,
                )
            }
        }
    }
}

@Composable
private fun TechniqueCardItem(
    card:          TechniqueCard,
    colors:        AppColors,
    navController: NavController,
    onApply:       (String) -> Unit,
    isIdle:        Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.07f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.primary.copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(card.icon, fontSize = 18.sp)
                }
                Column {
                    Text(card.title, style = MaterialTheme.typography.titleSmall,  color = colors.title,                        fontWeight = FontWeight.SemiBold)
                    Text(card.tag,   style = MaterialTheme.typography.labelSmall,  color = colors.primary.copy(alpha = 0.75f))
                }
            }
        }

        Text(
            text  = card.desc,
            style = MaterialTheme.typography.bodySmall,
            color = colors.subtitle.copy(alpha = 0.85f),
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isIdle) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary.copy(alpha = 0.14f))
                        .border(1.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                        .clickable { onApply(card.presetKey) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Apply", style = MaterialTheme.typography.labelMedium, color = colors.primary, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.background.copy(alpha = 0.60f))
                        .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Route.article(card.articleUrl)) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Read Guide →", style = MaterialTheme.typography.labelMedium, color = colors.subtitle)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.background.copy(alpha = 0.60f))
                        .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Route.article(card.articleUrl)) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Read Guide →", style = MaterialTheme.typography.labelMedium, color = colors.subtitle)
                }
            }
        }
    }
}

// ── Completion overlay ────────────────────────────────────────────────────────

@Composable
private fun CompletionOverlay(state: BreatheState, colors: AppColors, onDone: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(colors.primary.copy(alpha = 0.14f), CircleShape)
                    .drawBehind {
                        val r = size.width / 2 + 24.dp.toPx()
                        drawCircle(
                            brush  = Brush.radialGradient(
                                0f to colors.glowInner.copy(alpha = 0.35f),
                                1f to Color.Transparent,
                                center = Offset(size.width / 2, size.height / 2),
                                radius = r,
                            ),
                            radius = r,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(52.dp))
            }

            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.session_complete), style = MaterialTheme.typography.headlineSmall, color = colors.title)
            Spacer(Modifier.height(12.dp))
            Text(fmtElapsed(state.elapsedSeconds), style = MaterialTheme.typography.headlineMedium, color = colors.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "${state.cyclesCompleted} cycle${if (state.cyclesCompleted != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.subtitle,
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick  = onDone,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.primary),
            ) {
                Text(stringResource(R.string.btn_done), style = MaterialTheme.typography.labelLarge, color = colors.onPrimary)
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun fmtElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
