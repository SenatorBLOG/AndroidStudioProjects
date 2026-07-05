package com.breatheonline.breathe.ui.screens.profile

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.IntegrationStatusDto
import com.breatheonline.breathe.ui.icons.LucideAppIcons
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import com.breatheonline.breathe.utils.parseHealthDate
import com.breatheonline.breathe.viewmodel.ProfileState
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Provider descriptor ───────────────────────────────────────────────────────

private data class Provider(
    val key:      String,
    val name:     String,
    val emoji:    String,
    val color:    Color,
    val subtitle: String,
)

@Composable
private fun oauthProviders(): List<Provider> = listOf(
    Provider("fitbit",     "Fitbit",     "", Color(0xFF00B0B9), stringResource(R.string.devices_fitbit_subtitle)),
    Provider("google-fit", "Google Fit", "", Color(0xFF4285F4), stringResource(R.string.devices_google_fit_subtitle)),
)

// ── Main composable ───────────────────────────────────────────────────────────

@Composable
internal fun DevicesTab(
    colors: AppColors,
    state: ProfileState,
    onSync: () -> Unit,
    onConnect: (String, Context) -> Unit,
    onDisconnect: (String) -> Unit,
    onSyncHealthConnect: () -> Unit,
    onOpenHcSettings: () -> Unit,
) {
    val context = LocalContext.current
    val haptic  = LocalHapticFeedback.current

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // ── OAuth providers ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text          = stringResource(R.string.devices_connected_services),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 2.sp,
            )

            oauthProviders().forEach { provider ->
                val integration = state.integrations.find {
                    it.provider == provider.key || it.provider == provider.key.replace("-", "_")
                }
                ProviderCard(
                    provider    = provider,
                    integration = integration,
                    colors      = colors,
                    isBusy      = state.isSyncing,
                    onConnect   = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConnect(provider.key, context)
                    },
                    onDisconnect = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDisconnect(provider.key.replace("-", "_"))
                    },
                )
            }
        }

        // ── Sync All button (only shown if at least one connected) ────────────
        if (state.integrations.any { it.connected }) {
            SpringButton(
                onClick  = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSync()
                },
                enabled  = !state.isSyncing,
                colors   = colors,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.devices_syncing), color = colors.onPrimary)
                } else {
                    Text(stringResource(R.string.devices_sync_all), color = colors.onPrimary)
                }
            }
        }

        HorizontalDivider(color = colors.subtitle.copy(alpha = 0.12f))

        // ── Health Connect ────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text          = stringResource(R.string.devices_health_connect_section),
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.subtitle,
                letterSpacing = 2.sp,
            )
            Text(
                text  = stringResource(R.string.devices_health_connect_devices),
                style = MaterialTheme.typography.bodySmall,
                color = colors.subtitle.copy(alpha = 0.7f),
            )

            val hcIntegration = state.integrations.find { it.provider == "apple_health" }

            HealthConnectCard(
                colors          = colors,
                integration     = hcIntegration,
                available       = state.healthConnectAvailable,
                error           = state.healthConnectError,
                isBusy          = state.isSyncing,
                syncStep        = state.syncStep,
                onSync          = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSyncHealthConnect()
                },
                onOpenSettings  = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenHcSettings()
                },
            )
        }
    }
}

// ── Provider card ─────────────────────────────────────────────────────────────

