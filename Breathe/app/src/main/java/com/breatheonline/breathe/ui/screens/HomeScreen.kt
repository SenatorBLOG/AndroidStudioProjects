package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.AiCoachBottomSheet
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.AiCoachViewModel
import com.breatheonline.breathe.viewmodel.HomeViewModel
import com.breatheonline.breathe.viewmodel.SleepStoryViewModel
import com.breatheonline.breathe.viewmodel.StoryState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoonStar
import java.time.LocalTime

// ── Quick-exercise data ────────────────────────────────────────────────────────

private data class QuickExercise(
    val routeType:      String,
    val label:          String,
    val duration:       String,
    val icon:           ImageVector,
    @androidx.annotation.StringRes val descriptionRes: Int,
)

private val QUICK_EXERCISES = listOf(
    QuickExercise("4-7-8",     "4-7-8",        "4 · 7 · 8",     Icons.Filled.Air,             R.string.home_exercise_desc_4_7_8),
    QuickExercise("box",       "Box",           "4 · 4 · 4 · 4", Icons.Filled.CropSquare,      R.string.home_exercise_desc_box),
    QuickExercise("wimhof",    "Wim Hof",       "2 · 1 · 2",     Icons.Filled.LocalFireDepartment, R.string.home_exercise_desc_wimhof),
    QuickExercise("coherent",  "Coherent",      "5 · 5",         Icons.Filled.Favorite,        R.string.home_exercise_desc_coherent),
    QuickExercise("belly",     "Belly",         "4 · 6 · 2",     Icons.Filled.SelfImprovement, R.string.home_exercise_desc_belly),
    QuickExercise("morning",   "Morning",       "4 · 4 · 4",     Icons.Filled.WbSunny,         R.string.home_exercise_desc_morning),
    QuickExercise("alternate", "Alternate",     "4 · 4 · 4 · 2", Icons.Filled.Loop,            R.string.home_exercise_desc_alternate),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController:  NavController,
    colors: AppColors,
    viewModel: HomeViewModel = hiltViewModel(),
    coachViewModel: AiCoachViewModel = hiltViewModel(),
    storyViewModel: SleepStoryViewModel = hiltViewModel(),
) {
    val state      by viewModel.state.collectAsState()
    val storyState by storyViewModel.storyState.collectAsState()
    var showAiSheet    by remember { mutableStateOf(false) }
    var showStorySheet by remember { mutableStateOf(false) }

    val hour     = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 17 -> stringResource(R.string.greeting_afternoon)
        else      -> stringResource(R.string.greeting_evening)
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val lazyListState = rememberLazyListState()
    val parallaxOffset by remember {
        derivedStateOf {
            (lazyListState.firstVisibleItemScrollOffset +
             lazyListState.firstVisibleItemIndex * 280) * 0.28f
        }
    }

    val haptic = LocalHapticFeedback.current

    val enter0   = remember { fadeIn(tween(500,   0)) + slideInVertically(tween(500,   0)) { it / 10 } }
    val enter120 = remember { fadeIn(tween(500, 120)) + slideInVertically(tween(500, 120)) { it / 10 } }
    val enter180 = remember { fadeIn(tween(500, 180)) + slideInVertically(tween(500, 180)) { it / 10 } }
    val enter200 = remember { fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { it / 10 } }
    val enter280 = remember { fadeIn(tween(500, 280)) + slideInVertically(tween(500, 280)) { it / 10 } }

    if (showAiSheet) {
        AiCoachBottomSheet(
            onDismiss = { showAiSheet = false },
            navController = navController,
            colors = colors,
            viewModel = coachViewModel,
        )
    }

    if (showStorySheet) {
        SleepStorySheet(
            storyState = storyState,
            onGenerate = storyViewModel::generateStory,
            onDismiss  = {
                showStorySheet = false
                storyViewModel.reset()
            },
            colors     = colors,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // ── Parallax mesh-gradient background ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = -parallaxOffset }
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    // Blob 1 — top-left glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(colors.primary.copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(w * 0.12f, h * 0.08f),
                            radius = w * 0.62f,
                        ),
                        radius = w * 0.62f,
                        center = Offset(w * 0.12f, h * 0.08f),
                    )
                    // Blob 2 — bottom-right glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(colors.primary.copy(alpha = 0.10f), Color.Transparent),
                            center = Offset(w * 0.92f, h * 0.82f),
                            radius = w * 0.58f,
                        ),
                        radius = w * 0.58f,
                        center = Offset(w * 0.92f, h * 0.82f),
                    )
                    // Blob 3 — center ambient
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(colors.primary.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(w * 0.52f, h * 0.44f),
                            radius = w * 0.72f,
                        ),
                        radius = w * 0.72f,
                        center = Offset(w * 0.52f, h * 0.44f),
                    )
                },
        )
    LazyColumn(
        state   = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        item(key = "header") {
            AnimatedVisibility(visible = visible, enter = enter0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text          = stringResource(R.string.home_breathe_header),
                    style         = MaterialTheme.typography.labelSmall,
                    color         = colors.primary,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text      = if (state.userName.isNotEmpty()) "$greeting, ${state.userName}"
                                else greeting,
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = colors.title,
                    textAlign  = TextAlign.Center,
                )
                // ── Tiny stats pill ───────────────────────────────────────────
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colors.primary.copy(alpha = 0.10f),
                                    colors.surface.copy(alpha = 0.85f),
                                )
                            )
                        )
                        .border(1.dp, colors.primary.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint               = colors.primary,
                        modifier           = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = stringResource(R.string.home_streak_count, state.currentStreak),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        imageVector        = Icons.Default.Timer,
                        contentDescription = null,
                        tint               = colors.primary,
                        modifier           = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = stringResource(R.string.home_today_minutes, state.todayMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                    )
                    state.restingHeartRate?.let { bpm ->
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            imageVector        = Icons.Default.Favorite,
                            contentDescription = null,
                            tint               = Color(0xFFE57373),
                            modifier           = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "$bpm bpm",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.subtitle,
                        )
                    }
                    state.lastSleepHours?.let { h ->
                        val sleepStr = if (h % 1.0 < 0.1) "${h.toInt()}h" else "${h.toInt()}h ${((h % 1.0) * 60).toInt()}m"
                        Spacer(Modifier.width(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable {
                                navController.navigate(Route.history("sleep"))
                            },
                        ) {
                            Icon(
                                imageVector = Lucide.MoonStar,
                                contentDescription = null,
                                tint = colors.subtitle,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text  = sleepStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.subtitle,
                            )
                        }
                    }
                }
            }
            }
        }

        item(key = "spacer_after_header") { Spacer(Modifier.height(24.dp)) }



        // ── Breathing techniques grid ─────────────────────────────────────────
        item(key = "quick_exercises_section") {
            AnimatedVisibility(visible = visible, enter = enter120) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text     = stringResource(R.string.section_quick_exercises),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = colors.subtitle,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                // ── Bento Grid ────────────────────────────────────────────────
                // Row 0: Hero card (full width)
                HeroTechniqueCard(
                    exercise = QUICK_EXERCISES[0],
                    colors   = colors,
                    onClick  = { navController.navigate(Route.breathe(QUICK_EXERCISES[0].routeType)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                // Rows 1-3: 2-column pairs
                listOf(1..2, 3..4).forEach { range ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        range.forEach { idx ->
                            TechniqueCard(
                                exercise = QUICK_EXERCISES[idx],
                                colors   = colors,
                                onClick  = { navController.navigate(Route.breathe(QUICK_EXERCISES[idx].routeType)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            }
        }

        item(key = "spacer_after_quick_exercises") { Spacer(Modifier.height(8.dp)) }

        item(key = "achievement_highlights") {
            AnimatedVisibility(visible = visible, enter = enter180) {
                AchievementHighlightsSection(
                    colors = colors,
                    navController = navController,
                )
            }
        }

        item(key = "spacer_after_achievements") { Spacer(Modifier.height(8.dp)) }

        // ── Start Meditation CTA ──────────────────────────────────────────────
        item(key = "start_meditation_cta") {
            AnimatedVisibility(visible = visible, enter = enter280) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(Route.MEDITATION) {
                                popUpTo(Route.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                        .padding(vertical = 16.dp),
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Spa,
                            contentDescription = null,
                            tint               = colors.onPrimary,
                            modifier           = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = stringResource(R.string.home_start_meditation),
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { navController.navigate(Route.FAQ) }) {
                        Text(
                            text  = stringResource(R.string.home_how_it_works),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                        )
                    }
                    TextButton(onClick = { navController.navigate(Route.INTERACTIVE) }) {
                        Text(
                            text  = stringResource(R.string.home_breathing_quiz),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                        )
                    }
                }
            }
            }
        }

        item(key = "spacer_after_cta") { Spacer(Modifier.height(16.dp)) }
        // ── AI Coach ─────────────────────────────────────────────────────────
        item(key = "ai_coach_card") {
            AnimatedVisibility(visible = visible, enter = enter200) {
            val aiCoachSource = remember { MutableInteractionSource() }
            val aiCoachPressed by aiCoachSource.collectIsPressedAsState()
            val aiCoachScale by animateFloatAsState(
                targetValue   = if (aiCoachPressed) 0.97f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label         = "aiCoachScale",
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .graphicsLayer { scaleX = aiCoachScale; scaleY = aiCoachScale }
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.primary.copy(alpha = 0.08f),
                                colors.surface.copy(alpha = 0.82f),
                            )
                        )
                    )
                    .border(1.dp, colors.primary.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    .clickable(interactionSource = aiCoachSource, indication = null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAiSheet = true
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(R.string.home_ai_coach_title),
                        style      = MaterialTheme.typography.titleSmall,
                        color      = colors.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text  = stringResource(R.string.home_ai_coach_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint               = colors.primary,
                    modifier           = Modifier.size(16.dp),
                )
            }
            }
        }

        item(key = "spacer_after_ai_coach") { Spacer(Modifier.height(12.dp)) }

        item(key = "home_footer_note") {
            Text(
                text = stringResource(R.string.home_community_info),
                style = MaterialTheme.typography.bodySmall,
                color = colors.subtitle.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

    }
    } // Box
}

// ── Technique card (2-column grid) ────────────────────────────────────────────

@Composable
private fun TechniqueCard(
    exercise: QuickExercise,
    colors: AppColors,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "techniqueCardScale",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary.copy(alpha = 0.10f),
                        colors.surface.copy(alpha = 0.85f),
                    )
                )
            )
            .border(1.dp, colors.primary.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector        = exercise.icon,
                contentDescription = exercise.label,
                tint               = colors.primary,
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text       = exercise.label,
                style      = MaterialTheme.typography.titleSmall,
                color      = colors.title,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = stringResource(exercise.descriptionRes),
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 1.dp),
            )
            Text(
                text     = exercise.duration,
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.primary.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── Hero technique card (full-width bento) ────────────────────────────────────

@Composable
private fun HeroTechniqueCard(
    exercise: QuickExercise,
    colors: AppColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "heroCardScale",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary.copy(alpha = 0.16f),
                        colors.surface.copy(alpha = 0.90f),
                    )
                )
            )
            .border(1.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(colors.primary.copy(alpha = 0.16f), RoundedCornerShape(16.dp)),
        ) {
            Icon(
                imageVector        = exercise.icon,
                contentDescription = exercise.label,
                tint               = colors.primary,
                modifier           = Modifier.size(28.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text          = stringResource(R.string.home_featured_section),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.primary.copy(alpha = 0.70f),
                letterSpacing = 2.sp,
                modifier      = Modifier.padding(bottom = 2.dp),
            )
            Text(
                text       = exercise.label,
                style      = MaterialTheme.typography.titleMedium,
                color      = colors.title,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text     = stringResource(exercise.descriptionRes),
                style    = MaterialTheme.typography.bodySmall,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text     = exercise.duration,
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.primary.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint               = colors.primary,
            modifier           = Modifier.size(18.dp),
        )
    }
}

