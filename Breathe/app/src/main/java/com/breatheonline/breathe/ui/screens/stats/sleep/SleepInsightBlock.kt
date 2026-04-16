package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors

@Composable
internal fun SleepInsightBlock(
    paragraph: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(20.dp),
    ) {
        Text("Interpretation", color = colors.subtitle)
        Spacer(Modifier.height(8.dp))
        Text(paragraph, color = colors.title)
        Spacer(Modifier.height(16.dp))
        SleepPromptChips(colors)
    }
}
