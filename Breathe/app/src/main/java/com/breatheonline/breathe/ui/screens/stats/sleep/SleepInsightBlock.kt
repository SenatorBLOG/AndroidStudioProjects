package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent

@Composable
internal fun SleepInsightBlock(
    paragraph: String,
    prompts: List<String>,
    feedback: Int?,
    onFeedback: (Boolean) -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text(
            text = "Sleep interpretation and suggestions",
            style = MaterialTheme.typography.titleMedium,
            color = colors.title,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Local insight is shown instantly. AI coach only responds when you tap a prompt.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.subtitle,
        )
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleepAccent.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = SleepAccent,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(14.dp))
            InsightFeedbackRow(
                feedback = feedback,
                onFeedback = onFeedback,
                colors = colors,
            )
        }
        Spacer(Modifier.height(14.dp))
        SleepPromptChips(
            prompts = prompts,
            colors = colors,
        )
    }
}

@Composable
private fun InsightFeedbackRow(
    feedback: Int?,
    onFeedback: (Boolean) -> Unit,
    colors: AppColors,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Was this useful?",
            color = colors.title,
            style = MaterialTheme.typography.bodySmall,
        )
        FeedbackChip(
            label = "Helpful",
            selected = feedback == 1,
            onClick = { onFeedback(true) },
            colors = colors,
        )
        FeedbackChip(
            label = "Off target",
            selected = feedback == -1,
            onClick = { onFeedback(false) },
            colors = colors,
        )
    }
}

@Composable
private fun FeedbackChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
) {
    val background = if (selected) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
