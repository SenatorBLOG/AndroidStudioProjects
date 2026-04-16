package com.breatheonline.breathe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.GuidanceMode
import com.breatheonline.breathe.viewmodel.VoiceGender

// ── GuidancePicker ────────────────────────────────────────────────────────────

@Composable
fun GuidancePicker(
    mode: GuidanceMode,
    voiceGender: VoiceGender,
    colors: AppColors,
    onChange:    (GuidanceMode, VoiceGender) -> Unit,
    modifier:    Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Options panel (slides in above the pill) ───────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter   = slideInVertically(tween(280)) { it / 2 } + fadeIn(tween(200)),
            exit    = slideOutVertically(tween(200)) { it / 2 } + fadeOut(tween(150)),
        ) {
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.subtitle.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text  = "GUIDANCE MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = colors.label,
                            fontSize      = 9.sp,
                            letterSpacing = 2.sp,
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )

                    ModeOption(GuidanceMode.SILENT,    "🔇", "Silent",    mode, colors) {
                        onChange(it, voiceGender); expanded = false
                    }
                    ModeOption(GuidanceMode.VIBRATION, "📳", "Vibration", mode, colors) {
                        onChange(it, voiceGender); expanded = false
                    }
                    ModeOption(GuidanceMode.VOICE,     "🎙", "AI Voice",  mode, colors) {
                        onChange(it, voiceGender)
                    }

                    // Voice gender — shown only when voice is selected
                    AnimatedVisibility(visible = mode == GuidanceMode.VOICE) {
                        Column {
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.subtitle.copy(alpha = 0.12f)),
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text  = "VOICE STYLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = colors.label,
                                    fontSize      = 9.sp,
                                    letterSpacing = 2.sp,
                                ),
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GenderButton(
                                    gender   = VoiceGender.FEMALE,
                                    icon     = "👩",
                                    label    = "Female",
                                    current  = voiceGender,
                                    colors   = colors,
                                    modifier = Modifier.weight(1f),
                                ) { onChange(mode, it) }
                                GenderButton(
                                    gender   = VoiceGender.MALE,
                                    icon     = "👨",
                                    label    = "Male",
                                    current  = voiceGender,
                                    colors   = colors,
                                    modifier = Modifier.weight(1f),
                                ) { onChange(mode, it) }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Pill trigger ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (expanded) colors.primary.copy(alpha = 0.14f)
                    else          colors.surface,
                )
                .border(
                    1.dp,
                    if (expanded) colors.primary.copy(alpha = 0.40f)
                    else          colors.subtitle.copy(alpha = 0.20f),
                    RoundedCornerShape(20.dp),
                )
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { expanded = !expanded }
                .padding(horizontal = 18.dp, vertical = 9.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text     = mode.icon(),
                    fontSize = 14.sp,
                )
                Text(
                    text  = mode.label(),
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.subtitle),
                )
                Text(
                    text     = if (expanded) "▴" else "▾",
                    style    = MaterialTheme.typography.labelSmall.copy(
                        color    = colors.label,
                        fontSize = 8.sp,
                    ),
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun GuidanceMode.icon()  = when (this) {
    GuidanceMode.SILENT    -> "🔇"
    GuidanceMode.VIBRATION -> "📳"
    GuidanceMode.VOICE     -> "🎙"
}

private fun GuidanceMode.label() = when (this) {
    GuidanceMode.SILENT    -> "Silent"
    GuidanceMode.VIBRATION -> "Vibration"
    GuidanceMode.VOICE     -> "AI Voice"
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ModeOption(
    target: GuidanceMode,
    icon:    String,
    label:   String,
    current: GuidanceMode,
    colors: AppColors,
    onSelect: (GuidanceMode) -> Unit,
) {
    val on = target == current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (on) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                1.dp,
                if (on) colors.primary.copy(alpha = 0.32f) else Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .clickable { onSelect(target) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Radio dot
        Box(
            modifier         = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .border(2.dp, if (on) colors.primary else colors.subtitle.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (on) Box(Modifier.size(6.dp).clip(CircleShape).background(colors.primary))
        }
        Text(icon, fontSize = 15.sp)
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (on) colors.title else colors.subtitle,
            ),
        )
    }
}

@Composable
private fun GenderButton(
    gender: VoiceGender,
    icon:     String,
    label:    String,
    current: VoiceGender,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onSelect: (VoiceGender) -> Unit,
) {
    val on = gender == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (on) colors.primary.copy(alpha = 0.12f)
                else    colors.background.copy(alpha = 0.5f),
            )
            .border(
                1.dp,
                if (on) colors.primary.copy(alpha = 0.40f)
                else    colors.subtitle.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp),
            )
            .clickable { onSelect(gender) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(icon, fontSize = 18.sp)
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color    = if (on) colors.title else colors.label,
                    fontSize = 9.sp,
                ),
            )
        }
    }
}
