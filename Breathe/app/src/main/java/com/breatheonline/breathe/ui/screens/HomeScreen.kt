package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontStyle
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
import kotlinx.coroutines.delay
import java.time.LocalTime

// ── Quick-exercise data ────────────────────────────────────────────────────────

private data class QuickExercise(
    val routeType: String,
    val label:     String,
    val duration:  String,
    val icon:      ImageVector,
    val description: String,
)

private val QUICK_EXERCISES = listOf(
    QuickExercise("4-7-8",     "4-7-8",        "4 · 7 · 8",     Icons.Filled.Air,             "Calm anxiety fast"),
    QuickExercise("box",       "Box",           "4 · 4 · 4 · 4", Icons.Filled.CropSquare,      "Focus & reset"),
    QuickExercise("wimhof",    "Wim Hof",       "2 · 1 · 2",     Icons.Filled.LocalFireDepartment, "Energy & vitality"),
    QuickExercise("coherent",  "Coherent",      "5 · 5",         Icons.Filled.Favorite,        "Heart rate balance"),
    QuickExercise("belly",     "Belly",         "4 · 6 · 2",     Icons.Filled.SelfImprovement, "Deep relaxation"),
    QuickExercise("morning",   "Morning",       "4 · 4 · 4",     Icons.Filled.WbSunny,         "Gentle wake-up"),
    QuickExercise("alternate", "Alternate",     "4 · 4 · 4 · 2", Icons.Filled.Loop,            "Balance & clarity"),
)

// ── Quotes ────────────────────────────────────────────────────────────────────

private val QUICK_EXERCISE_ROWS = QUICK_EXERCISES.chunked(2)

private data class Quote(val text: String, val author: String)

private val QUOTES = listOf(
    Quote("The breath is the bridge which connects life to consciousness.", "Thich Nhat Hanh"),
    Quote("Almost everything will work again if you unplug it for a few minutes — including you.", "Anne Lamott"),
    Quote("Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure.", "Oprah Winfrey"),
    Quote("Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.", "Thich Nhat Hanh"),
    Quote("Your breath is your greatest tool to reset the nervous system.", "Andrew Huberman"),
    Quote("Breathing is the first act of life, and the last. Our very life depends on it.", "Joseph Pilates"),
    Quote("When you own your breath, nobody can steal your peace.", "Unknown"),
    Quote("The present moment always will have been. Breathe and be here.", "Unknown"),
)

// ── Explore cards ─────────────────────────────────────────────────────────────

private sealed interface ExploreAction {
    data class Nav(val route: String) : ExploreAction
    data class Web(val url: String)   : ExploreAction
}

private data class ExploreCard(
    val emoji:  String,
    val title:  String,
    val tag:    String,
    val action: ExploreAction,
)

private val EXPLORE_CARDS = listOf(
    ExploreCard("🌊", "Breathwork for Deep Sleep",    "Guide",     ExploreAction.Web("https://breatheonline.app/sleep/breathwork-for-deep-sleep")),
    ExploreCard("⚡", "Why Slow Breathing Calms You", "Science",   ExploreAction.Web("https://breatheonline.app/science/slow-breathing")),
    ExploreCard("🧘", "Morning Ritual",               "Practice",  ExploreAction.Nav(Route.breathe("morning"))),
    ExploreCard("📝", "Mood & Journal",               "Track",     ExploreAction.Nav(Route.JOURNAL)),
    ExploreCard("📊", "Session History",              "Track",     ExploreAction.Nav(Route.SESSION_HISTORY)),
    ExploreCard("🌍", "Community Globe",              "Community", ExploreAction.Nav(Route.GLOBE)),
)

// ── Sleep guides ──────────────────────────────────────────────────────────────

private data class SleepGuide(val emoji: String, val title: String, val url: String)

