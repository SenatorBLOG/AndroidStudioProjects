package com.breatheonline.breathe.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.ui.theme.AppColors
import java.util.Locale

@Composable
internal fun ProfileFormTab(
    colors: AppColors,
    displayName:          String,
    nickname:             String,
    onNicknameChange:     (String) -> Unit,
    heightCm:             String,
    onHeightChange:       (String) -> Unit,
    weightKg:             String,
    onWeightChange:       (String) -> Unit,
    age:                  String,
    onAgeChange:          (String) -> Unit,
    gender:               String,
    onGenderChange:       (String) -> Unit,
    goal:                 String,
    onGoalChange:         (String) -> Unit,
    bmi:                  Float?,
    bmiLabel:             String?,
    currentTheme:         String,
    onThemeChange:        (String) -> Unit,
    notificationsEnabled:    Boolean,
    reminderHour:            Int,
    reminderMinute:          Int,
    reminderIsExact:         Boolean,
    onNotificationsToggle:   (Boolean) -> Unit,
    onTimePickerOpen:        () -> Unit,
    dataCollectionEnabled:   Boolean,
    onDataCollectionToggle:  (Boolean) -> Unit,
    onSave:                  () -> Unit,
    saving:                  Boolean,
) {
    var personalExpanded by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {

        // ── Collapsible: Display Name + Body Data + Gender ────────────────────
        CollapsiblePersonalInfo(
            expanded         = personalExpanded,
            onToggle         = { personalExpanded = !personalExpanded },
            colors           = colors,
            displayName      = displayName,
            nickname         = nickname,
            onNicknameChange = onNicknameChange,
            heightCm         = heightCm,
            onHeightChange   = onHeightChange,
            weightKg         = weightKg,
            onWeightChange   = onWeightChange,
            age              = age,
            onAgeChange      = onAgeChange,
            gender           = gender,
            onGenderChange   = onGenderChange,
        )

        // ── BMI — always visible outside the collapsible ──────────────────────
        if (bmi != null) {
            ProfileChip(
                text   = "BMI ${"%.1f".format(bmi)} ${bmiLabel.orEmpty()}",
                colors = colors,
                active = true,
            )
        }

        // ── My Goal ───────────────────────────────────────────────────────────
        SectionCard(stringResource(R.string.form_my_goal), colors) {
            FlowPills(items = GOALS, selected = goal, colors = colors, onSelect = onGoalChange)
        }

        // ── Theme ─────────────────────────────────────────────────────────────
        SectionCard(stringResource(R.string.form_theme), colors) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Ocean" to R.string.form_theme_ocean,
                    "Forest" to R.string.form_theme_forest,
                    "Sunset" to R.string.form_theme_sunset,
                    "Day" to R.string.form_theme_day,
                ).forEach { (themeValue, labelRes) ->
                    ProfileChip(
                        text    = stringResource(labelRes),
                        colors  = colors,
                        active  = currentTheme == themeValue,
                        onClick = { onThemeChange(themeValue) },
                    )
                }
            }
        }

        // ── Language ──────────────────────────────────────────────────────────
        SectionCard(stringResource(R.string.profile_language), colors) {
            var selectedLang by remember {
                val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                mutableStateOf(when {
                    tags.contains("ru") -> "ru"
                    tags.contains("es") -> "es"
                    else -> "en"
                })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "en" to R.string.language_english,
                    "ru" to R.string.language_russian,
                    "es" to R.string.language_spanish,
                ).forEach { (code, labelRes) ->
                    val label = when (code) {
                        "en" -> "🇬🇧 ${stringResource(labelRes)}"
                        "ru" -> "🇷🇺 ${stringResource(labelRes)}"
                        else -> "🇪🇸 ${stringResource(labelRes)}"
                    }
                    ProfileChip(
                        text    = label,
                        colors  = colors,
                        active  = selectedLang == code,
                        onClick = {
                            selectedLang = code
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                        },
                    )
                }
            }
        }

        // ── Notifications ─────────────────────────────────────────────────────
        SectionCard(stringResource(R.string.form_notifications), colors) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.form_daily_reminder),      color = colors.title,                        style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.form_daily_reminder_hint), color = colors.subtitle.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                }
                Switch(
                    checked         = notificationsEnabled,
                    onCheckedChange = onNotificationsToggle,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor    = colors.onPrimary,
                        checkedTrackColor    = colors.primary,
                        uncheckedThumbColor  = colors.subtitle,
                        uncheckedTrackColor  = colors.surface,
                        uncheckedBorderColor = colors.subtitle.copy(alpha = 0.4f),
                    ),
                )
            }

            if (notificationsEnabled) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTimePickerOpen)
                        .background(colors.background.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                        .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(stringResource(R.string.form_reminder_time), color = colors.title, style = MaterialTheme.typography.bodyMedium)
                        if (!reminderIsExact) {
                            Text(stringResource(R.string.form_approximate_on_device), color = colors.subtitle.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text       = String.format(Locale.US, "%02d:%02d", reminderHour, reminderMinute),
                        color      = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ── Privacy & Data ────────────────────────────────────────────────────
        SectionCard(stringResource(R.string.form_privacy_data), colors) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.form_allow_data_collection),
                        color = colors.title,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        stringResource(R.string.form_share_stats_hint),
                        color = colors.subtitle.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Switch(
                    checked         = dataCollectionEnabled,
                    onCheckedChange = onDataCollectionToggle,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor    = colors.onPrimary,
                        checkedTrackColor    = colors.primary,
                        uncheckedThumbColor  = colors.subtitle,
                        uncheckedTrackColor  = colors.surface,
                        uncheckedBorderColor = colors.subtitle.copy(alpha = 0.4f),
                    ),
                )
            }
            if (!dataCollectionEnabled) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.form_data_collection_off),
                    color = colors.subtitle.copy(alpha = 0.70f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        // ── How we use this (bottom) ──────────────────────────────────────────
        SectionCard(stringResource(R.string.form_how_we_use), colors) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.primary.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Text(
                    text  = stringResource(R.string.form_data_calibration),
                    color = colors.text.copy(alpha = 0.80f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // ── Save button (bottom) ──────────────────────────────────────────────
        Button(
            onClick  = onSave,
            enabled  = !saving,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor   = colors.onPrimary,
            ),
        ) {
            if (saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.onPrimary, strokeWidth = 2.dp)
            else        Text(stringResource(R.string.form_save_changes), color = colors.onPrimary)
        }
    }
}

