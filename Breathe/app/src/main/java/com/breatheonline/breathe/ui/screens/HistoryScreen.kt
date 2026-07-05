package com.breatheonline.breathe.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.HistoryViewModel
import com.breatheonline.breathe.viewmodel.SessionsStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.FormatStyle
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    colors: AppColors,
    navController: NavController? = null,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        ) {
            if (navController != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint               = colors.primary,
                    )
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.title,
                )
                val sessionCount = state.sessions.size
                val subtitle = if (sessionCount > 0)
                    "$sessionCount ${stringResource(R.string.history_sessions_count)}"
                else
                    stringResource(R.string.history_subtitle)
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.subtitle,
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        when (val status = state.status) {

            is SessionsStatus.Loading -> {
                val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
                val shimmerAlpha by shimmerTransition.animateFloat(
                    initialValue  = 0.25f,
                    targetValue   = 0.60f,
                    animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
                    label         = "shimmerAlpha",
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    repeat(5) {
                        ShimmerRow(colors, shimmerAlpha)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            is SessionsStatus.Empty -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(40.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Air,
                            contentDescription = null,
                            tint               = colors.subtitle,
                            modifier           = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text      = stringResource(R.string.history_empty_title),
                            style     = MaterialTheme.typography.headlineSmall,
                            color     = colors.title,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = stringResource(R.string.history_empty_hint),
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = colors.subtitle,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is SessionsStatus.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(40.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint               = colors.subtitle.copy(alpha = 0.5f),
                            modifier           = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text      = status.message,
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = colors.subtitle,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.fetch() },
                            shape   = RoundedCornerShape(14.dp),
                            colors  = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint               = colors.onPrimary,
                                modifier           = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = stringResource(R.string.btn_try_again),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onPrimary,
                            )
                        }
                    }
                }
            }

            is SessionsStatus.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        SessionHistoryRow(session, colors)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Session row ───────────────────────────────────────────────────────────────

@Composable
private fun SessionHistoryRow(session: RemoteSession, colors: AppColors) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        // Left: icon circle + type + date
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = typeIcon(session.type),
                    contentDescription = null,
                    tint               = colors.primary,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text(
                    text       = typeLabel(session.type),
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = colors.title,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text     = formatDateTime(session.effectiveDate),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = colors.subtitle,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }

        // Right: duration
        Text(
            text  = fmtMin(session.duration),
            style = MaterialTheme.typography.bodySmall,
            color = colors.primary,
        )
    }
}

// ── Shimmer skeleton ──────────────────────────────────────────────────────────

@Composable
private fun ShimmerRow(colors: AppColors, alpha: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        // Icon placeholder
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.subtitle.copy(alpha = alpha)),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(13.dp)
                    .background(colors.subtitle.copy(alpha = alpha), RoundedCornerShape(7.dp)),
            )
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.38f)
                    .height(11.dp)
                    .background(colors.subtitle.copy(alpha = alpha * 0.65f), RoundedCornerShape(6.dp)),
            )
        }
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(13.dp)
                .background(colors.subtitle.copy(alpha = alpha), RoundedCornerShape(7.dp)),
        )
    }
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

private fun typeIcon(type: String?): ImageVector = when (type) {
    "box"       -> Icons.Filled.CropSquare
    "deep"      -> Icons.Filled.Spa
    "energy"    -> Icons.Filled.Bolt
    "wimhof"    -> Icons.Filled.Bolt
    "coherent"  -> Icons.Filled.Spa
    "belly"     -> Icons.Filled.Air
    "morning"   -> Icons.Filled.Spa
    "alternate" -> Icons.Filled.Air
    else        -> Icons.Filled.Air   // "4-7-8" and unknown/null
}

@Composable
private fun typeLabel(type: String?): String = when {
    type == null               -> stringResource(R.string.history_session_breathing)
    type.startsWith("custom_") -> stringResource(R.string.history_session_custom)
    else -> when (type) {
        "4-7-8"     -> stringResource(R.string.history_session_4_7_8)
        "box"       -> stringResource(R.string.history_session_box)
        "wimhof"    -> stringResource(R.string.history_session_wim_hof)
        "coherent"  -> stringResource(R.string.history_session_coherent)
        "belly"     -> stringResource(R.string.history_session_belly)
        "morning"   -> stringResource(R.string.history_session_morning)
        "alternate" -> stringResource(R.string.history_session_alternate)
        "deep"      -> stringResource(R.string.history_session_deep)
        "energy"    -> stringResource(R.string.history_session_energising)
        else        -> stringResource(R.string.history_session_breathing)
    }
}

@Composable
private fun fmtMin(seconds: Int): String {
    val m = seconds / 60
    return if (m < 1) stringResource(R.string.history_less_than_minute) else "$m ${stringResource(R.string.stat_unit_min)}"
}

@Composable
private fun formatDateTime(completedAt: String): String {
    val instant = runCatching { Instant.parse(completedAt) }.getOrNull() ?: return completedAt
    val zdt     = instant.atZone(ZoneId.systemDefault())
    val date    = zdt.toLocalDate()
    val today   = LocalDate.now()
    val locale  = Locale.getDefault()
    val timeStr = zdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    return when (date) {
        today              -> "${stringResource(R.string.history_today)} · $timeStr"
        today.minusDays(1) -> "${stringResource(R.string.history_yesterday)} · $timeStr"
        else               -> "${date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))} · $timeStr"
    }
}