@Composable
private fun ProviderCard(
    provider:    Provider,
    integration: IntegrationStatusDto?,
    colors:      AppColors,
    isBusy:      Boolean,
    onConnect:   () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = integration?.connected == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        provider.color.copy(alpha = if (connected) 0.12f else 0.05f),
                        colors.surface.copy(alpha = 0.85f),
                    )
                )
            )
            .border(
                1.dp,
                if (connected) provider.color.copy(alpha = 0.35f)
                else colors.subtitle.copy(alpha = 0.12f),
                RoundedCornerShape(20.dp),
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(provider.color.copy(alpha = 0.15f), CircleShape),
                    ) {
                        Icon(
                            imageVector = providerIcon(provider.key),
                            contentDescription = provider.name,
                            tint = provider.color,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = provider.name,
                            style      = MaterialTheme.typography.titleSmall,
                            color      = colors.title,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text  = provider.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.subtitle,
                        )
                    }
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            if (connected) provider.color.copy(alpha = 0.18f)
                            else colors.subtitle.copy(alpha = 0.08f),
                            RoundedCornerShape(50.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (connected) {
                            Icon(
                                imageVector = LucideAppIcons.CircleCheck,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = provider.color,
                            )
                        }
                        Text(
                            text  = if (connected) stringResource(R.string.devices_connected_status) else stringResource(R.string.devices_not_linked),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (connected) provider.color else colors.subtitle,
                        )
                    }
                }
            }

            // ── Health data (only if connected + data exists) ─────────────────
            if (connected && integration?.data != null) {
                HealthDataRow(integration = integration, providerColor = provider.color, colors = colors)
            }

            // ── Last sync + action button ─────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                if (connected && integration?.lastSyncAt != null) {
                    Text(
                        text  = stringResource(R.string.devices_synced_ago, formatRelativeTime(integration.lastSyncAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.subtitle.copy(alpha = 0.6f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (connected) {
                    OutlinedButton(
                        onClick  = onDisconnect,
                        enabled  = !isBusy,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, colors.subtitle.copy(alpha = 0.3f)),
                    ) {
                        Text(stringResource(R.string.devices_disconnect), style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
                    }
                } else {
                    SpringButton(
                        onClick  = onConnect,
                        enabled  = !isBusy,
                        colors   = colors,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    ) {
                        Text(stringResource(R.string.devices_connect), style = MaterialTheme.typography.labelSmall, color = colors.onPrimary)
                    }
                }
            }
        }
    }
}

// ── Health data row ───────────────────────────────────────────────────────────

@Composable
private fun HealthDataRow(
    integration:   IntegrationStatusDto,
    providerColor: Color,
    colors:        AppColors,
) {
    val data = integration.data ?: return
    val latestSleep = mergeSleepDays(data.sleep ?: emptyList()).lastOrNull()
    val latestHrv   = data.hrv?.lastOrNull()
    val latestHr    = mergeHeartRateDays(data.heartRate ?: emptyList()).lastOrNull()
    val latestMetricDate = listOfNotNull(latestSleep?.date, latestHrv?.date, latestHr?.date)
        .maxByOrNull { parseHealthDate(it) ?: LocalDate.MIN }

    if (latestSleep == null && latestHrv == null && latestHr == null) return

    HorizontalDivider(color = providerColor.copy(alpha = 0.12f))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier              = Modifier.fillMaxWidth(),
        ) {
            latestSleep?.let { s ->
                HealthMetricChip(
                    label = "Sleep",
                    value = "${s.duration / 60}h ${s.duration % 60}m",
                    color = providerColor,
                    colors = colors,
                )
            }
            latestHrv?.let { h ->
                h.rmssd?.let { rmssd ->
                    HealthMetricChip(
                        label  = "HRV",
                        value  = "${rmssd.toInt()} ms",
                        color  = providerColor,
                        colors = colors,
                    )
                }
            }
            latestHr?.let { h ->
                val rate = h.restingRate ?: h.avgRate
                rate?.let {
                    HealthMetricChip(
                        label  = "Heart",
                        value  = "$it bpm",
                        color  = providerColor,
                        colors = colors,
                    )
                }
            }
        }

        latestMetricDate?.let { rawDate ->
            Text(
                text  = "Latest data date: ${formatMetricDate(rawDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun HealthMetricChip(label: String, value: String, color: Color, colors: AppColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle.copy(alpha = 0.7f),
        )
    }
}

// ── Health Connect card ───────────────────────────────────────────────────────

