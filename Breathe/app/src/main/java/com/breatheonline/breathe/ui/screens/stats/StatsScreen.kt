package com.breatheonline.breathe.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.ShimmerStatScreen
import com.breatheonline.breathe.ui.screens.stats.meditation.MeditationStatsContent
import com.breatheonline.breathe.ui.screens.stats.sleep.LegacySleepStatsContent
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.StatsViewModel

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun StatsScreen(
    colors: AppColors,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var period by remember { mutableStateOf(0) }  // 0=Week 1=Month 2=Year
    var topTab by remember { mutableStateOf(0) }  // 0=Meditation 1=Sleep

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text  = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.title,
            )
            Text(
                text     = stringResource(R.string.stats_subtitle),
                style    = MaterialTheme.typography.titleMedium,
                color    = colors.subtitle,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(16.dp))
            StatsModeTabs(selected = topTab, onSelect = { topTab = it }, colors = colors)
        }

        if (state.isLoading) {
            ShimmerStatScreen(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
            return@Column
        }

        if (topTab == 0) {
            MeditationStatsContent(
                state         = state,
                period        = period,
                onPeriodSelect = { period = it },
                colors        = colors,
                navController = navController,
            )
        } else {
            LegacySleepStatsContent(
                state = state,
                colors = colors,
                onSleepInsightFeedback = viewModel::setSleepInsightFeedback,
            )
        }
    }
}

// ── Mode tabs ──────────────────────────────────────────────────────────────────

@Composable
private fun StatsModeTabs(selected: Int, onSelect: (Int) -> Unit, colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("Meditation statistics", "Sleep statistics").forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) colors.onPrimary else colors.subtitle,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
