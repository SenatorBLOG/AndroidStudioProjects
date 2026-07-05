package com.breatheonline.breathe.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import com.composables.icons.lucide.ChartNoAxesColumn
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.FlaskConical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Waves
import com.composables.icons.lucide.Wind

// ── Quiz data ─────────────────────────────────────────────────────────────────

private enum class BreathTag { STRESS, SHALLOW, DEEP }

private data class QuizQuestion(@StringRes val textRes: Int, val opts: List<Pair<Int, BreathTag>>)

private val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        R.string.interactive_quiz_question_breathing_from,
        listOf(
            R.string.interactive_quiz_option_chest to BreathTag.SHALLOW,
            R.string.interactive_quiz_option_belly to BreathTag.DEEP,
            R.string.interactive_quiz_option_not_sure to BreathTag.STRESS,
        ),
    ),
    QuizQuestion(
        R.string.interactive_quiz_question_mouth,
        listOf(
            R.string.interactive_quiz_option_often to BreathTag.SHALLOW,
            R.string.interactive_quiz_option_sometimes to BreathTag.STRESS,
            R.string.interactive_quiz_option_rarely to BreathTag.DEEP,
        ),
    ),
    QuizQuestion(
        R.string.interactive_quiz_question_bedtime,
        listOf(
            R.string.interactive_quiz_option_racing_thoughts to BreathTag.STRESS,
            R.string.interactive_quiz_option_a_bit_restless to BreathTag.SHALLOW,
            R.string.interactive_quiz_option_calm to BreathTag.DEEP,
        ),
    ),
    QuizQuestion(
        R.string.interactive_quiz_question_sigh_yawn,
        listOf(
            R.string.interactive_quiz_option_yes_constantly to BreathTag.STRESS,
            R.string.interactive_quiz_option_sometimes to BreathTag.SHALLOW,
            R.string.interactive_quiz_option_not_really to BreathTag.DEEP,
        ),
    ),
    QuizQuestion(
        R.string.interactive_quiz_question_energy_midday,
        listOf(
            R.string.interactive_quiz_option_exhausted to BreathTag.STRESS,
            R.string.interactive_quiz_option_a_bit_tired to BreathTag.SHALLOW,
            R.string.interactive_quiz_option_still_going to BreathTag.DEEP,
        ),
    ),
)

private data class QuizResult(
    val icon:        ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @StringRes val techniqueRes: Int,
    val color:       Color,
    val route:       String,
    val planRes:     List<Int>,
)

private val QUIZ_RESULTS = mapOf(
    "stress" to QuizResult(
        icon        = Lucide.CircleAlert,
        titleRes    = R.string.result_stress_breather_title,
        color       = Color(0xFFFF8A8A),
        descRes     = R.string.result_stress_breather_desc,
        techniqueRes = R.string.result_stress_breather_technique,
        route       = "4-7-8",
        planRes     = listOf(
            R.string.interactive_plan_stress_day1_2,
            R.string.interactive_plan_stress_day3_5,
            R.string.interactive_plan_stress_day6_7,
        ),
    ),
    "shallow" to QuizResult(
        icon        = Lucide.Wind,
        titleRes    = R.string.result_shallow_breather_title,
        color       = Color(0xFFFFD97D),
        descRes     = R.string.result_shallow_breather_desc,
        techniqueRes = R.string.result_shallow_breather_technique,
        route       = "belly",
        planRes     = listOf(
            R.string.interactive_plan_shallow_day1_2,
            R.string.interactive_plan_shallow_day3_5,
            R.string.interactive_plan_shallow_day6_7,
        ),
    ),
    "natural" to QuizResult(
        icon        = Lucide.Waves,
        titleRes    = R.string.result_natural_breather_title,
        color       = Color(0xFF4AE8A0),
        descRes     = R.string.result_natural_breather_desc,
        techniqueRes = R.string.result_natural_breather_technique,
        route       = "coherent",
        planRes     = listOf(
            R.string.interactive_plan_natural_day1_2,
            R.string.interactive_plan_natural_day3_5,
            R.string.interactive_plan_natural_day6_7,
        ),
    ),
)

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun InteractiveScreen(navController: NavController, colors: AppColors) {
    var tab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint               = colors.title,
                )
            }
            Text(
                text   = stringResource(R.string.interactive_breathing_tools),
                style  = MaterialTheme.typography.headlineSmall,
                color  = colors.title,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Tab switcher ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface.copy(alpha = 0.7f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabBtn(Lucide.FlaskConical, stringResource(R.string.interactive_breathing_quiz_tab), tab == 0, colors, Modifier.weight(1f)) { tab = 0 }
            TabBtn(Lucide.ChartNoAxesColumn, stringResource(R.string.interactive_stress_score_tab), tab == 1, colors, Modifier.weight(1f)) { tab = 1 }
        }

        Spacer(Modifier.height(16.dp))

        // ── Content ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .padding(20.dp),
        ) {
            if (tab == 0) QuizSection(colors = colors, navController = navController)
            else          StressSection(colors = colors, navController = navController)
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Tab button ────────────────────────────────────────────────────────────────

@Composable
private fun TabBtn(icon: ImageVector, label: String, active: Boolean, colors: AppColors, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) colors.background else Color.Transparent)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) colors.title else colors.subtitle,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = if (active) colors.title else colors.subtitle,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize   = 11.sp,
                ),
            )
        }
    }
}

