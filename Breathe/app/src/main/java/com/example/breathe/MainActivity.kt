package com.example.breathe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.breathe.ui.navigation.MainScreen
import com.example.breathe.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.navigation.NavController
import com.example.breathe.ui.screens.StatsScreen
import dagger.hilt.android.AndroidEntryPoint

// DataStore для сохранения настроек
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeKey = stringPreferencesKey("theme")
            val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[themeKey] ?: "Ocean" // По умолчанию "Ocean"
            }
            val currentTheme by themeFlow.collectAsState(initial = "Ocean")

            // Выбор цветовой темы
            val colors = when (currentTheme) {
                "Forest" -> ForestThemeColors
                "Sunset" -> SunsetThemeColors
                else -> OceanThemeColors
            }

            BreatheTheme(colors) {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            MainScreen(colors = colors) { newTheme ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    context.dataStore.edit { settings ->
                                        settings[themeKey] = newTheme
                                    }
                                }
                            }
                        }
                        composable("stats") { StatsScreen(colors, navController) }
                    }
                }
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