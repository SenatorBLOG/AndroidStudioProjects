package com.breatheonline.breathe.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.components.ShimmerStatScreen
import com.breatheonline.breathe.ui.screens.Route
import com.breatheonline.breathe.ui.screens.stats.common.StatsTopTabs
import com.breatheonline.breathe.ui.screens.stats.meditation.MeditationStatsContent
import com.breatheonline.breathe.ui.screens.stats.sleep.SleepStatsContent
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.StatsViewModel

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun StatsScreen(
    colors: AppColors,
    navController: NavController,
    initialTab: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var period by remember { mutableStateOf(0) }  // 0=Week 1=Month 2=Year
    var topTab by remember { mutableStateOf(initialTab) }  // 0=Meditation 1=Sleep

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
            StatsTopTabs(selected = topTab, onSelect = { topTab = it }, colors = colors)
        }

        if (topTab == 0) {
            if (state.isLoading) {
                ShimmerStatScreen(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
            } else {
                MeditationStatsContent(
                    state         = state,
                    period        = period,
                    onPeriodSelect = { period = it },
                    colors        = colors,
                    navController = navController,
                )
            }
        } else {
            if (state.isLoading && state.sleepDayView == null) {
                ShimmerStatScreen(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
            } else {
                SleepStatsContent(
                    state = state,
                    onViewChange = { viewModel.setSleepView(it) },
                    onMoveSelection = { viewModel.moveSleepSelection(it) },
                    onJumpToLatest = { viewModel.jumpToLatestSleep() },
                    onInsightFeedback = { viewModel.setSleepInsightFeedback(it) },
                    onConnect = { navController.navigate(Route.PROFILE) },
                    colors = colors,
                )
            }
        }

        if (com.breatheonline.breathe.ui.components.AiCoachLauncher.open.value) {
            val prompt = com.breatheonline.breathe.ui.components.AiCoachLauncher.pendingPrompt.value
            com.breatheonline.breathe.ui.components.AiCoachBottomSheet(
                onDismiss = { com.breatheonline.breathe.ui.components.AiCoachLauncher.dismiss() },
                navController = navController,
                colors = colors,
                initialPrompt = prompt,
            )
        }
    }
}

// StatsTopTabs lives in common/StatsTopTabs.kt
