package com.breatheonline.breathe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.SessionFeedback
import java.time.LocalTime

// ── Data ──────────────────────────────────────────────────────────────────────

private val FEELINGS = listOf(
    "focused"    to "🎯",
    "calm"       to "🌊",
    "energized"  to "⚡",
    "drowsy"     to "💤",
    "distracted" to "🌀",
    "peaceful"   to "☮️",
    "anxious"    to "😰",
    "refreshed"  to "🌿",
)

private val NOISE_OPTS = listOf(
    "Silent"   to "🔇",
    "Quiet"    to "🔉",
    "Moderate" to "🔊",
    "Noisy"    to "📢",
)

private fun autoTimeOfDay(): String = when (LocalTime.now().hour) {
    in 5..11  -> "Morning"
    in 12..16 -> "Afternoon"
    in 17..20 -> "Evening"
    else      -> "Night"
}

// ── Main composable ───────────────────────────────────────────────────────────

@Composable
fun SessionFeedbackModal(
    visible: Boolean,
    colors: AppColors,
    onSubmit: (SessionFeedback) -> Unit,
    onDismiss: () -> Unit,
) {
    val timeOfDay         = remember { autoTimeOfDay() }
    var step              by remember { mutableStateOf(0) }
    var moodBefore        by remember { mutableStateOf(5f) }
    var moodAfter         by remember { mutableStateOf(5f) }
    var focusLevel        by remember { mutableStateOf(5) }
    var calmnessScore     by remember { mutableStateOf(5) }
    var breathingDepth    by remember { mutableStateOf(5) }
    var stressLevel       by remember { mutableStateOf(5) }
    var distractionCount  by remember { mutableStateOf(0) }
    var noiseLevel        by remember { mutableStateOf("Quiet") }
    var notes             by remember { mutableStateOf("") }
    var feelings          by remember { mutableStateOf(emptySet<String>()) }

    fun buildFeedback() = SessionFeedback(
        moodBefore = moodBefore.toInt(),
        moodAfter = moodAfter.toInt(),
        focusLevel = focusLevel,
        stressLevel = stressLevel,
        breathingDepth = breathingDepth,
        calmnessScore = calmnessScore,
        distractionCount = distractionCount,
        noiseLevel = noiseLevel,
        notes = buildList {
            if (notes.isNotBlank()) add(notes.trim())
            if (feelings.isNotEmpty()) add("Feelings: ${feelings.joinToString(", ")}")
        }.joinToString(" | "),
        timeOfDay = timeOfDay,
    )

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(250)),
        exit    = fadeOut(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.88f))
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* consume backdrop clicks */ },
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter   = slideInVertically(tween(380)) { it } + fadeIn(tween(300, delayMillis = 60)),
                exit    = slideOutVertically(tween(280)) { it } + fadeOut(tween(200)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(colors.surface)
                        .navigationBarsPadding(),
                ) {
                    // ── Handle bar ────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.label.copy(alpha = 0.3f)),
                    )

                    // ── Header ────────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "Session complete",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color    = colors.title,
                                    fontSize = 15.sp,
                                ),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = "Your data trains your personal AI coach",
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.label),
                            )
                        }
                        if (step == 0) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text  = "Skip",
                                    style = MaterialTheme.typography.labelMedium.copy(color = colors.label),
                                )
                            }
                        }
                    }

                    // ── Step progress dots ─────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (i == step) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i <= step) colors.primary else colors.label.copy(alpha = 0.3f)
                                    ),
                            )
                        }
                    }

                    // ── Step content (scrollable) ──────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 320.dp)
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (step) {
                            0 -> StepMood(
                                colors   = colors,
                                before   = moodBefore,
                                after    = moodAfter,
                                onBefore = { moodBefore = it },
                                onAfter  = { moodAfter  = it },
                            )
                            1 -> StepFeelings(
                                colors   = colors,
                                selected = feelings,
                                onToggle = { tag ->
                                    feelings = if (tag in feelings) feelings - tag else feelings + tag
                                },
                            )
                            2 -> StepQuality(
                                colors           = colors,
                                focusLevel       = focusLevel,
                                calmnessScore    = calmnessScore,
                                breathingDepth   = breathingDepth,
                                stressLevel      = stressLevel,
                                distractionCount = distractionCount,
                                noiseLevel       = noiseLevel,
                                onFocus          = { focusLevel        = it },
                                onCalmness       = { calmnessScore     = it },
                                onBreath         = { breathingDepth    = it },
                                onStress         = { stressLevel       = it },
                                onDistract       = { distractionCount  = it },
                                onNoise          = { noiseLevel        = it },
                            )
                            3 -> StepNotes(
                                colors     = colors,
                                notes      = notes,
                                feelings   = feelings,
                                moodBefore = moodBefore.toInt(),
                                moodAfter  = moodAfter.toInt(),
                                onNotes    = { notes = it },
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Footer ─────────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (step > 0) {
                            OutlinedButton(
                                onClick = { step-- },
                                shape   = RoundedCornerShape(12.dp),
                                border  = BorderStroke(1.dp, colors.label.copy(alpha = 0.3f)),
                                colors  = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.label,
                                ),
                            ) {
                                Text(
                                    text  = "Back",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (step < 3) step++
                                else onSubmit(buildFeedback())
                            },
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor   = colors.onPrimary,
                            ),
                        ) {
                            Text(
                                text  = if (step < 3) "Continue →" else "Save ✓",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Step header (shared) ──────────────────────────────────────────────────────

@Composable
private fun StepHeader(colors: AppColors, step: Int, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = "STEP $step OF 4",
            style = MaterialTheme.typography.labelSmall.copy(
                color         = colors.label,
                letterSpacing = 2.sp,
                fontSize      = 10.sp,
            ),
        )
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color    = colors.title,
                fontSize = 16.sp,
            ),
        )
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
        )
    }
}

