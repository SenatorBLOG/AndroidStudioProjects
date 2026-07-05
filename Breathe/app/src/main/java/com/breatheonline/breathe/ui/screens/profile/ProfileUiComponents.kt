package com.breatheonline.breathe.ui.screens.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors

internal data class ChoiceItem(
    val value: String,
    @StringRes val labelRes: Int,
)

internal val GOALS = listOf(
    ChoiceItem("sleep", R.string.goal_better_sleep),
    ChoiceItem("stress", R.string.goal_less_stress),
    ChoiceItem("focus", R.string.goal_more_focus),
    ChoiceItem("energy", R.string.goal_more_energy),
    ChoiceItem("general", R.string.goal_general_wellness),
)

internal val GENDERS = listOf(
    ChoiceItem("male", R.string.gender_male),
    ChoiceItem("female", R.string.gender_female),
    ChoiceItem("other", R.string.gender_other),
    ChoiceItem("prefer_not", R.string.gender_prefer_not_to_say),
)

@Composable
internal fun SectionCard(title: String, colors: AppColors, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
internal fun ProfileChip(
    text: String,
    colors: AppColors,
    active: Boolean,
    icon: ImageVector? = null,
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (active) colors.primary else colors.subtitle,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = text,
                color = if (active) colors.primary else colors.subtitle,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
