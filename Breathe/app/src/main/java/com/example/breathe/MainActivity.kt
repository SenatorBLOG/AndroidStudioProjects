package com.example.breathe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.breathe.ui.navigation.MainScreen
import com.example.breathe.ui.screens.MeditationRegularityScreen
import com.example.breathe.ui.screens.ProfileScreen
import com.example.breathe.ui.screens.StatsScreen
import com.example.breathe.ui.theme.AppColors
import com.example.breathe.ui.theme.BreatheTheme
import com.example.breathe.ui.theme.ForestThemeColors
import com.example.breathe.ui.theme.OceanThemeColors
import com.example.breathe.ui.theme.SunsetThemeColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
                            Modifier.fillMaxSize()
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .height(70.dp)
            .background(colors.primary),
        containerColor = colors.onPrimary,
        contentColor = colors.title
    ) {
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.home_icon), contentDescription = "Home", tint = colors.title) },
            label = { Text("Home", color = colors.title) },
            selected = currentRoute == "main",
            onClick = { navController.navigate("main") { popUpTo("main") { inclusive = true } } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.onPrimary,
                unselectedIconColor = colors.title,
                selectedTextColor = colors.onPrimary,
                unselectedTextColor = colors.title,
                indicatorColor = colors.primary.copy(alpha = 0.3f)
            )
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.stats_icon), contentDescription = "Stats", tint = colors.title) },
            label = { Text("Stats", color = colors.title) },
            selected = currentRoute == "stats",
            onClick = { navController.navigate("stats") { popUpTo("stats") { inclusive = true } } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.onPrimary,
                unselectedIconColor = colors.title,
                selectedTextColor = colors.onPrimary,
                unselectedTextColor = colors.title,
                indicatorColor = colors.primary.copy(alpha = 0.3f)
            )
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.profile_icon), contentDescription = "Profile", tint = colors.title) },
            label = { Text("Profile", color = colors.title) },
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") { popUpTo("profile") { inclusive = true } } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.onPrimary,
                unselectedIconColor = colors.title,
                selectedTextColor = colors.onPrimary,
                unselectedTextColor = colors.title,
                indicatorColor = colors.primary.copy(alpha = 0.3f)
            )
        )
    }
}
