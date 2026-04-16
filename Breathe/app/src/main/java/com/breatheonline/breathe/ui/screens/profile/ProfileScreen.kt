package com.breatheonline.breathe.ui.screens.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.breatheonline.breathe.ui.screens.Route
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.ui.theme.ForestThemeColors
import com.breatheonline.breathe.ui.theme.SunsetThemeColors
import com.breatheonline.breathe.viewmodel.ProfileViewModel

private const val TAB_PROFILE    = "profile"
private const val TAB_SESSIONS   = "sessions"
private const val TAB_PROGRESS   = "progress"
private const val TAB_CHALLENGES = "challenges"
private const val TAB_DEVICES    = "devices"

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    colors: AppColors,
    navController: NavController,
    onThemeChange: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state   by viewModel.state.collectAsState()
    val context =  LocalContext.current

    var tab           by remember { mutableStateOf(TAB_PROFILE) }
    var nickname      by remember { mutableStateOf("") }
    var avatarSrc     by remember { mutableStateOf<String?>(null) }
    var heightCm      by remember { mutableStateOf("") }
    var weightKg      by remember { mutableStateOf("") }
    var age           by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("") }
    var goal          by remember { mutableStateOf("") }
    var initialized   by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val currentTheme = when (colors.primary) {
        ForestThemeColors.primary -> "Forest"
        SunsetThemeColors.primary -> "Sunset"
        else                      -> "Ocean"
    }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) avatarSrc = uriToBase64(context, uri)
    }

    LaunchedEffect(state.userName, state.userEmail, state.avatar, state.height, state.weight, state.age, state.gender, state.goal) {
        if (!initialized && (state.userEmail.isNotBlank() || state.userName.isNotBlank())) {
            nickname  = state.userName
            avatarSrc = state.avatar
            heightCm  = state.height?.toString().orEmpty()
            weightKg  = state.weight?.toString().orEmpty()
            age       = state.age?.toString().orEmpty()
            gender    = state.gender.orEmpty()
            goal      = state.goal.orEmpty()
            initialized = true
        }
    }

    val displayName = nickname.ifBlank {
        state.userName.ifBlank { state.userEmail.substringBefore("@").ifBlank { "Your Profile" } }
    }
    val bmi = computeBmi(heightCm, weightKg)
    val bmiLabel = bmi?.let {
        when {
            it < 18.5f -> "Underweight"
            it < 25f   -> "Normal"
            it < 30f   -> "Overweight"
            else       -> "Obese"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Profile",                                                                                                style = MaterialTheme.typography.headlineLarge, color = colors.title, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Everything in one place: profile, sessions, progress, challenges and devices.", style = MaterialTheme.typography.bodySmall,    color = colors.subtitle.copy(alpha = 0.85f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        Spacer(Modifier.height(18.dp))

        HeaderCard(
            colors       = colors,
            displayName  = displayName,
            email        = state.userEmail,
            avatarSrc    = avatarSrc,
            initialsText = initials(displayName),
            heightCm     = heightCm,
            weightKg     = weightKg,
            age          = age,
            goal         = goal,
            onAvatarPick = { avatarPicker.launch("image/*") },
        )

        Spacer(Modifier.height(16.dp))
        QuickNavSection(
            colors            = colors,
            navController     = navController,
            onChallengesClick = { tab = TAB_CHALLENGES },
        )
        Spacer(Modifier.height(16.dp))
        TabBar(activeTab = tab, colors = colors, onSelect = { tab = it })
        Spacer(Modifier.height(16.dp))

        when (tab) {
            TAB_PROFILE    -> ProfileFormTab(
                colors = colors, displayName = displayName,
                nickname = nickname, onNicknameChange = { nickname = it.take(30) },
                heightCm = heightCm, onHeightChange = { heightCm = it.filter(Char::isDigit).take(3) },
                weightKg = weightKg, onWeightChange = { weightKg = it.filter(Char::isDigit).take(3) },
                age = age, onAgeChange = { age = it.filter(Char::isDigit).take(3) },
                gender = gender, onGenderChange = { gender = it },
                goal = goal, onGoalChange = { goal = it },
                bmi = bmi, bmiLabel = bmiLabel,
                currentTheme = currentTheme, onThemeChange = onThemeChange,
                notificationsEnabled = state.notificationsEnabled,
                reminderHour = state.reminderHour, reminderMinute = state.reminderMinute,
                reminderIsExact = state.reminderIsExact,
                onNotificationsToggle = { viewModel.setReminderEnabled(it) },
                onTimePickerOpen = { showTimePicker = true },
                dataCollectionEnabled  = state.dataCollectionEnabled,
                onDataCollectionToggle = { viewModel.setDataCollectionEnabled(it) },
                onSave = {
                    viewModel.saveProfile(
                        nickname = nickname, avatar = avatarSrc,
                        height = heightCm.toIntOrNull(), weight = weightKg.toIntOrNull(),
                        age = age, gender = gender, goal = goal,
                    )
                },
                saving = state.isSavingProfile,
            )
            TAB_SESSIONS   -> SessionsTab(colors = colors, state = state)
            TAB_PROGRESS   -> ProgressTab(colors = colors, state = state)
            TAB_CHALLENGES -> ChallengesTab(colors = colors)
            TAB_DEVICES    -> DevicesTab(colors = colors, state = state, onSync = { viewModel.syncIntegrations() })
        }

        Spacer(Modifier.height(28.dp))

        // ── Sign out ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x12FF5555))
                .border(1.dp, Color(0x1FFF5555), RoundedCornerShape(16.dp))
                .clickable {
                    viewModel.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Sign out", color = Color(0xFFFF7070), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            hour      = state.reminderHour,
            minute    = state.reminderMinute,
            onConfirm = { h, m -> viewModel.setReminderTime(h, m); showTimePicker = false },
            onDismiss = { showTimePicker = false },
        )
    }
}

// ── Header card ───────────────────────────────────────────────────────────────

@Composable
private fun HeaderCard(
    colors: AppColors,
    displayName:  String,
    email:        String,
    avatarSrc:    String?,
    initialsText: String,
    heightCm:     String,
    weightKg:     String,
    age:          String,
    goal:         String,
    onAvatarPick: () -> Unit,
) {
    val goalLabel = GOALS.firstOrNull { it.first == goal }?.second

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(24.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp).clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.12f))
                .clickable(onClick = onAvatarPick)
                .drawBehind {
                    val glow = size.width / 2 + 18.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to colors.glowInner.copy(alpha = 0.22f),
                            1f to Color.Transparent,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = glow,
                        ),
                        radius = glow,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                !avatarSrc.isNullOrBlank() && avatarSrc.startsWith("data:image") -> {
                    val bmp = base64ToBitmap(avatarSrc)
                    if (bmp != null) Image(bmp.asImageBitmap(), "Avatar", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else             Text(initialsText, color = colors.primary, fontWeight = FontWeight.Bold)
                }
                !avatarSrc.isNullOrBlank() -> AsyncImage(avatarSrc, "Avatar", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else -> Text(initialsText, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.size(16.dp))

        Column(Modifier.weight(1f)) {
            Text(displayName, style = MaterialTheme.typography.headlineSmall, color = colors.title, fontWeight = FontWeight.SemiBold)
            Text(email,       style = MaterialTheme.typography.bodySmall,     color = colors.subtitle.copy(alpha = 0.9f))
            if (goalLabel != null) {
                Spacer(Modifier.height(8.dp))
                ProfileChip(text = "✦ $goalLabel", colors = colors, active = true)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            if (heightCm.isNotBlank()) Text("$heightCm cm", color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
            if (weightKg.isNotBlank()) Text("$weightKg kg", color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
            if (age.isNotBlank())      Text("$age y",       color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ── Tab bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TabBar(activeTab: String, colors: AppColors, onSelect: (String) -> Unit) {
    val tabs = listOf(
        TAB_PROFILE to "Profile", TAB_SESSIONS to "Sessions",
        TAB_PROGRESS to "Progress", TAB_CHALLENGES to "Challenges", TAB_DEVICES to "Devices",
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp).fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { (id, label) ->
            TabButton(active = activeTab == id, text = label, colors = colors, onClick = { onSelect(id) })
        }
    }
}

@Composable
private fun TabButton(active: Boolean, text: String, colors: AppColors, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text       = text,
            color      = if (active) colors.onPrimary else colors.subtitle,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            fontSize   = 11.sp,
        )
    }
}

// ── Reminder time picker ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    hour:      Int,
    minute:    Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton    = { TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) { Text("OK") } },
        dismissButton    = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title            = { Text("Reminder Time") },
        text             = { TimePicker(state = pickerState) },
    )
}

// ── Quick-nav section ─────────────────────────────────────────────────────────

@Composable
private fun QuickNavSection(
    colors: AppColors,
    navController:     NavController,
    onChallengesClick: () -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text          = "QUICK ACCESS",
            style         = MaterialTheme.typography.labelSmall,
            color         = colors.subtitle,
            letterSpacing = 1.sp,
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NavCard(
                emoji    = "📊",
                title    = "Statistics",
                subtitle = "Meditation data",
                colors   = colors,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate(Route.HISTORY) },
            )
            NavCard(
                emoji    = "📝",
                title    = "Journal",
                subtitle = "Reflections & mood",
                colors   = colors,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate(Route.JOURNAL) },
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NavCard(
                emoji    = "❓",
                title    = "Help & FAQ",
                subtitle = "Guides & answers",
                colors   = colors,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate(Route.FAQ) },
            )
            NavCard(
                emoji    = "🔒",
                title    = "Privacy",
                subtitle = "Policy & your data",
                colors   = colors,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate(Route.PRIVACY_POLICY) },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.18f),
                            colors.primary.copy(alpha = 0.08f),
                        )
                    )
                )
                .border(1.dp, colors.primary.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                .clickable(onClick = onChallengesClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text("🏆  Challenges", color = colors.title,    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    Text("Complete daily goals",  color = colors.subtitle.copy(alpha = 0.80f), style = MaterialTheme.typography.labelSmall)
                }
                Text("→", color = colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NavCard(
    emoji:    String,
    title:    String,
    subtitle: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit,
) {
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(emoji, fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))
        Text(title,    color = colors.title,                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, color = colors.subtitle.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
    }
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

private fun initials(name: String): String =
    name.trim().split("\\s+".toRegex()).take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

private fun computeBmi(h: String, w: String): Float? {
    val height = h.toFloatOrNull() ?: return null
    val weight = w.toFloatOrNull() ?: return null
    return if (height > 0) weight / ((height / 100) * (height / 100)) else null
}

private fun uriToBase64(c: Context, u: Uri): String? = runCatching {
    val bytes = c.contentResolver.openInputStream(u)?.use { it.readBytes() } ?: return null
    "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
}.getOrNull()

private fun base64ToBitmap(b: String) = runCatching {
    val bytes = Base64.decode(b.substringAfter("base64,"), Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()
