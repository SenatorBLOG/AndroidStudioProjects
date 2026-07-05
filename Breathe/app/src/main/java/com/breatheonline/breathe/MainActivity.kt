package com.breatheonline.breathe

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import com.breatheonline.breathe.ui.screens.AppNavGraph
import com.breatheonline.breathe.ui.screens.Route
import com.breatheonline.breathe.ui.theme.BreatheTheme
import com.breatheonline.breathe.ui.theme.DayThemeColors
import com.breatheonline.breathe.ui.theme.ForestThemeColors
import com.breatheonline.breathe.ui.theme.OceanThemeColors
import com.breatheonline.breathe.ui.theme.SunsetThemeColors
import com.breatheonline.breathe.utils.AuthEventBus
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
                "Day"    -> DayThemeColors
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
                        lifecycleScope.launch(Dispatchers.IO) {
                            context.dataStore.edit { it[themeKey] = newTheme }
                        }
                    },
                )
            }
        }
    }
}
