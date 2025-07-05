package com.example.breathe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breathe.ui.theme.AppColors
import java.time.LocalDate

@Composable
fun ProfileScreen(colors: AppColors, navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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

        // Основной контент профиля
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge
            )

            // Пример данных профиля
            Text(
                text = "Joined: ${LocalDate.now().minusDays(30).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Total Sessions: 15", // Можно заменить на данные из ViewModel
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Last Active: ${LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}