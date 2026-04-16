package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.components.AiCoachLauncher
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent

@Composable
internal fun SleepPromptChips(colors: AppColors, modifier: Modifier = Modifier) {
    val prompts = listOf(
        "Порекомендуй технику дыхания перед сном",
        "Почему я плохо сплю?",
        "Как углубить Deep sleep?",
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        prompts.forEach { prompt ->
            Text(
                text = prompt,
                color = SleepAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .clickable { AiCoachLauncher.request(prompt) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