// ── Quiz ──────────────────────────────────────────────────────────────────────

@Composable
private fun QuizSection(colors: AppColors, navController: NavController) {
    val answers  = remember { Array<BreathTag?>(5) { null } }
    var step     by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<BreathTag?>(null) }

    if (step == 5) {
        // Tally results
        val counts = mapOf(BreathTag.STRESS to 0, BreathTag.SHALLOW to 0, BreathTag.DEEP to 0)
            .toMutableMap()
        answers.forEach { it?.let { t -> counts[t] = (counts[t] ?: 0) + 1 } }
        val resultKey = when {
            (counts[BreathTag.DEEP] ?: 0) >= 3    -> "natural"
            (counts[BreathTag.SHALLOW] ?: 0) >= 3 -> "shallow"
            else                                   -> "stress"
        }
        // Safe fallback: resultKey is always one of the 3 keys above, but guard against
        // map drift to avoid runtime crash.
        val result = QUIZ_RESULTS[resultKey] ?: QUIZ_RESULTS.values.firstOrNull() ?: return
        QuizResult(
            result       = result,
            colors       = colors,
            navController = navController,
            onReset      = {
                answers.fill(null)
                step     = 0
                selected = null
            },
        )
        return
    }

    val q = QUIZ_QUESTIONS[step]

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Progress bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.fillMaxWidth(),
        ) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                i < step  -> colors.primary
                                i == step -> colors.primary.copy(alpha = 0.5f)
                                else      -> colors.subtitle.copy(alpha = 0.18f)
                            }
                        ),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text  = stringResource(R.string.interactive_question_progress, step + 1),
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colors.label,
                    fontSize      = 10.sp,
                    letterSpacing = 1.sp,
                ),
            )
            Text(text = stringResource(q.textRes), style = MaterialTheme.typography.titleSmall.copy(color = colors.title))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            q.opts.forEach { (labelRes, tag) ->
                val on = selected == tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (on) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (on) colors.primary.copy(alpha = 0.5f)
                            else    colors.subtitle.copy(alpha = 0.18f),
                            RoundedCornerShape(14.dp),
                        )
                        .clickable { selected = tag }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text  = stringResource(labelRes),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (on) colors.title else colors.subtitle,
                        ),
                    )
                }
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
                    val sel = selected ?: return@Button
                    answers[step] = sel
                    selected = null
                    step++
                },
                enabled = selected != null,
                shape   = RoundedCornerShape(14.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor  = colors.primary,
                    contentColor    = colors.onPrimary,
                    disabledContainerColor = colors.primary.copy(alpha = 0.3f),
                    disabledContentColor   = colors.onPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text  = if (step < 4) stringResource(R.string.interactive_next) else stringResource(R.string.interactive_see_result),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun QuizResult(
    result:        QuizResult,
    colors: AppColors,
    navController: NavController,
    onReset:       () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = result.icon,
            contentDescription = null,
            tint = result.color,
            modifier = Modifier.size(48.dp),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text  = stringResource(result.titleRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    color      = result.color,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text      = stringResource(result.descRes),
                style     = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
                textAlign = TextAlign.Center,
            )
        }

        // 7-day plan card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.background.copy(alpha = 0.6f))
                .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text  = stringResource(R.string.interactive_seven_day_plan),
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colors.label,
                    letterSpacing = 1.sp,
                ),
            )
            result.planRes.forEach { lineRes ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("→", color = result.color, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text  = stringResource(lineRes),
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
                    )
                }
            }
        }

        // Start technique button
        Button(
            onClick = { navController.navigate(Route.breathe(result.route)) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor   = colors.onPrimary,
            ),
        ) {
            Text(
                text  = stringResource(R.string.interactive_start_technique, stringResource(result.techniqueRes)),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(onClick = onReset) {
            Text(
                text  = stringResource(R.string.interactive_retake_quiz),
                style = MaterialTheme.typography.bodySmall.copy(color = colors.label),
            )
        }
    }
}

// ── Stress Calculator ─────────────────────────────────────────────────────────

