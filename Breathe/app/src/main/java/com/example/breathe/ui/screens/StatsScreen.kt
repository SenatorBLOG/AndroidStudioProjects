package com.example.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.breathe.ui.theme.*

@Composable
fun StatsScreen(colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Statistics",
            color = colors.title,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Posture Progression",
            color = colors.subtitle,
            style = MaterialTheme.typography.titleMedium
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(colors.surface.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Graph Placeholder", color = colors.text)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(label = "TOTAL MEDITATION TIME", value = "0 min", colors = colors)
            StatCard(label = "BEST STREAK", value = "0 days", colors = colors)
        }
        StatCard(label = "SESSIONS THIS WEEK", value = "0 sessions", colors = colors, fullWidth = true)
    }
}

@Composable
fun StatCard(label: String, value: String, colors: AppColors, fullWidth: Boolean = false) {
    val modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier.width(150.dp)
    Card(
        modifier = modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = colors.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                color = colors.value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}