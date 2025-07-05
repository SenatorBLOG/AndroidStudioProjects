package com.example.breathe.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.viewmodel.StatsViewModel

@Composable
fun MeditationRegularityScreen(colors: AppColors, navController: NavController) {
    val viewModel: StatsViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Кнопка "Back" вверху
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primary
                )
            }
        }

        // Основной контент со статистикой
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Meditation Regularity",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge
            )

            // Детальная статистика
            Text(
                text = "Total Meditation Time: ${viewModel.formatMinutesToClock(state.totalMeditationMinutes)}",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Best Streak: ${state.bestStreakDays} days",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Sessions This Week: ${state.sessionsThisWeek}",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            // В будущем можно добавить графики или дополнительные метрики
        }
    }
}