@Composable
private fun StressSection(colors: AppColors, navController: NavController) {
    var sleep      by remember { mutableFloatStateOf(5f) }
    var anxiety    by remember { mutableFloatStateOf(5f) }
    var focus      by remember { mutableFloatStateOf(5f) }
    var energy     by remember { mutableFloatStateOf(5f) }
    var showResult by remember { mutableStateOf(false) }

    val score = ((sleep / 10f) * 25 + ((10 - anxiety) / 10f) * 25 + (focus / 10f) * 25 + (energy / 10f) * 25).toInt()

    val scoreColor = when {
        score >= 76 -> Color(0xFF4AE8A0)
        score >= 51 -> Color(0xFF4A9EFF)
        score >= 31 -> Color(0xFFFFD97D)
        else        -> Color(0xFFFF8A8A)
    }
    val scoreLabel = when {
        score >= 76 -> stringResource(R.string.interactive_score_low_stress)
        score >= 51 -> stringResource(R.string.journal_mood_balanced)
        score >= 31 -> stringResource(R.string.interactive_score_moderate_stress)
        else        -> stringResource(R.string.interactive_score_high_stress)
    }

    if (showResult) {
        StressResult(
            score      = score,
            scoreColor = scoreColor,
            scoreLabel = scoreLabel,
            sleep      = sleep,
            anxiety    = anxiety,
            focus      = focus,
            energy     = energy,
            colors     = colors,
            navController = navController,
            onBack     = { showResult = false },
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text  = stringResource(R.string.interactive_rate_how_feel),
            style = MaterialTheme.typography.titleSmall.copy(color = colors.title),
        )

        listOf(
            StressSliderData(stringResource(R.string.interactive_sleep_quality), sleep,   colors.primary,             false) { sleep   = it },
            StressSliderData(stringResource(R.string.interactive_anxiety_level), anxiety, Color(0xFFFF8A8A),          true)  { anxiety = it },
            StressSliderData(stringResource(R.string.interactive_focus),         focus,   colors.primary.copy(0.75f), false) { focus   = it },
            StressSliderData(stringResource(R.string.interactive_energy),        energy,  Color(0xFFFFD97D),          false) { energy  = it },
        ).forEach { data ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = data.label,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
                    )
                    Text(
                        text  = "${data.value.toInt()}/10",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color      = data.color,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                Slider(
                    value         = data.value,
                    onValueChange = data.onChange,
                    valueRange    = 1f..10f,
                    steps         = 8,
                    colors        = SliderDefaults.colors(
                        thumbColor         = data.color,
                        activeTrackColor   = data.color,
                        inactiveTrackColor = colors.subtitle.copy(alpha = 0.18f),
                        activeTickColor    = Color.Transparent,
                        inactiveTickColor  = Color.Transparent,
                    ),
                )
            }
        }

        // Live preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.background.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = stringResource(R.string.interactive_your_stress_score),
                style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text  = "$score",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = scoreColor,
                        fontWeight = FontWeight.Light,
                    ),
                )
                Text(
                    text  = scoreLabel,
                    style = MaterialTheme.typography.labelSmall.copy(color = scoreColor),
                )
            }
        }

        Button(
            onClick  = { showResult = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor   = colors.onPrimary,
            ),
        ) {
            Text(
                text  = stringResource(R.string.interactive_calculate_plan),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private data class StressSliderData(
    val label:    String,
    val value:    Float,
    val color:    Color,
    val invert:   Boolean,
    val onChange: (Float) -> Unit,
)

@Composable
private fun StressResult(
    score:      Int,
    scoreColor: Color,
    scoreLabel: String,
    sleep:      Float,
    anxiety:    Float,
    focus:      Float,
    energy:     Float,
    colors: AppColors,
    navController: NavController,
    onBack:     () -> Unit,
) {
    val breakdown = buildList {
        if (sleep   <= 4f) add(stringResource(R.string.interactive_breakdown_sleep_lever))
        if (anxiety >= 7f) add(stringResource(R.string.interactive_breakdown_anxiety_spiking))
        if (focus   <= 4f) add(stringResource(R.string.interactive_breakdown_focus_fatigue))
        if (energy  <= 4f) add(stringResource(R.string.interactive_breakdown_energy_help))
        if (isEmpty()) {
            add(stringResource(R.string.interactive_breakdown_healthy_balance))
            add(stringResource(R.string.interactive_breakdown_coherent_deeper))
            add(stringResource(R.string.interactive_breakdown_evening_wind_down))
        }
    }.take(3)

    val planRoute = when {
        score < 50 -> "4-7-8"
        else       -> "coherent"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Score display
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text  = "$score",
                style = MaterialTheme.typography.displaySmall.copy(
                    color      = scoreColor,
                    fontWeight = FontWeight.Light,
                ),
            )
            Text(
                text  = scoreLabel,
                style = MaterialTheme.typography.titleSmall.copy(color = scoreColor),
            )
        }

        // Score bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.subtitle.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(scoreColor),
            )
        }

        // Breakdown card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.background.copy(alpha = 0.6f))
                .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text  = stringResource(R.string.interactive_whats_driving),
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colors.label,
                    letterSpacing = 1.sp,
                ),
            )
            breakdown.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("→", color = scoreColor, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text  = item,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
                    )
                }
            }
        }

        Button(
            onClick  = { navController.navigate(Route.breathe(planRoute)) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor   = colors.onPrimary,
            ),
        ) {
            Text(
                text  = stringResource(R.string.interactive_start_your_plan),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(onClick = onBack) {
            Text(
                text  = stringResource(R.string.interactive_adjust_scores),
                style = MaterialTheme.typography.bodySmall.copy(color = colors.label),
            )
        }
    }
}
