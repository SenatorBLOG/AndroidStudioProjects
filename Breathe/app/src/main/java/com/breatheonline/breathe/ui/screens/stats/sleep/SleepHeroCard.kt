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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.SleepAccent

@Composable
internal fun SleepHeroCard(
    title: String,        // e.g. "5 hrs 54 mins"
    subtitle: String,     // e.g. "2026-04-12"
    qualityLabel: String, // e.g. "Excellent"
    deltaLine: String?,   // e.g. "↓ 41 m vs 7-day avg"
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            text = subtitle,
            color = colors.subtitle,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            color = colors.title,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 44.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = qualityLabel,
            color = SleepAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (deltaLine != null) {
            Spacer(Modifier.height(10.dp))
            SleepDeltaBanner(text = deltaLine, colors = colors)
        }
    }
}