// ── Step 1: Mood Before / After ───────────────────────────────────────────────

@Composable
private fun StepMood(
    colors: AppColors,
    before: Float, after: Float,
    onBefore: (Float) -> Unit, onAfter: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(colors, 1, "How did your mood shift?", "Slide before & after to track your progress")
        MoodSliderRow("BEFORE", before, colors.label,   colors, onBefore)
        MoodSliderRow("AFTER",  after,  colors.primary, colors, onAfter)

        // Delta badge
        val delta = after.toInt() - before.toInt()
        val (badgeText, badgeTint) = when {
            delta > 0 -> "+$delta better" to colors.primary
            delta < 0 -> "$delta worse"   to Color(0xFFFF8A8A)
            else      -> "no change"      to colors.label
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(colors.label.copy(alpha = 0.18f)))
            Text(
                text     = badgeText,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeTint.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                style    = MaterialTheme.typography.labelSmall.copy(
                    color      = badgeTint,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Box(Modifier.weight(1f).height(1.dp).background(colors.label.copy(alpha = 0.18f)))
        }
    }
}

@Composable
private fun MoodSliderRow(
    label: String, value: Float, thumbColor: Color,
    colors: AppColors, onChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text     = label,
            modifier = Modifier.width(52.dp),
            style    = MaterialTheme.typography.labelSmall.copy(
                color         = colors.label,
                letterSpacing = 1.2.sp,
            ),
        )
        Slider(
            value         = value,
            onValueChange = onChange,
            valueRange    = 1f..10f,
            steps         = 8,
            modifier      = Modifier.weight(1f),
            colors        = SliderDefaults.colors(
                thumbColor         = thumbColor,
                activeTrackColor   = thumbColor,
                inactiveTrackColor = colors.label.copy(alpha = 0.22f),
                activeTickColor    = Color.Transparent,
                inactiveTickColor  = Color.Transparent,
            ),
        )
        Text(
            text      = value.toInt().toString(),
            modifier  = Modifier.width(20.dp),
            style     = MaterialTheme.typography.labelMedium.copy(color = colors.title),
            textAlign = TextAlign.End,
        )
    }
}

// ── Step 2: Feelings ──────────────────────────────────────────────────────────

