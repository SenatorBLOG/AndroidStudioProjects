package com.breatheonline.breathe.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.StatsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StatsScreen(
    colors: AppColors,
    navController: NavController,
    initialTab: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) = com.breatheonline.breathe.ui.screens.stats.StatsScreen(
    colors = colors,
    navController = navController,
    initialTab = initialTab,
    modifier = modifier,
    viewModel = viewModel,
)
