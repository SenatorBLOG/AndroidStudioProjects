package com.breatheonline.breathe.ui.screens.stats.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent

@Composable
internal fun SleepHeroCard(
    title: String,
    subtitle: String,
    qualityLabel: String,
    deltaLine: String?,
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
        Text(text = subtitle, color = colors.subtitle)
        Spacer(Modifier.height(6.dp))
        Text(text = title, color = colors.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = qualityLabel, color = SleepAccent, fontWeight = FontWeight.SemiBold)
            if (deltaLine != null) {
                Spacer(Modifier.height(6.dp))
                Text(text = "  · $deltaLine", color = colors.subtitle)
            }
        }
    }
}
