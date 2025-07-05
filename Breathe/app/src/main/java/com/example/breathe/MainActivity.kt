package com.example.breathe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.breathe.ui.screens.MeditationRegularityScreen
import com.example.breathe.ui.screens.ProfileScreen
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
                    bottomBar = { BottomNavigationBar(
                        navController,
                        colors
                    ) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            MainScreen(colors = colors, { newTheme ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    context.dataStore.edit { settings ->
                                        settings[themeKey] = newTheme
                                    }
                                }
                            }, Modifier.padding(innerPadding))
                        }
                        composable("stats") { StatsScreen(
                            colors,
                            navController,
                            Modifier.padding(innerPadding)
                        ) }
                        composable("profile") { ProfileScreen(
                            colors,
                            navController,
                            Modifier.padding(innerPadding)
                        ) }
                        composable("meditation_regularity") { MeditationRegularityScreen(
                            colors = colors, navController = navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, colors: AppColors) {
    NavigationBar(
        modifier = Modifier
            .height(70.dp) // Увеличиваем высоту
            .background(colors.primary), // Кастомный цвет фона
        containerColor = colors.primary, // Убедимся, что фон совпадает
        contentColor = colors.title // Цвет иконок и текста по умолчанию
    ) {
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.home_icon), contentDescription = "Home", tint = colors.title) },
            label = { Text("Home", color = colors.title) },
            selected = navController.currentDestination?.route == "main",
            onClick = { navController.navigate("main") { popUpTo("main") { saveState = true } } }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.stats_icon), contentDescription = "Stats", tint = colors.title) },
            label = { Text("Stats", color = colors.title) },
            selected = navController.currentDestination?.route == "stats",
            onClick = { navController.navigate("stats") { popUpTo("stats") { saveState = true } } }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.profile_icon), contentDescription = "Profile", tint = colors.title) },
            label = { Text("Profile", color = colors.title) },
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") { popUpTo("profile") { saveState = true } } }
        )
    }
}