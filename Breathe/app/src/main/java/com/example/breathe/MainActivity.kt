package com.example.breathe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.example.breathe.ui.screens.AppNavGraph
import com.example.breathe.ui.screens.Route
import com.example.breathe.ui.theme.BreatheTheme
import com.example.breathe.ui.theme.ForestThemeColors
import com.example.breathe.ui.theme.OceanThemeColors
import com.example.breathe.ui.theme.SunsetThemeColors
import com.example.breathe.utils.AuthEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val context       = LocalContext.current
            val themeKey      = stringPreferencesKey("theme")
            val navController = rememberNavController()

            val currentTheme by context.dataStore.data
                .map { prefs -> prefs[themeKey] ?: "Ocean" }
                .collectAsState(initial = "Ocean")

            val colors = when (currentTheme) {
                "Forest" -> ForestThemeColors
                "Sunset" -> SunsetThemeColors
                else     -> OceanThemeColors
            }

            BreatheTheme(colors) {
                // Redirect to login whenever the API returns 401
                LaunchedEffect(Unit) {
                    AuthEventBus.events.collect {
                        navController.navigate(Route.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                AppNavGraph(
                    navController = navController,
                    colors        = colors,
                    onThemeChange = { newTheme ->
                        CoroutineScope(Dispatchers.IO).launch {
                            context.dataStore.edit { it[themeKey] = newTheme }
                        }
                    },
                )
            }
        }
    }
}
