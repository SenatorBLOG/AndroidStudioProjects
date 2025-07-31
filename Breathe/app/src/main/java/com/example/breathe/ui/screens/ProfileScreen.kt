package com.example.breathe.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import com.example.breathe.ui.theme.AppColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.breathe.viewmodel.StatsViewModel

// --- DataStore setup  ---
private const val USER_PREFERENCES_NAME = "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
val USER_BIO_KEY = stringPreferencesKey("user_bio")
val USER_DAILY_GOAL_MINUTES_KEY = stringPreferencesKey("user_daily_goal_minutes")
val USER_DAILY_GOAL_SESSIONS_KEY = stringPreferencesKey("user_daily_goal_sessions")
val USER_DAILY_INTENTION_KEY = stringPreferencesKey("user_daily_intention")

val NOTIFICATIONS_REMINDER_DAYS_KEY = stringPreferencesKey("notifications_reminder_days")
val NOTIFICATIONS_REMINDER_TIME_KEY = stringPreferencesKey("notifications_reminder_time")

val INTEGRATION_WEAR_OS_SYNC_KEY = booleanPreferencesKey("integration_wear_os_sync")
val INTEGRATION_WATCH_OS_SYNC_KEY = booleanPreferencesKey("integration_watch_os_sync")
val INTEGRATION_GOOGLE_FIT_KEY = booleanPreferencesKey("integration_google_fit")
val INTEGRATION_APPLE_HEALTH_KEY = booleanPreferencesKey("integration_apple_health")
val INTEGRATION_PPG_SENSOR_KEY = booleanPreferencesKey("integration_ppg_sensor")

val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
val APP_AUTOSTART_ON_WAKE_KEY = booleanPreferencesKey("app_autostart_on_wake")


