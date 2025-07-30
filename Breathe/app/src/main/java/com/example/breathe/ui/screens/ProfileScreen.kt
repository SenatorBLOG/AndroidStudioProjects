package com.example.breathe.ui.screens

import android.content.Context // <-- Убедись, что есть этот импорт
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.breathe.ui.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore // <-- Добавь это, если ещё нет
import androidx.datastore.preferences.core.Preferences // <-- Добавь это
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch // <-- Добавь этот импорт для scope.launch

// --- DataStore Singleton Setup ---
private const val USER_PREFERENCES_NAME = "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

// Ключ для хранения никнейма
val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")

// Вспомогательная функция для получения DataStore
// Это гарантирует, что DataStore создается и используется только один раз
// @OptIn(androidx.datastore.core.ExperimentalMultiProcessDataStore::class) // Можешь удалить, если не используешь multiprocess
fun Context.getUserDataStore(): DataStore<Preferences> = this.dataStore
// --- End DataStore Singleton Setup ---


@Composable
fun ProfileScreen(colors: AppColors, navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("User") }
    var userNickname by remember { mutableStateOf("") }
    var showNicknameInput by remember { mutableStateOf(false) }

    // Загружаем никнейм при первом входе на экран
    LaunchedEffect(Unit) {
        // Используем getUserDataStore() для доступа к синглтону
        val storedNickname = context.getUserDataStore().data.first()[USER_NICKNAME_KEY]
        if (storedNickname != null && storedNickname.isNotBlank()) {
            userName = storedNickname
            userNickname = "@$storedNickname"
        } else {
            showNicknameInput = true
        }
    }

    val joinedDate = LocalDate.now().minusDays(30)
    val totalSessions = 15
    val totalMeditationTime = 225 // in minutes
    val averageSessionDuration = 15 // in minutes
    val currentStreak = 5 // consecutive days
    val achievements = listOf("10 sessions in a row", "Meditated for 30 minutes")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Кнопка "Назад" и заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Profile",
                color = colors.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Блок с именем пользователя и кнопкой редактирования
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    color = colors.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userNickname,
                    color = colors.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { showNicknameInput = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = colors.primary
                )
            }
        }

        // Поле ввода никнейма (показываем, если showNicknameInput true)
        if (showNicknameInput) {
            var inputNickname by remember { mutableStateOf(userName) }

            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                OutlinedTextField(
                    value = inputNickname,
                    onValueChange = { inputNickname = it },
                    label = { Text("Enter your nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.text.copy(alpha = 0.5f),
                        cursorColor = colors.primary,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            // Используем getUserDataStore() для доступа к синглтону
                            context.getUserDataStore().edit { settings ->
                                settings[USER_NICKNAME_KEY] = inputNickname
                            }
                            userName = inputNickname
                            userNickname = "@$inputNickname"
                            showNicknameInput = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Save Nickname", color = colors.onPrimary)
                }
            }
        }


        // Статистическая карточка (без изменений)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Meditation Stats",
                    color = colors.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatItem(label = "Total Sessions", value = totalSessions.toString(), colors = colors)
                    StatItem(label = "Total Time", value = "$totalMeditationTime min", colors = colors)
                    StatItem(label = "Avg. Session", value = "$averageSessionDuration min", colors = colors)
                }
            }
        }

        // Карточка прогресса (без изменений)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Progress",
                    color = colors.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatItem(label = "Current Streak", value = "$currentStreak days", colors = colors)
                    AchievementBadge(text = achievements[0], colors = colors)
                }
            }
        }

        // Карточка графика активности (упрощенный пример) (без изменений)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Activity Graph",
                    color = colors.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Graph placeholder",
                        color = colors.text,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Кнопки действий (без изменений)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Переход к редактированию профиля (сейчас это уже на экране) */ },
                colors = ButtonDefaults.buttonColors(containerColor =colors.primary)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
            Button(
                onClick = { /* Переход к настройкам */ },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settings")
            }
        }
    }
}

// Auxiliary components (StatItem, AchievementBadge остаются без изменений)
@Composable
fun StatItem(label: String, value: String, colors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = colors.value, style = MaterialTheme.typography.headlineSmall)
        Text(text = label, color = colors.text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AchievementBadge(text: String, colors: AppColors) {
    Box(
        modifier = Modifier
            .background(colors.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = colors.primary, style = MaterialTheme.typography.bodySmall)
    }
}