// ── Collapsible personal info (Display Name + Body Data + Gender) ─────────────

@Composable
private fun CollapsiblePersonalInfo(
    expanded:         Boolean,
    onToggle:         () -> Unit,
    colors: AppColors,
    displayName:      String,
    nickname:         String,
    onNicknameChange: (String) -> Unit,
    heightCm:         String,
    onHeightChange:   (String) -> Unit,
    weightKg:         String,
    onWeightChange:   (String) -> Unit,
    age:              String,
    onAgeChange:      (String) -> Unit,
    gender:           String,
    onGenderChange:   (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.subtitle.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
    ) {
        // Tappable header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text          = stringResource(R.string.form_personal_info),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 1.sp,
            )
            Icon(
                imageVector        = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint               = colors.subtitle,
                modifier           = Modifier.size(20.dp),
            )
        }

        // Expandable body
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier            = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Display name
                Text(stringResource(R.string.form_display_name), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 1.sp)
                BasicTextField(
                    value         = nickname,
                    onValueChange = onNicknameChange,
                    singleLine    = true,
                    cursorBrush   = SolidColor(colors.primary),
                    textStyle     = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                    modifier      = Modifier
                        .fillMaxWidth()
                        .background(colors.background.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                        .border(1.dp, colors.subtitle.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    decorationBox = { inner ->
                        Box {
                            if (nickname.isEmpty()) Text(displayName, color = colors.subtitle.copy(alpha = 0.45f), style = MaterialTheme.typography.bodyMedium)
                            inner()
                        }
                    },
                )
                Text(stringResource(R.string.form_shown_in_community), color = colors.subtitle.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)

                // Body data
                Text(stringResource(R.string.form_body_data), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 1.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    MiniInput(stringResource(R.string.form_height_label), stringResource(R.string.form_height_unit),  heightCm, colors, onHeightChange, Modifier.weight(1f))
                    MiniInput(stringResource(R.string.form_weight_label), stringResource(R.string.form_weight_unit),  weightKg, colors, onWeightChange, Modifier.weight(1f))
                }
                MiniInput(stringResource(R.string.form_age_label), stringResource(R.string.form_age_unit), age, colors, onAgeChange, Modifier.fillMaxWidth())

                // Gender
                Text(stringResource(R.string.form_gender), style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 1.sp)
                FlowPills(items = GENDERS, selected = gender, colors = colors, onSelect = onGenderChange)
            }
        }
    }
}

// ── Local-only components ─────────────────────────────────────────────────────

@Composable
private fun FlowPills(
    items:    List<ChoiceItem>,
    selected: String,
    colors: AppColors,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            ProfileChip(
                text    = stringResource(item.labelRes),
                colors  = colors,
                active  = selected == item.value,
                onClick = { onSelect(item.value) },
            )
        }
    }
}

@Composable
private fun MiniInput(
    label:         String,
    unit:          String,
    value:         String,
    colors: AppColors,
    onValueChange: (String) -> Unit,
    modifier:      Modifier,
) {
    Column(modifier) {
        Text(label, color = colors.subtitle, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                .border(1.dp, colors.subtitle.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicTextField(
                value           = value,
                onValueChange   = onValueChange,
                modifier        = Modifier.weight(1f),
                singleLine      = true,
                cursorBrush     = SolidColor(colors.primary),
                textStyle       = MaterialTheme.typography.bodyMedium.copy(color = colors.title),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox   = { inner ->
                    Box {
                        if (value.isEmpty()) Text("—", color = colors.subtitle.copy(alpha = 0.4f), style = MaterialTheme.typography.bodyMedium)
                        inner()
                    }
                },
            )
            Text(unit, color = colors.subtitle.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
