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
                text = "Posture Progression",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "TOTAL MEDITATION TIME",
                    value = "0 min", colors = colors)
                StatCard(label = "BEST STREAK", value = "0 days", colors = colors)
            }
            StatCard(
                label = "SESSIONS THIS WEEK", value = "0 sessions", colors = colors, fullWidth = true
            )
        }
    }

}

@Composable
fun StatCard(label: String, value: String, colors: AppColors, fullWidth: Boolean = false) {
    val modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier.width(150.dp)
    Card(
        modifier = modifier.padding(8.dp)
            .height(100.dp),

        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(colors.surface, shape = RoundedCornerShape(14.dp))
                .width(100.dp)
                .height(120.dp)
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = label,
                    color = colors.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = value,
                    color = colors.value,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }


        }
    }
}
