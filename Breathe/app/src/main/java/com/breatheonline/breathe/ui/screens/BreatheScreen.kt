package com.breatheonline.breathe.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.AtmosphericBackground
import com.breatheonline.breathe.ui.components.GlowButton
import com.breatheonline.breathe.ui.components.SessionFeedbackModal
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.BREATH_PRESETS
import com.breatheonline.breathe.viewmodel.BreathPhase
import com.breatheonline.breathe.viewmodel.BreatheState
import com.breatheonline.breathe.viewmodel.BreatheViewModel
import com.breatheonline.breathe.viewmodel.GuidanceMode
import com.breatheonline.breathe.viewmodel.VoiceGender
import com.composables.icons.lucide.Bed
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Square
import com.composables.icons.lucide.Waves
import com.composables.icons.lucide.Zap

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun BreatheScreen(
    navController:  NavController,
    colors: AppColors,
    exerciseType:   String,
    viewModel: BreatheViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    val isRunning = state.phase != BreathPhase.IDLE && state.phase != BreathPhase.DONE
    val immersiveMode = isRunning && !state.isCompleted

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
        AtmosphericBackground(colors = colors)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            item(key = "top_bar") {
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = colors.title)
                }
                Text(
                    text     = state.exerciseName,
                    style    = MaterialTheme.typography.headlineSmall,
                    color    = colors.title,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                }
            }

            // ── Quiet header above circle (label + live time/cycles) ──────────
            item(key = "session_header") {
                SessionHeaderAbove(
                    state = state,
                    colors = colors,
                    compact = immersiveMode,
                )
            }

            item(key = "spacer_1") { Spacer(Modifier.height(if (immersiveMode) 10.dp else 20.dp)) }

            // ── Breathing circle ─────────────────────────────────────────────
            item(key = "breathing_circle") {
                BreathingCircle(
                    state = state,
                    scale = circleScale.value,
                    colors = colors,
                    expanded = immersiveMode,
                )
            }

            item(key = "spacer_2") { Spacer(Modifier.height(if (immersiveMode) 12.dp else 24.dp)) }

            // ── Controls (start/pause/guidance) ──────────────────────────────
            item(key = "session_controls") {
                SessionControls(
                    state     = state,
                    colors    = colors,
                    onStart   = { viewModel.startSession() },
                    onToggle  = { viewModel.togglePause() },
                    onEnd     = { viewModel.endSession() },
                    onGuidance= { m, g -> viewModel.setGuidance(m, g) },
                )
            }

            item(key = "spacer_3") { Spacer(Modifier.height(if (immersiveMode) 10.dp else 16.dp)) }

            // ── All-time stats row (Streak / Total / Sessions) ────────────────
            item(key = "stats_row") {
                SessionStatsRow(state = state, colors = colors)
            }

            if (!immersiveMode) {
                item(key = "spacer_4") { Spacer(Modifier.height(24.dp)) }

                // ── Pattern editor ────────────────────────────────────────────
                item(key = "pattern_editor") {
                    PatternEditor(
                        state    = state,
                        colors   = colors,
                        onChange = { i, h1, e, h2 -> viewModel.updatePattern(i, h1, e, h2) },
                    )
                }

                item(key = "spacer_5") { Spacer(Modifier.height(12.dp)) }

                // ── Presets ───────────────────────────────────────────────────
                item(key = "presets_row") {
                    PresetsRow(
                        selected = state.selectedPreset,
                        enabled  = state.phase == BreathPhase.IDLE,
                        colors   = colors,
                        onSelect = { viewModel.applyPreset(it) },
                    )
                }

                item(key = "spacer_6") { Spacer(Modifier.height(28.dp)) }

                // ── Technique guide cards ─────────────────────────────────────
                item(key = "technique_guides") {
                    TechniqueGuideSection(
                        colors        = colors,
                        navController = navController,
                        onApply       = { viewModel.applyPreset(it) },
                        isIdle        = state.phase == BreathPhase.IDLE,
                    )
                }
            } else {
                item(key = "immersive_hint") {
                    Text(
                        text = stringResource(R.string.breathe_pattern_guides_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.subtitle.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 10.dp),
                    )
                }
            }
        }

        // ── Exit dialog ───────────────────────────────────────────────────────
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor   = colors.surface,
                title   = { Text(stringResource(R.string.exit_dialog_title), color = colors.title, style = MaterialTheme.typography.titleLarge) },
                text    = {
                    Text(
                        text  = if (state.elapsedSeconds >= 30) stringResource(R.string.exit_dialog_body_with_progress, fmtElapsed(state.elapsedSeconds)) else stringResource(R.string.exit_dialog_body_no_progress),
                        color = colors.subtitle,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        if (state.elapsedSeconds > 0) viewModel.endSession() else navController.navigateUp()
                    }) { Text(stringResource(R.string.exit_dialog_confirm), color = colors.primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.exit_dialog_dismiss), color = colors.subtitle) }
                },
            )
        }

        // ── Completion overlay ────────────────────────────────────────────────
        AnimatedVisibility(visible = state.isCompleted, enter = fadeIn(tween(500))) {
            CompletionOverlay(state = state, colors = colors, onDone = { navController.navigateUp() })
        }

        // ── Feedback modal ────────────────────────────────────────────────────
        SessionFeedbackModal(
            visible = state.showFeedbackModal,
            colors = colors,
            onSubmit = { viewModel.submitFeedback(it) },
            onDismiss = { viewModel.skipFeedback() },
        )
    }
}

