package com.example.breathe.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.breathe.ui.navigation.MainScreen
import com.example.breathe.ui.screens.StatsScreen
import com.example.breathe.ui.theme.BreatheTheme
import com.example.breathe.ui.theme.OceanThemeColors

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppPreview() {
    val colors = OceanThemeColors
    val navController = rememberNavController()
    BreatheTheme(colors) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "stats",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("main") { MainScreen(colors = colors, onThemeChange = {}) }
                composable("stats") { StatsScreen(colors = colors, navController) }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = navController.currentDestination?.route == "main",
            onClick = { navController.navigate("main") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "Stats") },
            label = { Text("Stats") },
            selected = navController.currentDestination?.route == "stats",
            onClick = { navController.navigate("stats") }
        )
    }
}