@Composable
private fun HealthConnectCard(
    colors:         AppColors,
    integration:    IntegrationStatusDto?,
    available:      Boolean,
    error:          String?,
    isBusy:         Boolean,
    syncStep:       String?,
    onSync:         () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val connected = integration != null
    val hcColor   = Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        hcColor.copy(alpha = if (connected) 0.10f else 0.04f),
                        colors.surface.copy(alpha = 0.85f),
                    )
                )
            )
            .border(
                1.dp,
                if (connected) hcColor.copy(alpha = 0.30f)
                else colors.subtitle.copy(alpha = 0.10f),
                RoundedCornerShape(20.dp),
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(hcColor.copy(alpha = 0.15f), CircleShape),
                    ) {
                        Icon(
                            imageVector = LucideAppIcons.HeartPulse,
                            contentDescription = "Health Connect",
                            tint = hcColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text       = "Health Connect",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = colors.title,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text  = if (available) stringResource(R.string.devices_hc_available) else stringResource(R.string.devices_hc_not_installed),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (available) hcColor.copy(alpha = 0.8f) else colors.subtitle,
                        )
                    }
                }
                if (connected) {
                    Box(
                        modifier = Modifier
                            .background(hcColor.copy(alpha = 0.18f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = LucideAppIcons.CircleCheck,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = hcColor,
                            )
                            Text(stringResource(R.string.devices_hc_synced_badge), style = MaterialTheme.typography.labelSmall, color = hcColor)
                        }
                    }
                }
            }

            if (connected && integration?.data != null) {
                HealthDataRow(integration = integration, providerColor = hcColor, colors = colors)
            }

            if (error != null) {
                val isPermissionError = error.contains("permissions", ignoreCase = true)
                Text(
                    text  = if (isPermissionError) stringResource(R.string.devices_hc_permissions_error) else error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                if (isPermissionError) {
                    SpringButton(
                        onClick  = onOpenSettings,
                        enabled  = true,
                        colors   = colors,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                    ) {
                        Text(stringResource(R.string.devices_open_hc_settings), style = MaterialTheme.typography.labelSmall, color = colors.onPrimary)
                    }
                }
            }

            if (available) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    if (connected && integration?.lastSyncAt != null && !isBusy) {
                        Text(
                            text  = stringResource(R.string.devices_synced_ago, formatRelativeTime(integration.lastSyncAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.subtitle.copy(alpha = 0.6f),
                        )
                    } else if (isBusy && syncStep != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(12.dp),
                                color       = hcColor.copy(alpha = 0.7f),
                                strokeWidth = 1.5.dp,
                            )
                            Text(
                                text  = syncStep,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.subtitle.copy(alpha = 0.8f),
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    SpringButton(
                        onClick  = onSync,
                        enabled  = !isBusy,
                        colors   = colors,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = colors.onPrimary, strokeWidth = 1.5.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = stringResource(R.string.devices_syncing),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onPrimary,
                            )
                        } else {
                            Text(
                                text  = if (connected) stringResource(R.string.devices_resync) else stringResource(R.string.devices_sync_now),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onPrimary,
                            )
                        }
                    }
                }
            } else {
                Text(
                    text  = stringResource(R.string.devices_install_hc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ── Spring button helper ──────────────────────────────────────────────────────

@Composable
private fun SpringButton(
    onClick: () -> Unit,
    enabled: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable () -> Unit,
) {
    val source    = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale     by animateFloatAsState(
        targetValue   = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "btnScale",
    )
    Button(
        onClick        = onClick,
        enabled        = enabled,
        interactionSource = source,
        modifier       = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        contentPadding = contentPadding,
        colors         = ButtonDefaults.buttonColors(
            containerColor         = colors.primary,
            disabledContainerColor = colors.primary.copy(alpha = 0.4f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        content()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun providerIcon(providerKey: String): ImageVector = when (providerKey) {
    "fitbit" -> LucideAppIcons.HeartPulse
    "google-fit" -> LucideAppIcons.Analytics
    else -> LucideAppIcons.Heart
}

private fun formatRelativeTime(isoString: String): String = runCatching {
    val instant = Instant.parse(isoString)
    val diffMin = (Instant.now().epochSecond - instant.epochSecond) / 60
    when {
        diffMin < 1    -> "just now"
        diffMin < 60   -> "${diffMin}m ago"
        diffMin < 1440 -> "${diffMin / 60}h ago"
        else           -> DateTimeFormatter.ofPattern("MMM d")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }
}.getOrDefault(isoString)

private fun formatMetricDate(raw: String): String = runCatching {
    val date = parseHealthDate(raw) ?: return@runCatching raw
    DateTimeFormatter.ofPattern("MMM d").format(date)
}.getOrDefault(raw)