// ── Session header above circle ───────────────────────────────────────────────

@Composable
private fun SessionHeaderAbove(state: BreatheState, colors: AppColors, compact: Boolean = false) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
    ) {
        val label = when {
            state.phase == BreathPhase.IDLE -> stringResource(R.string.breathe_ready_state)
            state.isPaused                  -> stringResource(R.string.breathe_paused_state)
            else                            -> state.exerciseName
        }
        Text(
            text      = label,
            style     = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
            color     = colors.subtitle.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
        )
        // Live time + cycles — shown only once session has started
        if (state.phase != BreathPhase.IDLE) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiniLiveStat(fmtElapsed(state.elapsedSeconds), stringResource(R.string.breathe_time_label),   colors)
                Box(Modifier.size(3.dp).background(colors.subtitle.copy(alpha = 0.20f), CircleShape))
                MiniLiveStat("${state.cyclesCompleted}",        stringResource(R.string.breathe_cycles_label), colors)
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
private fun BreathingCircle(
    state: BreatheState,
    scale: Float,
    colors: AppColors,
    expanded: Boolean = false,
) {
    val phaseText = when (state.phase) {
        BreathPhase.INHALE             -> stringResource(R.string.breathe_inhale)
        BreathPhase.HOLD1, BreathPhase.HOLD2 -> stringResource(R.string.breathe_hold)
        BreathPhase.EXHALE             -> stringResource(R.string.breathe_exhale)
        else                           -> ""
    }
    val circleSize    = if (expanded) 300.dp else 240.dp
    val containerHeight = if (expanded) 420.dp else 320.dp
    val isActive = state.phase != BreathPhase.IDLE && state.phase != BreathPhase.DONE

    // Theme-aware orb duet — every theme breathes in its own hues
    val orbA = colors.primary
    val orbB = colors.orbSecondary

    // Idle glow pulse — slower when active (breathing cycle drives the visual energy)
    val infiniteTransition = rememberInfiniteTransition(label = "orb_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = if (isActive) 0.35f else 0.20f,
        targetValue   = if (isActive) 0.55f else 0.38f,
        animationSpec = infiniteRepeatable(
            animation  = tween(if (isActive) 1400 else 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )
    // Slow orbital drift of the petal layers — the orb feels alive even at rest
    val petalAngle by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(48000, easing = LinearEasing)),
        label = "petal_angle",
    )

    // Phase progress ring — sweeps once per phase, freezes on pause
    val totalPhaseS = (state.phaseDurationMs / 1000f).coerceAtLeast(0.001f)
    val animSecLeft by animateFloatAsState(
        targetValue   = state.phaseSecondsLeft.toFloat(),
        animationSpec = tween(900, easing = LinearEasing),
        label = "phase_arc",
    )
    val phaseFraction = if (isActive) (1f - animSecLeft / totalPhaseS).coerceIn(0f, 1f) else 0f

    Box(
        modifier         = Modifier.fillMaxWidth().height(containerHeight),
        contentAlignment = Alignment.Center,
    ) {
        // Outer glow aura drawn behind the scaled circle
        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(scale)
                .drawBehind {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val baseR = size.width / 2
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f   to orbA.copy(alpha = glowAlpha),
                            0.45f to orbA.copy(alpha = glowAlpha * 0.40f),
                            1f   to Color.Transparent,
                            center = Offset(cx, cy),
                            radius = baseR + 90.dp.toPx(),
                        ),
                        radius = baseR + 90.dp.toPx(),
                    )
                    if (isActive) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f   to orbB.copy(alpha = glowAlpha * 0.55f),
                                0.6f to orbB.copy(alpha = glowAlpha * 0.15f),
                                1f   to Color.Transparent,
                                center = Offset(cx, cy),
                                radius = baseR + 48.dp.toPx(),
                            ),
                            radius = baseR + 48.dp.toPx(),
                        )
                    }

                    // ── Orbiting petals: two counter-rotating translucent layers ──
                    val petalR  = baseR * 0.68f
                    val orbitR  = baseR * 0.40f
                    rotate(petalAngle, pivot = Offset(cx, cy)) {
                        for (k in 0 until 3) {
                            val a = Math.toRadians((k * 120).toDouble())
                            val px = cx + (orbitR * Math.cos(a)).toFloat()
                            val py = cy + (orbitR * Math.sin(a)).toFloat()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0f to orbA.copy(alpha = 0.26f),
                                    1f to Color.Transparent,
                                    center = Offset(px, py),
                                    radius = petalR,
                                ),
                                radius = petalR,
                                center = Offset(px, py),
                            )
                        }
                    }
                    rotate(-petalAngle * 0.7f, pivot = Offset(cx, cy)) {
                        for (k in 0 until 2) {
                            val a = Math.toRadians((90 + k * 180).toDouble())
                            val px = cx + (orbitR * 0.8f * Math.cos(a)).toFloat()
                            val py = cy + (orbitR * 0.8f * Math.sin(a)).toFloat()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0f to orbB.copy(alpha = 0.22f),
                                    1f to Color.Transparent,
                                    center = Offset(px, py),
                                    radius = petalR * 1.1f,
                                ),
                                radius = petalR * 1.1f,
                                center = Offset(px, py),
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .drawBehind {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val r  = size.width / 2

                        // Orb body: off-center radial gradient = lit sphere, not flat disc
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f    to orbA,
                                0.55f to orbA.copy(alpha = 0.92f),
                                1f    to orbB,
                                center = Offset(cx * 0.72f, cy * 0.62f),
                                radius = r * 1.65f,
                            ),
                            radius = r,
                            center = Offset(cx, cy),
                        )

                        // Glassmorphism highlight: bright spot top-left
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f    to Color.White.copy(alpha = 0.50f),
                                0.42f to Color.White.copy(alpha = 0.16f),
                                1f    to Color.Transparent,
                                center = Offset(cx * 0.50f, cy * 0.40f),
                                radius = r * 0.68f,
                            ),
                            radius = r,
                            center = Offset(cx, cy),
                        )

                        // Edge vignette for 3D depth
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f    to Color.Transparent,
                                0.70f to Color.Transparent,
                                1f    to Color.Black.copy(alpha = 0.28f),
                                center = Offset(cx, cy),
                                radius = r,
                            ),
                            radius = r,
                            center = Offset(cx, cy),
                        )

                        // Rim stroke
                        drawCircle(
                            color  = orbA.copy(alpha = if (isActive) 0.65f else 0.35f),
                            radius = r,
                            center = Offset(cx, cy),
                            style  = Stroke(width = 1.5.dp.toPx()),
                        )

                        // Phase progress ring — thin arc floating just outside the orb
                        if (phaseFraction > 0f) {
                            val ringPad = 14.dp.toPx()
                            drawArc(
                                color      = Color.White.copy(alpha = 0.85f),
                                startAngle = -90f,
                                sweepAngle = 360f * phaseFraction,
                                useCenter  = false,
                                topLeft    = Offset(-ringPad, -ringPad),
                                size       = androidx.compose.ui.geometry.Size(size.width + ringPad * 2, size.height + ringPad * 2),
                                style      = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        state.isPaused -> {
                            Icon(
                                imageVector        = Icons.Filled.Pause,
                                contentDescription = null,
                                tint               = Color.White.copy(alpha = 0.90f),
                                modifier           = Modifier.size(32.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.breathe_paused_state),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                        state.phase == BreathPhase.IDLE -> {
                            Icon(
                                imageVector        = Lucide.Sparkles,
                                contentDescription = null,
                                tint               = Color.White.copy(alpha = 0.70f),
                                modifier           = Modifier.size(26.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.breathe_ready_state),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                        else -> {
                            if (state.phaseSecondsLeft > 0) {
                                Text(
                                    text       = "${state.phaseSecondsLeft}",
                                    style      = MaterialTheme.typography.displaySmall.copy(
                                        fontSize      = 58.sp,
                                        lineHeight    = 62.sp,
                                        letterSpacing = (-1).sp,
                                    ),
                                    color      = Color.White,
                                    fontWeight = FontWeight.ExtraLight,
                                )
                            }
                            if (phaseText.isNotEmpty()) {
                                Text(
                                    text  = phaseText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize      = 20.sp,
                                        letterSpacing = 3.sp,
                                    ),
                                    color = Color.White.copy(alpha = 0.88f),
                                )
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
                    Box(Modifier.size(6.dp).background(orbA.copy(alpha = 0.75f), CircleShape))
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
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill("${state.streakDays}d",   stringResource(R.string.stat_label_streak),    colors, Modifier.weight(1f))
        StatPill("${state.totalMinutes}m", stringResource(R.string.sessions_total_label), colors, Modifier.weight(1f))
        StatPill("${state.totalSessions}", stringResource(R.string.stat_sessions),        colors, Modifier.weight(1f))
        if (state.heartRate != null) {
            StatPill("${state.heartRate}", stringResource(R.string.breathe_heart_rate_label), colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, colors: AppColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleSmall,
            color      = colors.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = colors.subtitle.copy(alpha = 0.70f),
            fontSize  = 10.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Session Controls ──────────────────────────────────────────────────────────

@Composable
private fun SessionControls(
    state: BreatheState,
    colors: AppColors,
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
            GlowButton(
                text     = stringResource(R.string.breathe_start_button),
                colors   = colors,
                onClick  = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
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
                        text  = if (state.isPaused) stringResource(R.string.breathe_resume_button) else stringResource(R.string.breathe_pause_button),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                    )
                }
                Button(
                    onClick  = onEnd,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.13f),
                        contentColor   = Color(0xFFFF8A8A),
                    ),
                ) {
                    Text(stringResource(R.string.breathe_end_button), style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF8A8A))
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
    mode: GuidanceMode,
    voiceGender: VoiceGender,
    colors: AppColors,
    onChange:    (GuidanceMode, VoiceGender) -> Unit,
) {
    val modes = listOf(
        Triple(GuidanceMode.SILENT,    R.string.guidance_silent,  "○"),
        Triple(GuidanceMode.VIBRATION, R.string.guidance_vibration, "≋"),
        Triple(GuidanceMode.VOICE,     R.string.guidance_ai_voice,   "◉"),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        modes.forEach { (gMode, labelRes, icon) ->
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
                        text     = stringResource(labelRes),
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
    state: BreatheState,
    colors: AppColors,
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
                text          = stringResource(R.string.breathe_pattern_title),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.primary,
                letterSpacing = 1.sp,
                fontWeight    = FontWeight.SemiBold,
            )
            if (!editable) {
                Text(
                    text  = stringResource(R.string.breathe_active),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary.copy(alpha = 0.65f),
                )
            }
        }

        PhaseBar(
            label      = stringResource(R.string.breathe_inhale),
            value      = state.inhaleS,
            active     = state.phase == BreathPhase.INHALE,
            editable   = editable,
            canBeZero  = false,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS - 1, state.hold1S, state.exhaleS, state.hold2S) },
            onIncrement = { onChange(state.inhaleS + 1, state.hold1S, state.exhaleS, state.hold2S) },
        )
        PhaseBar(
            label      = stringResource(R.string.breathe_hold),
            value      = state.hold1S,
            active     = state.phase == BreathPhase.HOLD1,
            editable   = editable,
            canBeZero  = true,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS, state.hold1S - 1, state.exhaleS, state.hold2S) },
            onIncrement = { onChange(state.inhaleS, state.hold1S + 1, state.exhaleS, state.hold2S) },
        )
        PhaseBar(
            label      = stringResource(R.string.breathe_exhale),
            value      = state.exhaleS,
            active     = state.phase == BreathPhase.EXHALE,
            editable   = editable,
            canBeZero  = false,
            colors     = colors,
            onDecrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS - 1, state.hold2S) },
            onIncrement = { onChange(state.inhaleS, state.hold1S, state.exhaleS + 1, state.hold2S) },
        )
        PhaseBar(
            label      = stringResource(R.string.breathe_pause),
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
    colors: AppColors,
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
    colors: AppColors,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text          = stringResource(R.string.breathe_section_presets),
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
                            text       = stringResource(preset.labelRes),
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
    val icon:       ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val tagRes:   Int,
    @StringRes val descRes:  Int,
    val presetKey:  String,
    val articleUrl: String,
)

private val TECHNIQUE_CARDS = listOf(
    TechniqueCard(Lucide.Square, R.string.technique_box_breathing_title, R.string.technique_box_breathing_tag, R.string.technique_box_breathing_desc, "box", "https://breatheonline.app/articles/box-breathing"),
    TechniqueCard(Lucide.Bed, R.string.technique_478_sleep_title, R.string.technique_478_sleep_tag, R.string.technique_478_sleep_desc, "4-7-8", "https://breatheonline.app/articles/4-7-8-breathing"),
    TechniqueCard(Lucide.Zap, R.string.technique_wim_hof_title, R.string.technique_wim_hof_tag, R.string.technique_wim_hof_desc, "wimhof", "https://breatheonline.app/articles/wim-hof"),
    TechniqueCard(Lucide.Waves, R.string.technique_coherent_title, R.string.technique_coherent_tag, R.string.technique_coherent_desc, "coherent", "https://breatheonline.app/articles/coherent-breathing"),
)

@Composable
private fun TechniqueGuideSection(
    colors: AppColors,
    navController: NavController,
    onApply:       (String) -> Unit,
    isIdle:        Boolean,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text          = stringResource(R.string.breathe_section_techniques),
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
    colors: AppColors,
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
                    Icon(
                        imageVector = card.icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column {
                    Text(stringResource(card.titleRes), style = MaterialTheme.typography.titleSmall,  color = colors.title,                        fontWeight = FontWeight.SemiBold)
                    Text(stringResource(card.tagRes),   style = MaterialTheme.typography.labelSmall,  color = colors.primary.copy(alpha = 0.75f))
                }
            }
        }

        Text(
            text  = stringResource(card.descRes),
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
                    Text(stringResource(R.string.breathe_apply), style = MaterialTheme.typography.labelMedium, color = colors.primary, fontWeight = FontWeight.SemiBold)
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
                    Text(stringResource(R.string.breathe_read_guide), style = MaterialTheme.typography.labelMedium, color = colors.subtitle)
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
                    Text(stringResource(R.string.breathe_read_guide), style = MaterialTheme.typography.labelMedium, color = colors.subtitle)
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
                text  = pluralStringResource(R.plurals.cycle_count, state.cyclesCompleted, state.cyclesCompleted),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.subtitle,
            )
            Spacer(Modifier.height(48.dp))
            GlowButton(
                text     = stringResource(R.string.btn_done),
                colors   = colors,
                onClick  = onDone,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun fmtElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