private val SLEEP_GUIDES = listOf(
    SleepGuide("💤", "Why Sleep is So Important",    "https://breatheonline.app/sleep/why-sleep-is-important"),
    SleepGuide("😮", "What is Sleep Apnea?",         "https://breatheonline.app/sleep/what-is-sleep-apnea"),
    SleepGuide("🌊", "Breathwork for Deep Sleep",    "https://breatheonline.app/sleep/breathwork-for-deep-sleep"),
    SleepGuide("⚡", "Why Slow Breathing Calms You", "https://breatheonline.app/science/slow-breathing"),
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

    fun enter(delay: Int) =
        fadeIn(tween(500, delay)) + slideInVertically(tween(500, delay)) { it / 10 }

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
            AnimatedVisibility(visible = visible, enter = enter(0)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text          = "BREATHE",
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
                    fontWeight = FontWeight.Light,
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
                        text  = "${state.currentStreak} day streak",
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
                        text  = "${state.todayMinutes} min today",
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
                        Text(
                            text  = "💤 $sleepStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.subtitle,
                            modifier = Modifier.clickable {
                                navController.navigate(Route.history("sleep"))
                            },
                        )
                    }
                }
            }
            }
        }

        item(key = "spacer_after_header") { Spacer(Modifier.height(24.dp)) }



        // ── Breathing techniques grid ─────────────────────────────────────────
        item(key = "quick_exercises_section") {
            AnimatedVisibility(visible = visible, enter = enter(120)) {
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
            AnimatedVisibility(visible = visible, enter = enter(180)) {
                AchievementHighlightsSection(
                    colors = colors,
                    navController = navController,
                )
            }
        }

        item(key = "spacer_after_achievements") { Spacer(Modifier.height(8.dp)) }

        // ── Start Meditation CTA ──────────────────────────────────────────────
        item(key = "start_meditation_cta") {
            AnimatedVisibility(visible = visible, enter = enter(280)) {
            val meditationHaptic = LocalHapticFeedback.current
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
                            meditationHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            text       = "Start Meditation",
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { navController.navigate(Route.FAQ) }) {
                        Text(
                            text  = "How it works →",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                        )
                    }
                    TextButton(onClick = { navController.navigate(Route.INTERACTIVE) }) {
                        Text(
                            text  = "Breathing quiz →",
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
            AnimatedVisibility(visible = visible, enter = enter(200)) {
            val aiCoachHaptic = LocalHapticFeedback.current
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
                        aiCoachHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAiSheet = true
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = "AI Coach",
                        style      = MaterialTheme.typography.titleSmall,
                        color      = colors.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text  = "Personalised breathing guidance and bedtime help",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text  = "→",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.primary,
                )
            }
            }
        }

        item(key = "spacer_after_ai_coach") { Spacer(Modifier.height(12.dp)) }

        item(key = "home_footer_note") {
            Text(
                text = "Community, long-form guides and extra discovery now live off the home feed so this screen stays focused.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.subtitle.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

    }
    } // Box
}