@Composable
private fun StepFeelings(
    colors: AppColors,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(colors, 2, "How do you feel right now?", "Pick all that apply")
        FEELINGS.chunked(4).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (key, emoji) ->
                    val on = key in selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (on) colors.primary.copy(alpha = 0.16f)
                                else    colors.background.copy(alpha = 0.5f),
                            )
                            .border(
                                1.dp,
                                if (on) colors.primary.copy(alpha = 0.5f)
                                else    colors.label.copy(alpha = 0.18f),
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { onToggle(key) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(emoji, fontSize = if (on) 20.sp else 16.sp)
                            Text(
                                text  = key.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color    = if (on) colors.title else colors.label,
                                    fontSize = 9.sp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Step 3: Session Quality ───────────────────────────────────────────────────

@Composable
private fun StepQuality(
    colors: AppColors,
    focusLevel: Int, calmnessScore: Int, breathingDepth: Int,
    stressLevel: Int, distractionCount: Int, noiseLevel: String,
    onFocus: (Int) -> Unit, onCalmness: (Int) -> Unit, onBreath: (Int) -> Unit,
    onStress: (Int) -> Unit, onDistract: (Int) -> Unit, onNoise: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        StepHeader(
            colors   = colors, step = 3,
            title    = "Session quality",
            subtitle = "Quick ratings help your AI coach personalise your sessions",
        )
        DotSliderRow("Focus level",   focusLevel,       10, colors.primary,                  colors, onFocus)
        DotSliderRow("Calmness",      calmnessScore,    10, colors.primary.copy(alpha = 0.7f), colors, onCalmness)
        DotSliderRow("Breath depth",  breathingDepth,   10, colors.primary,                  colors, onBreath)
        DotSliderRow("Stress level",  stressLevel,      10, Color(0xFFFF8A8A),               colors, onStress)
        DotSliderRow("Distractions",  distractionCount, 10, colors.primary.copy(alpha = 0.6f), colors, onDistract)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text  = "Noise level",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NOISE_OPTS.forEach { (value, emoji) ->
                    val on = value == noiseLevel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (on) colors.primary.copy(alpha = 0.16f)
                                else    colors.background.copy(alpha = 0.5f),
                            )
                            .border(
                                1.dp,
                                if (on) colors.primary.copy(alpha = 0.5f)
                                else    colors.label.copy(alpha = 0.18f),
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { onNoise(value) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(emoji, fontSize = if (on) 18.sp else 14.sp)
                            Text(
                                text  = value,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color    = if (on) colors.title else colors.label,
                                    fontSize = 9.sp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DotSliderRow(
    label: String, value: Int, max: Int,
    dotColor: Color, colors: AppColors,
    onChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (i in 0..max) {
                Box(
                    modifier = Modifier
                        .size(if (i <= value) 11.dp else 9.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= value) dotColor else colors.label.copy(alpha = 0.22f),
                        )
                        .clickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onChange(i) },
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text  = "$value",
                style = MaterialTheme.typography.labelSmall.copy(color = colors.label),
            )
        }
    }
}

// ── Step 4: Notes ─────────────────────────────────────────────────────────────

@Composable
private fun StepNotes(
    colors: AppColors,
    notes: String, feelings: Set<String>,
    moodBefore: Int, moodAfter: Int,
    onNotes: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row {
            Text(
                text  = "Any thoughts? ",
                style = MaterialTheme.typography.titleSmall.copy(
                    color    = colors.title,
                    fontSize = 16.sp,
                ),
            )
            Text(
                text  = "(optional)",
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = colors.label,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }

        // AI hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.Top,
        ) {
            Text("✨", style = MaterialTheme.typography.bodySmall)
            Text(
                text  = "Your feedback trains your personal AI breathing coach.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.subtitle),
            )
        }

        // Notes input
        BasicTextField(
            value         = notes,
            onValueChange = onNotes,
            modifier      = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background.copy(alpha = 0.6f))
                .border(1.dp, colors.label.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .heightIn(min = 88.dp),
            textStyle     = MaterialTheme.typography.bodySmall.copy(color = colors.text),
            cursorBrush   = SolidColor(colors.primary),
            decorationBox = { inner ->
                Box {
                    if (notes.isEmpty()) {
                        Text(
                            text  = "How did this session feel? Any observations…",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.label),
                        )
                    }
                    inner()
                }
            },
        )

        // Summary chips
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            SummaryChip("mood $moodBefore→$moodAfter", colors.label, colors)
            feelings.forEach { f -> SummaryChip(f, colors.primary, colors) }
        }
    }
}

@Composable
private fun SummaryChip(text: String, tint: Color, colors: AppColors) {
    Text(
        text     = text,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.10f))
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style    = MaterialTheme.typography.labelSmall.copy(
            color    = tint,
            fontSize = 9.sp,
        ),
    )
}
