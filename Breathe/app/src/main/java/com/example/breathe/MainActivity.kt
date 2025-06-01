package com.example.breathe

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.breathe.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import com.example.breathe.ui.components.BreathingCircle

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

data class BreathingPattern(
    val inhale: Long,
    val holdAfterInhale: Long,
    val exhale: Long,
    val holdAfterExhale: Long
)

enum class Phase {
    INHALE, HOLD1, EXHALE, HOLD2
}
@Composable
fun MainScreen(colors: AppColors, onThemeChange: (String) -> Unit) {
    var duration by remember { mutableStateOf("10 min") }
    var breathingPattern by remember { mutableStateOf("4-7-8") }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(0L) }
    val scale = remember { Animatable(1.0f) }
    var currentPhase by remember { mutableStateOf(Phase.INHALE) }

    val patterns = mapOf(
        "4-7-8" to BreathingPattern(4000, 7000, 8000, 0),
        "Box Breathing\n4-4-4-4" to BreathingPattern(4000, 4000, 4000, 4000)
    )

    val pattern = patterns[breathingPattern] ?: BreathingPattern(4000, 0, 4000, 0)

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                currentPhase = Phase.INHALE
                scale.animateTo(1.5f, animationSpec = tween(durationMillis = pattern.inhale.toInt(), easing = LinearEasing))
                if (!isRunning) break
                currentPhase = Phase.HOLD1
                delay(pattern.holdAfterInhale)
                if (!isRunning) break
                currentPhase = Phase.EXHALE
                scale.animateTo(1.0f, animationSpec = tween(durationMillis = pattern.exhale.toInt(), easing = LinearEasing))
                if (!isRunning) break
                if (pattern.holdAfterExhale > 0) {
                    currentPhase = Phase.HOLD2
                    delay(pattern.holdAfterExhale)
                    if (!isRunning) break
                }
            }
        } else {
            // Сброс состояния при остановке
            scale.snapTo(1.0f) // Возвращаем масштаб к исходному
            currentPhase = Phase.INHALE // Сбрасываем фазу
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            delay(remainingTime)
            isRunning = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
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
                text = "Breathe Better",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge,
            )

            Text(
                text = "Meditation for Sleep & Relaxation",
                color = colors.subtitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 0.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))

            //КРуг Миша
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .scale(scale.value)
                    .drawBehind {
                        // Внешнее свечение
                        val outerGlowRadius = size.width / 2 + 64.dp.toPx()
                        val outerGradient = Brush.radialGradient(
                            0f to colors.glowOuter,

                            1f to Color.Transparent,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = outerGlowRadius
                        )
                        drawCircle(brush = outerGradient, radius = outerGlowRadius)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Круг с фоновым цветом
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(color = colors.background, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Внутреннее свечение от краёв внутрь
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = colors.glowBackground, shape = CircleShape)
                            .drawBehind {
                                val innerGradient = Brush.radialGradient(
                                    colorStops = arrayOf(

                                        0f to colors.glowInner.copy(alpha = 0.05f), // Центр слегка светится
                                        0.99f to colors.glowInner.copy(alpha = 0.6f),
                                        1f to colors.glowInner.copy(alpha = 0.85f),// Плавно исчезает к границе
                                    ),                   // Полностью исчезает за краем
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width / 2
                                )
                                drawCircle(brush = innerGradient, radius = size.width / 2)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Текст поверх всего
                        Text(
                            text = when (currentPhase) {
                                Phase.INHALE -> "Inhale"
                                Phase.HOLD1 -> "Hold"
                                Phase.EXHALE -> "Exhale"
                                Phase.HOLD2 -> "Hold"
                            },
                            color = colors.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }


            /*BreathingCircle(
                scale = scale.value,
                phase = currentPhase,
                colors = colors
            )*/

            Spacer(modifier = Modifier.height(64.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .height(100.dp),
            ) {
                SettingItem(title = "Duration", value = duration, onClick = { showDurationDialog = true },colors = colors)
                SettingItem(title = "Breathing\nPattern", value = breathingPattern, onClick = { showPatternDialog = true },colors = colors)
                SettingItem(title = "Sound", value = "Voice", onClick = { /* TODO */ },colors = colors)
            }
            Spacer(modifier = Modifier.height(34.dp))
            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false // Останавливаем
                    } else {
                        isRunning = true // Запускаем заново
                        remainingTime = when (duration) {
                            "5 min" -> 5 * 60 * 1000L
                            "10 min" -> 10 * 60 * 1000L
                            "15 min" -> 15 * 60 * 1000L
                            "20 min" -> 20 * 60 * 1000L
                            else -> 10 * 60 * 1000L
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) colors.primary else colors.surface // Цвет меняется: серый для "Stop", цвет duration для "Start"
                )
            ) {
                Text(
                    text = if (isRunning) "Stop" else "Start",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.title,
                )
            }
        }
        // [Исправление] Шестерёнка теперь открывает диалог настроек, а не темы
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .statusBarsPadding() // [Исправление] Отступ для статус-бара
                .align(Alignment.TopEnd)
                .padding(end = 32.dp, top = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colors.title
            )
        }
        // [Добавлено] Диалог настроек
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Settings", style = MaterialTheme.typography.titleSmall) },
                text = {
                    Column {
                        Text(
                            text = "Themes",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSettingsDialog = false
                                    showThemeDialog = true
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Можно добавить другие настройки, например:
                        // Text(
                        //     text = "Sound",
                        //     modifier = Modifier
                        //         .fillMaxWidth()
                        //         .clickable { /* TODO */ }
                        //         .padding(16.dp),
                        //     style = MaterialTheme.typography.bodyMedium
                        // )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
        // [Исправление] Диалог темы открывается только из диалога настроек
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme", style = MaterialTheme.typography.titleSmall) },
                text = {
                    Column {
                        listOf("Ocean", "Forest", "Sunset").forEach { theme ->
                            Text(
                                text = theme,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onThemeChange(theme)
                                        showThemeDialog = false
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }

        if (showDurationDialog) {
            AlertDialog(
                onDismissRequest = { showDurationDialog = false },
                title = { Text("Choose Duration", style = MaterialTheme.typography.titleSmall) },
                text = {
                    Column {
                        listOf("5 min", "10 min", "15 min", "20 min").forEach { option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        duration = option
                                        showDurationDialog = false
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showDurationDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }

        if (showPatternDialog) {
            AlertDialog(
                onDismissRequest = { showPatternDialog = false },
                title = { Text("Choose Breathing Pattern",
                    style = MaterialTheme.typography.titleSmall) },
                text = {
                    Column {
                        listOf("4-7-8", "Box Breathing").forEach { option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        breathingPattern = option
                                        showPatternDialog = false
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPatternDialog = false }) {
                        Text("Cancel",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    }

}

@Composable
fun SettingItem(title: String, value: String, onClick: () -> Unit, colors: AppColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(colors.surface, shape = RoundedCornerShape(14.dp))
            .width(100.dp)
            .height(120.dp)
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(48.dp) // Фиксированная высота для заголовка
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Text(text = title.uppercase(), color = colors.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(0.dp))
        Box(
            modifier = Modifier
                .height(24.dp) // Фиксированная высота для значения
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Text(text = value, color = colors.value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

    }
}