@Composable
fun ProfileScreen(colors: AppColors, navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val statsViewModel: StatsViewModel = hiltViewModel()
    val statsState by statsViewModel.state.collectAsState()

    // --- Personal Information States ---
    var userName by remember { mutableStateOf("User") }
    var userBio by remember { mutableStateOf("I'm on a journey to mindfulness.") }
    var showNicknameInput by remember { mutableStateOf(false) }

    // --- Goals & Intentions States ---
    var dailyIntention by remember { mutableStateOf("Calm") }
    val intentionsList = listOf("Calm", "Focus", "Relaxation", "Energy", "Gratitude")
    var reminderDays by remember { mutableStateOf("Mon,Tue,Wed,Thu,Fri") }
    var reminderTime by remember { mutableStateOf("08:00") }
    var dailyGoalMinutes by remember { mutableStateOf("10") }
    var dailyGoalSessions by remember { mutableStateOf("1") }

    // --- Integrations & Devices States ---
    var wearOsSyncEnabled by remember { mutableStateOf(false) }
    var watchOsSyncEnabled by remember { mutableStateOf(false) }
    var googleFitEnabled by remember { mutableStateOf(false) }
    var appleHealthEnabled by remember { mutableStateOf(false) }
    var ppgSensorEnabled by remember { mutableStateOf(false) }

    // --- App Settings States ---
    var appLanguage by remember { mutableStateOf("English") }
    val languages = listOf("English", "Русский")
    var autostartOnWakeEnabled by remember { mutableStateOf(false) }

    // Load user data from DataStore
    LaunchedEffect(Unit) {
        val data = context.dataStore.data.first()
        userName = data[USER_NICKNAME_KEY] ?: "User"
        userBio = data[USER_BIO_KEY] ?: "I'm on a journey to mindfulness."
        dailyIntention = data[USER_DAILY_INTENTION_KEY] ?: "Calm"
        reminderDays = data[NOTIFICATIONS_REMINDER_DAYS_KEY] ?: "Mon,Tue,Wed,Thu,Fri"
        reminderTime = data[NOTIFICATIONS_REMINDER_TIME_KEY] ?: "08:00"
        dailyGoalMinutes = data[USER_DAILY_GOAL_MINUTES_KEY] ?: "10"
        dailyGoalSessions = data[USER_DAILY_GOAL_SESSIONS_KEY] ?: "1"

        wearOsSyncEnabled = data[INTEGRATION_WEAR_OS_SYNC_KEY] == true
        watchOsSyncEnabled = data[INTEGRATION_WATCH_OS_SYNC_KEY] == true
        googleFitEnabled = data[INTEGRATION_GOOGLE_FIT_KEY] == true
        appleHealthEnabled = data[INTEGRATION_APPLE_HEALTH_KEY] == true
        ppgSensorEnabled = data[INTEGRATION_PPG_SENSOR_KEY] == true

        appLanguage = data[APP_LANGUAGE_KEY] ?: "English"
        autostartOnWakeEnabled = data[APP_AUTOSTART_ON_WAKE_KEY] == true
    }

    // Calculate user level based on total meditation time from ViewModel
    val userLevel = when {
        statsState.totalMeditationMinutes < 100 -> "Beginner"
        statsState.totalMeditationMinutes < 500 -> "Intermediate"
        else -> "Zen Master"
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
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
        }
    ) { paddingValues ->
        // Make the content scrollable
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Personal Information Section ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Hello, $userName!",
                        color = colors.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Level: $userLevel",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userBio,
                        color = colors.text,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Nickname and Bio input field
                    if (showNicknameInput) {
                        var inputNickname by remember { mutableStateOf(userName) }
                        var inputBio by remember { mutableStateOf(userBio) }
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                            OutlinedTextField(
                                value = inputNickname,
                                onValueChange = { inputNickname = it },
                                label = { Text("Name") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary, unfocusedBorderColor = colors.text.copy(alpha = 0.5f),
                                    cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.text
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputBio,
                                onValueChange = { inputBio = it },
                                label = { Text("Short Bio / Status") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary, unfocusedBorderColor = colors.text.copy(alpha = 0.5f),
                                    cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.text
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        context.dataStore.edit { settings ->
                                            settings[USER_NICKNAME_KEY] = inputNickname
                                            settings[USER_BIO_KEY] = inputBio
                                        }
                                        userName = inputNickname
                                        userBio = inputBio
                                        showNicknameInput = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Save", color = colors.onPrimary)
                            }
                        }
                    }
                }
                IconButton(
                    onClick = { showNicknameInput = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = colors.primary
                    )
                }
            }

            // --- Meditation Statistics Section ---
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
                        text = "Meditation Statistics",
                        color = colors.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatItem(label = "Total Sessions", value = statsState.totalSessions.toString(), colors = colors)
                        StatItem(label = "Total Time", value = statsViewModel.formatMinutesToClock(statsState.totalMeditationMinutes), colors = colors)
                        StatItem(label = "Avg. Session", value = "${statsState.averageSessionDuration} min", colors = colors)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    StatItem(label = "Best Streak", value = "${statsState.bestStreakDays} days", colors = colors)
                }
            }

            // --- Dynamic Expandable Sections ---
            // Goals & Intentions
            ExpandableSection(title = "Goals & Intentions", colors = colors) {
                // Mood / Daily Intention
                Text(
                    text = "Mood / Daily Intention:",
                    color = colors.text,
                    style = MaterialTheme.typography.bodyMedium, // bodyMedium
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DropdownMenuBox(
                    label = "Select Intention",
                    selectedItem = dailyIntention,
                    items = intentionsList,
                    onItemSelected = { selected ->
                        dailyIntention = selected
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[USER_DAILY_INTENTION_KEY] = selected
                            }
                        }
                    },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Set Reminders
                Text(
                    text = "Reminders:",
                    color = colors.text,
                    style = MaterialTheme.typography.bodyMedium, // bodyMedium
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = reminderDays,
                    onValueChange = { newValue ->
                        reminderDays = newValue
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[NOTIFICATIONS_REMINDER_DAYS_KEY] = newValue
                            }
                        }
                    },
                    label = { Text("Days (e.g., Mon,Tue,Wed)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary, unfocusedBorderColor = colors.subtitle.copy(alpha = 0.5f),
                        cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.subtitle
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { newValue ->
                        reminderTime = newValue
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[NOTIFICATIONS_REMINDER_TIME_KEY] = newValue
                            }
                        }
                    },
                    label = { Text("Time (e.g., 08:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary, unfocusedBorderColor = colors.subtitle.copy(alpha = 0.5f),
                        cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.subtitle
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Daily Goal
                Text(
                    text = "Daily Goal:",
                    color = colors.text,
                    style = MaterialTheme.typography.bodyMedium, // bodyMedium
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dailyGoalMinutes,
                        onValueChange = { newValue -> if (newValue.all { it.isDigit() } || newValue.isEmpty()) dailyGoalMinutes = newValue },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary, unfocusedBorderColor = colors.subtitle.copy(alpha = 0.5f),
                            cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.subtitle
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("or", color = colors.text, style = MaterialTheme.typography.bodyMedium) // bodyMedium
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = dailyGoalSessions,
                        onValueChange = { newValue -> if (newValue.all { it.isDigit() } || newValue.isEmpty()) dailyGoalSessions = newValue },
                        label = { Text("Sessions/Cycles") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary, unfocusedBorderColor = colors.subtitle.copy(alpha = 0.5f),
                            cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.subtitle
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[USER_DAILY_GOAL_MINUTES_KEY] = dailyGoalMinutes
                                settings[USER_DAILY_GOAL_SESSIONS_KEY] = dailyGoalSessions
                            }
                            Toast.makeText(context, "Goal saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp), // Matched Start/Stop button shape
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Save Goals", color = colors.onPrimary, style = MaterialTheme.typography.labelLarge) // Matched button text style
                }
            }

            // Integrations & Devices
            ExpandableSection(title = "Integrations & Devices", colors = colors) {
                // Smartwatches
                IntegrationToggleItem(
                    label = "Wear OS Sync",
                    checked = wearOsSyncEnabled,
                    onCheckedChange = { isChecked ->
                        wearOsSyncEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[INTEGRATION_WEAR_OS_SYNC_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
                IntegrationToggleItem(
                    label = "WatchOS Sync",
                    checked = watchOsSyncEnabled,
                    onCheckedChange = { isChecked ->
                        watchOsSyncEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[INTEGRATION_WATCH_OS_SYNC_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Health Data Sharing
                IntegrationToggleItem(
                    label = "Google Fit (Heart Rate, SpO₂)",
                    checked = googleFitEnabled,
                    onCheckedChange = { isChecked ->
                        googleFitEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[INTEGRATION_GOOGLE_FIT_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
                IntegrationToggleItem(
                    label = "Apple Health (Heart Rate, SpO₂)",
                    checked = appleHealthEnabled,
                    onCheckedChange = { isChecked ->
                        appleHealthEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[INTEGRATION_APPLE_HEALTH_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Camera PPG Sensor
                IntegrationToggleItem(
                    label = "Measure Heart Rate via Flash",
                    checked = ppgSensorEnabled,
                    onCheckedChange = { isChecked ->
                        ppgSensorEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[INTEGRATION_PPG_SENSOR_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
            }

            // App Settings
            ExpandableSection(title = "App Settings", colors = colors) {
                // Interface Language
                Text(
                    text = "Interface Language:",
                    color = colors.text,
                    style = MaterialTheme.typography.bodyMedium, // bodyMedium
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DropdownMenuBox(
                    label = "Select Language",
                    selectedItem = appLanguage,
                    items = languages,
                    onItemSelected = { selected ->
                        appLanguage = selected
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[APP_LANGUAGE_KEY] = selected
                            }
                        }
                        Toast.makeText(context, "Language changed to $selected (restart required)", Toast.LENGTH_LONG).show()
                    },
                    colors = colors
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Auto-start Session
                IntegrationToggleItem(
                    label = "Auto-start session on wake (Smart Alarm)",
                    checked = autostartOnWakeEnabled,
                    onCheckedChange = { isChecked ->
                        autostartOnWakeEnabled = isChecked
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[APP_AUTOSTART_ON_WAKE_KEY] = isChecked
                            }
                        }
                    },
                    colors = colors
                )
            }

            // Account & Support
            ExpandableSection(title = "Account & Support", colors = colors) {
                Button(
                    onClick = { /* TODO: Navigate to Account Management Screen */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp), // Matched Start/Stop button shape
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface) // Matched button color
                ) {
                    Text("Manage Account", color = colors.title, style = MaterialTheme.typography.labelLarge) // Matched button text style
                }
                Button(
                    onClick = { /* TODO: Navigate to Subscription Screen */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                ) {
                    Text("Subscription / Pro Access", color = colors.title, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { /* TODO: Navigate to FAQ / Help */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                ) {
                    Text("FAQ / Help", color = colors.title, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { /* TODO: Open feedback form/email client */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface)
                ) {
                    Text("Send Feedback", color = colors.title, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Extra space at the bottom for scrolling
        }
    }
}


// --- HELPER COMPOSABLES

@Composable
fun StatItem(label: String, value: String, colors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = colors.primary, style = MaterialTheme.typography.headlineSmall)
        Text(text = label, color = colors.text, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSection(
    title: String,
    colors: AppColors,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = colors.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.primary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun IntegrationToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.text, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                uncheckedThumbColor = colors.text.copy(alpha = 0.5f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    label: String,
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    colors: AppColors
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {}, // Read-only
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary, unfocusedBorderColor = colors.text.copy(alpha = 0.5f),
                cursorColor = colors.primary, focusedLabelColor = colors.primary, unfocusedLabelColor = colors.text
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, style = MaterialTheme.typography.bodyMedium, color = colors.title) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}