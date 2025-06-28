package com.example.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breathe.ui.theme.*

@Composable
fun StatsScreen(colors: AppColors, navController: NavController) {
    val totalMeditatonTime = "00:00:00"
    val daysStreak = "0 days"
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
                    .background(colors.surface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Graph Placeholder", color = colors.text)
            }
            Spacer(modifier = Modifier.height(48.dp))

            //Коробки с данными статистики
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier. height(120.dp)
            ) {
                StatCard(
                    title = "Total\nmeditation",
                    value = totalMeditatonTime,
                    onClick = { /*TODO*/ },
                    colors = colors
                )
                StatCard(
                    title = "Best streak",
                    value = daysStreak,
                    onClick = { /*TODO*/ },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            StatCard(
                title = "Session\nthis week",
                value = "0 sessions",
                onClick = { /*TODO*/ },
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
    colors: AppColors) {
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
