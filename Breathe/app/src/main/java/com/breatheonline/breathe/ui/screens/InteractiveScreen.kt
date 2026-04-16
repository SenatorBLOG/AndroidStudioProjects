package com.breatheonline.breathe.ui.screens

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breatheonline.breathe.ui.theme.AppColors

// ── Quiz data ─────────────────────────────────────────────────────────────────

private enum class BreathTag { STRESS, SHALLOW, DEEP }

private data class QuizQuestion(val text: String, val opts: List<Pair<String, BreathTag>>)

private val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
        "Where do you breathe from?",
        listOf("Chest" to BreathTag.SHALLOW, "Belly" to BreathTag.DEEP, "Not sure" to BreathTag.STRESS),
    ),
    QuizQuestion(
        "Do you breathe through your mouth?",
        listOf("Often" to BreathTag.SHALLOW, "Sometimes" to BreathTag.STRESS, "Rarely" to BreathTag.DEEP),
    ),
    QuizQuestion(
        "How do you feel at bedtime?",
        listOf("Racing thoughts" to BreathTag.STRESS, "A bit restless" to BreathTag.SHALLOW, "Calm" to BreathTag.DEEP),
    ),
    QuizQuestion(
        "Do you sigh or yawn a lot?",
        listOf("Yes, constantly" to BreathTag.STRESS, "Sometimes" to BreathTag.SHALLOW, "Not really" to BreathTag.DEEP),
    ),
    QuizQuestion(
        "Energy by midday?",
        listOf("Exhausted" to BreathTag.STRESS, "A bit tired" to BreathTag.SHALLOW, "Still going" to BreathTag.DEEP),
    ),
)

private data class QuizResult(
    val emoji:     String,
    val title:     String,
    val desc:      String,
    val color:     Color,
    val technique: String,
    val route:     String,
    val plan:      List<String>,
)

private val QUIZ_RESULTS = mapOf(
    "stress" to QuizResult(
        emoji     = "😰",
        title     = "Stress Breather",
        color     = Color(0xFFFF8A8A),
        desc      = "Your nervous system is running hot. Short breath holds and slow exhales will rebalance it fast.",
        technique = "4-7-8 Breathing",
        route     = "4-7-8",
        plan      = listOf("Day 1–2: Reset your baseline with 4-7-8", "Day 3–5: Box breathing for daytime calm", "Day 6–7: Lock in the evening wind-down"),
    ),
    "shallow" to QuizResult(
        emoji     = "🌀",
        title     = "Shallow Breather",
        color     = Color(0xFFFFD97D),
        desc      = "You're using only the top of your lungs. Belly breathing unlocks more oxygen and calm.",
        technique = "Belly Breathing",
        route     = "belly",
        plan      = listOf("Day 1–2: Belly breathing foundation", "Day 3–5: Expand to coherent breathing", "Day 6–7: Full diaphragm activation habit"),
    ),
    "natural" to QuizResult(
        emoji     = "🌊",
        title     = "Natural Breather",
        color     = Color(0xFF4AE8A0),
        desc      = "You have solid breathing instincts. Coherent breathing will take you to the next level.",
        technique = "Coherent Breathing",
        route     = "coherent",
        plan      = listOf("Day 1–2: Establish 5.5 BPM resonance", "Day 3–5: HRV tracking + Wim Hof boost", "Day 6–7: Lock in your peak-performance routine"),
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
                    imageVector        = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = colors.title,
                )
            }
            Text(
                text   = "Breathing Tools",
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
            TabBtn("🧪  Breathing Quiz", tab == 0, colors, Modifier.weight(1f)) { tab = 0 }
            TabBtn("📊  Stress Score",   tab == 1, colors, Modifier.weight(1f)) { tab = 1 }
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
private fun TabBtn(label: String, active: Boolean, colors: AppColors, modifier: Modifier, onClick: () -> Unit) {
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
        val result = QUIZ_RESULTS[resultKey]!!
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
                text  = "Question ${step + 1} of 5",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colors.label,
                    fontSize      = 10.sp,
                    letterSpacing = 1.sp,
                ),
            )
            Text(
                text  = q.text,
                style = MaterialTheme.typography.titleSmall.copy(color = colors.title),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            q.opts.forEach { (label, tag) ->
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
                        text  = label,
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
                    text  = if (step < 4) "Next →" else "See my result →",
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
        Text(result.emoji, fontSize = 48.sp)

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text  = result.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color      = result.color,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text      = result.desc,
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
                text  = "Your 7-day plan",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colors.label,
                    letterSpacing = 1.sp,
                ),
            )
            result.plan.forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("→", color = result.color, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text  = line,
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
                text  = "Start ${result.technique} →",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(onClick = onReset) {
            Text(
                text  = "Retake quiz",
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
        score >= 76 -> "Low Stress"
        score >= 51 -> "Balanced"
        score >= 31 -> "Moderate Stress"
        else        -> "High Stress"
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
            text  = "Rate how you feel today",
            style = MaterialTheme.typography.titleSmall.copy(color = colors.title),
        )

        listOf(
            StressSliderData("Sleep Quality", sleep,   colors.primary,             false) { sleep   = it },
            StressSliderData("Anxiety Level", anxiety, Color(0xFFFF8A8A),          true)  { anxiety = it },
            StressSliderData("Focus",         focus,   colors.primary.copy(0.75f), false) { focus   = it },
            StressSliderData("Energy",        energy,  Color(0xFFFFD97D),          false) { energy  = it },
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
                text  = "Your stress score:",
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
                text  = "Calculate my plan →",
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
        if (sleep   <= 4f) add("Sleep is your biggest lever right now")
        if (anxiety >= 7f) add("Anxiety is spiking your baseline stress")
        if (focus   <= 4f) add("Low focus suggests mental fatigue — try box breathing before work")
        if (energy  <= 4f) add("Morning routine would help energy levels")
        if (isEmpty()) {
            add("Maintain your current healthy balance")
            add("Add Coherent Breathing to go deeper")
            add("Evening wind-down session recommended")
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
                text  = "What's driving your score",
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
                text  = "Start your plan →",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(onClick = onBack) {
            Text(
                text  = "Adjust scores",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.label),
            )
        }
    }
}
