package com.breatheonline.breathe.ui.screens.stats.meditation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.HealthSummary

// ── Health data panel ─────────────────────────────────────────────────────────

@Composable
internal fun HealthPanel(health: HealthSummary, colors: AppColors) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (health.avgSleep7dMin != null) {
                val h = health.avgSleep7dMin / 60; val m = health.avgSleep7dMin % 60
                HealthTile(Icons.Default.Hotel, "AVG SLEEP", if (m > 0) "${h}h ${m}m" else "${h}h", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
            if (health.avgHrv7d != null) {
                HealthTile(Icons.Default.TrendingUp, "AVG HRV", "${health.avgHrv7d}ms", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (health.restingHr != null) {
                HealthTile(Icons.Default.Favorite, "RESTING HR", "${health.restingHr} bpm", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
            if (health.recoveryScore != null) {
                HealthTile(Icons.Default.AutoAwesome, "RECOVERY", "${health.recoveryScore}/100", colors, Modifier.weight(1f))
            } else { Spacer(Modifier.weight(1f)) }
        }
        if (health.sources.isNotEmpty()) {
            Text(
                text  = "From: ${health.sources.joinToString(", ") { healthSourceLabel(it) }}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.subtitle.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}

@Composable
private fun HealthTile(
    icon:     ImageVector,
    label:    String,
    value:    String,
    colors: AppColors,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(14.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = colors.title, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.subtitle)
    }
}

internal fun healthSourceLabel(s: String) = when (s) {
    "apple_health" -> "Apple Health"
    "google_fit"   -> "Google Fit"
    "fitbit"       -> "Fitbit"
    else           -> s.replaceFirstChar { it.uppercase() }
}
