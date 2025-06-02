package com.example.breathe

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.breathe.ui.navigation.MainScreen
import com.example.breathe.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.breathe.BreathingPattern
import com.example.breathe.Phase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore extension property
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Считываем сохраненную тему
            val themeKey = stringPreferencesKey("theme")
            val themeFlow: Flow<String> = dataStore.data.map { preferences ->
                preferences[themeKey] ?: "Ocean"
            }
            val currentTheme by themeFlow.collectAsState(initial = "Ocean")

            // Определяем цвета на основе выбранной темы
            val colors = when (currentTheme) {
                "Forest" -> ForestThemeColors
                "Sunset" -> SunsetThemeColors
                else -> OceanThemeColors // По умолчанию "Ocean"
            }
            BreatheTheme(colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.background // Используем цвет фона из темы
                ) {
                    MainScreen(colors) { newTheme ->
                        // Сохраняем выбранную тему
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStore.edit { settings ->
                                settings[themeKey] = newTheme
                            }
                        }
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    BreatheTheme(OceanThemeColors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = OceanThemeColors.background
        ) {
            MainScreen(OceanThemeColors) {}
        }
    }
}