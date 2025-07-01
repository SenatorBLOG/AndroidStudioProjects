package com.example.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.breathe.ui.theme.*
import com.example.breathe.viewmodel.StatsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(colors: AppColors, navController: NavController) {
    val viewModel: StatsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val totals    by viewModel.totalsByDay.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "Statistics",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Meditation Progress",
                color = colors.subtitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 0.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(colors.surface.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Daily Meditation (min)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.subtitle
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(totals) { dayTotal ->
                        val displayDate = LocalDate
                            .parse(dayTotal.day)
                            .format(DateTimeFormatter.ofPattern("MMM d"))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(displayDate, color = colors.text)
                            Text("${dayTotal.totalDuration / 60} min", color = colors.value)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                StatCard(
                    title = "Total\nmeditation",
                    value = viewModel.formatMinutesToClock(state.totalMeditationMinutes),
                    onClick = { viewModel.saveSession(300, System.currentTimeMillis()) }, // 5 минут
                    colors = colors
                )
                StatCard(
                    title = "Best streak",
                    value = "${state.bestStreakDays} days",
                    onClick = { },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            StatCard(
                title = "Sessions\nthis week",
                value = "${state.sessionsThisWeek} sessions",
                onClick = { },
                colors = colors
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    onClick: () -> Unit,
    colors: AppColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(colors.surface, shape = RoundedCornerShape(14.dp))
            .width(120.dp)
            .height(110.dp)
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .width(120.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.uppercase(),
                color = colors.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = colors.value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}