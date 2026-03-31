package com.example.breathe.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathe.ui.theme.AppColors

// ── Shared display-label catalogues ──────────────────────────────────────────

internal val GOALS = listOf(
    "sleep"   to "😴 Better Sleep",
    "stress"  to "🧘 Less Stress",
    "focus"   to "🎯 More Focus",
    "energy"  to "⚡ More Energy",
    "general" to "🌊 General Wellness",
)

internal val GENDERS = listOf(
    "male"       to "Male",
    "female"     to "Female",
    "other"      to "Other",
    "prefer_not" to "Prefer not to say",
)

// ── SectionCard — titled card used by every tab ───────────────────────────────

@Composable
internal fun SectionCard(title: String, colors: AppColors, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text          = title.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// ── ProfileChip — pill badge used in HeaderCard and ProfileFormTab ─────────────

@Composable
internal fun ProfileChip(
    text:    String,
    colors:  AppColors,
    active:  Boolean,
    onClick: (() -> Unit)? = null,
) {
    val base = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        base
            .background(
                color = if (active) colors.primary.copy(alpha = 0.10f) else colors.background,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = if (active) colors.primary else colors.subtitle.copy(alpha = 0.20f),
                shape = CircleShape,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text  = text,
            color = if (active) colors.primary else colors.subtitle,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