@Composable
private fun UtilityAction(
    label: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
            .border(1.dp, colors.primary.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
    ) {
        Text(
            text = label,
            color = colors.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Soul Orb ──────────────────────────────────────────────────────────────────

@Composable
private fun SoulOrb(colors: AppColors, onClick: () -> Unit, size: Int = 180) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue  = 0.52f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue  = 0.60f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ringAlpha",
    )

    var blinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500L + (0..3500).random())
            blinking = true
            delay(130L)
            blinking = false
        }
    }

    val orbDp  = size.dp
    val eyeDp  = (orbDp.value * 0.72f).dp
    val eyeSz  = (size * 0.085f).dp
    val pupilSz = (size * 0.038f).dp
    val eyeGap  = (size * 0.14f).dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(orbDp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = glowAlpha * 0.65f),
                            colors.primary.copy(alpha = glowAlpha * 0.20f),
                            Color.Transparent,
                        ),
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                        radius = this.size.width / 2f,
                    ),
                )
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(eyeDp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.28f),
                            colors.surface,
                        )
                    )
                )
                .border(1.5.dp, colors.primary.copy(alpha = ringAlpha), CircleShape)
                .clickable(onClick = onClick),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = (size * 0.04f).dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(eyeGap)) {
                    repeat(2) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(eyeSz)
                                .height(if (blinking) 2.dp else eyeSz)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.88f)),
                        ) {
                            if (!blinking) {
                                Box(
                                    modifier = Modifier
                                        .size(pupilSz)
                                        .clip(CircleShape)
                                        .background(colors.background.copy(alpha = 0.75f)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
                text     = exercise.description,
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
                text          = "FEATURED",
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
                text     = exercise.description,
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
        Text(
            text  = "→",
            style = MaterialTheme.typography.titleMedium,
            color = colors.primary,
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
                    Text(text = "\uD83C\uDF19", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text          = "AI SLEEP STORY",
                        style         = MaterialTheme.typography.labelSmall,
                        color         = colors.subtitle,
                        letterSpacing = 3.sp,
                        fontWeight    = FontWeight.Bold,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text  = "Close",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (storyState) {
                is StoryState.Idle -> {
                    Text(
                        text      = "Tap generate for a calming bedtime story personalised just for you.",
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
                            text       = "Generate story",
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                is StoryState.Loading -> {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = "Crafting your bedtime story...",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = colors.subtitle,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "\uD83C\uDF19",
                        fontSize = 36.sp,
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
                            text       = "Generate another",
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
                            text       = "Try again",
                            style      = MaterialTheme.typography.labelLarge,
                            color      = colors.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Text(
                text          = "POWERED BY GEMINI AI",
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle.copy(alpha = 0.30f),
                letterSpacing = 1.5.sp,
                modifier      = Modifier.padding(top = 16.dp),
            )
        }
    }
}

// ── Daily quote strip ─────────────────────────────────────────────────────────

@Composable
private fun QuoteStrip(colors: AppColors) {
    val initial    = remember { QUOTES.indices.random() }
    val pagerState = rememberPagerState(initialPage = initial) { QUOTES.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text          = "DAILY INSPIRATION",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 3.sp,
            modifier      = Modifier.padding(bottom = 12.dp),
        )
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val quote = QUOTES[page]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.primary.copy(alpha = 0.55f)),
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text      = "\u201C${quote.text}\u201D",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = colors.subtitle,
                        fontStyle = FontStyle.Italic,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "\u2014 ${quote.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.label,
                    )
                }
            }
        }
        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            QUOTES.indices.forEach { i ->
                val selected = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (selected) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) colors.primary
                            else          colors.subtitle.copy(alpha = 0.28f)
                        ),
                )
            }
        }
    }
}

// ── Explore section ───────────────────────────────────────────────────────────

@Composable
private fun ExploreSection(navController: NavController, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text          = "EXPLORE",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 3.sp,
            modifier      = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(EXPLORE_CARDS) { card ->
                ExploreCardItem(
                    card   = card,
                    colors = colors,
                    onClick = {
                        when (val a = card.action) {
                            is ExploreAction.Nav -> navController.navigate(a.route)
                            is ExploreAction.Web -> navController.navigate(Route.article(a.url))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ExploreCardItem(
    card:    ExploreCard,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .background(colors.surface, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text  = card.tag,
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(text = card.emoji, fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text       = card.title,
            style      = MaterialTheme.typography.bodySmall,
            color      = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Sleep guides section ──────────────────────────────────────────────────────

@Composable
private fun SleepGuidesSection(navController: NavController, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text          = "SLEEP GUIDES",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 3.sp,
            modifier      = Modifier.padding(bottom = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp)),
        ) {
            SLEEP_GUIDES.forEachIndexed { idx, guide ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Route.article(guide.url)) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                ) {
                    Text(guide.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text     = guide.title,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = colors.title,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text  = "→",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                    )
                }
                if (idx < SLEEP_GUIDES.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(colors.subtitle.copy(alpha = 0.10f)),
                    )
                }
            }
        }
    }
}