// ── AI Sleep Story bottom sheet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepStorySheet(
    storyState: StoryState,
    onGenerate: () -> Unit,
    onDismiss:  () -> Unit,
    colors: AppColors,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.surface,
        dragHandle       = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Lucide.MoonStar,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text          = stringResource(R.string.home_ai_sleep_story_title),
                        style         = MaterialTheme.typography.labelSmall,
                        color         = colors.subtitle,
                        letterSpacing = 3.sp,
                        fontWeight    = FontWeight.Bold,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text  = stringResource(R.string.home_close),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (storyState) {
                is StoryState.Idle -> {
                    Text(
                        text      = stringResource(R.string.home_generate_story_hint),
                        style     = MaterialTheme.typography.bodySmall,
                        color     = colors.subtitle,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 8.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .clickable(onClick = onGenerate)
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text       = stringResource(R.string.home_generate_story_button),
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                is StoryState.Loading -> {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = stringResource(R.string.home_crafting_story),
                        style     = MaterialTheme.typography.bodySmall,
                        color     = colors.subtitle,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = Lucide.MoonStar,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(36.dp),
                    )
                }

                is StoryState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background, RoundedCornerShape(16.dp))
                            .padding(18.dp),
                    ) {
                        Text(
                            text  = storyState.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface.copy(alpha = 0.6f))
                            .border(1.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onGenerate)
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text       = stringResource(R.string.home_generate_another),
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                is StoryState.Error -> {
                    Text(
                        text      = storyState.message,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = colors.subtitle,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .clickable(onClick = onGenerate)
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text       = stringResource(R.string.home_try_again),
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Text(
                text          = stringResource(R.string.home_powered_by_gemini),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle.copy(alpha = 0.30f),
                letterSpacing = 1.5.sp,
                modifier      = Modifier.padding(top = 16.dp),
            )
        }
    }
}
