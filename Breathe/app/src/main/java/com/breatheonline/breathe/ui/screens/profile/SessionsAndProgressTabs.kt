package com.breatheonline.breathe.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.ProfileState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Sessions tab ──────────────────────────────────────────────────────────────

@Composable
internal fun SessionsTab(colors: AppColors, state: ProfileState) {
    val avgMinutes = if (state.totalSessions > 0) state.totalMinutes / state.totalSessions else 0
    val thisWeek = remember(state.sessions) {
        val cutoff = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        state.sessions.count { s ->
            runCatching {
                val dateStr = s.effectiveDate
                Instant.parse(dateStr).toEpochMilli() > cutoff
            }.getOrDefault(false)
        }
    }

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Bento row 1: hero + streak chips ────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BentoHero(
                value    = "${state.totalSessions}",
                label    = stringResource(R.string.sessions_label),
                subLabel = stringResource(R.string.sessions_total_completed),
                icon     = Icons.Default.Spa,
                colors   = colors,
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
            )
            Column(
                modifier            = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BentoChip(
                    value    = "${state.currentStreak}d",
                    label    = stringResource(R.string.sessions_streak_label),
                    icon     = Icons.Default.Bolt,
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                BentoChip(
                    value    = "${state.longestStreak}d",
                    label    = stringResource(R.string.sessions_best_label),
                    icon     = Icons.Default.EmojiEvents,
                    colors   = colors,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }

        // ── Bento row 2: three equal stat tiles ─────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BentoStat(
                value    = "${state.totalMinutes}",
                unit     = stringResource(R.string.stat_unit_min),
                label    = stringResource(R.string.sessions_total_label),
                icon     = Icons.Default.AccessTime,
                colors   = colors,
                modifier = Modifier.weight(1f),
            )
            BentoStat(
                value    = "$avgMinutes",
                unit     = stringResource(R.string.stat_unit_min),
                label    = stringResource(R.string.sessions_average_label),
                icon     = Icons.Default.TrendingUp,
                colors   = colors,
                modifier = Modifier.weight(1f),
            )
            BentoStat(
                value    = "$thisWeek",
                unit     = "",
                label    = stringResource(R.string.sessions_this_week_label),
                icon     = Icons.Default.DateRange,
                colors   = colors,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Session list ─────────────────────────────────────────────────────
        if (state.sessions.isEmpty()) {
            EmptySessionsCard(colors)
        } else {
            Spacer(Modifier.height(2.dp))
            Text(
                text       = stringResource(R.string.sessions_recent_label),
                style      = MaterialTheme.typography.labelMedium,
                color      = colors.subtitle,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 4.dp),
            )
            state.sessions.take(15).forEach { SessionItem(it, colors) }
        }
    }
}

// ── Bento tiles ───────────────────────────────────────────────────────────────

@Composable
private fun BentoHero(
    value:   String,
    label:   String,
    subLabel: String,
    icon:    ImageVector,
    colors: AppColors,
    modifier: Modifier,
) {
    val primary = colors.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.surface, colors.background),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .drawBehind {
                // Corner ambient glow (top-right)
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.68f,
                    ),
                    radius = size.width * 0.68f,
                    center = Offset(size.width, 0f),
                )
                // Top accent line
                drawLine(
                    brush       = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, primary.copy(alpha = 0.55f), Color.Transparent),
                    ),
                    start       = Offset(size.width * 0.10f, 1.dp.toPx()),
                    end         = Offset(size.width * 0.90f, 1.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx(),
                )
            }
            .border(1.dp, colors.subtitle.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Icon badge
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .background(primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = primary, modifier = Modifier.size(17.dp))
            }
            // Value + labels
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = value,
                    fontSize   = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.title,
                    lineHeight = 44.sp,
                )
                Text(
                    text          = label,
                    style         = MaterialTheme.typography.labelSmall,
                    color         = primary,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                )
                Text(
                    text  = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun BentoChip(
    value:   String,
    label:   String,
    icon:    ImageVector,
    colors: AppColors,
    modifier: Modifier,
) {
    Row(
        modifier              = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                color      = colors.title,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text          = label,
                style         = MaterialTheme.typography.labelSmall,
                color         = colors.primary,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
        }
        Box(
            modifier         = Modifier
                .size(26.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun BentoStat(
    value:   String,
    unit:    String,
    label:   String,
    icon:    ImageVector,
    colors: AppColors,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Box(
            modifier         = Modifier
                .size(28.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = colors.title, fontWeight = FontWeight.Bold)
            if (unit.isNotBlank()) {
                Text(unit, style = MaterialTheme.typography.labelSmall, color = colors.subtitle, modifier = Modifier.padding(bottom = 1.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.subtitle, letterSpacing = 0.5.sp)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptySessionsCard(colors: AppColors) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(vertical = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("🌬️", fontSize = 32.sp)
            Text(stringResource(R.string.sessions_no_sessions), color = colors.title, fontWeight = FontWeight.SemiBold)
            Text(
                text      = stringResource(R.string.sessions_complete_to_see),
                color     = colors.subtitle,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}

// ── Session row card ──────────────────────────────────────────────────────────

@Composable
private fun SessionItem(session: RemoteSession, colors: AppColors) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(typeIcon(session.type), contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text       = typeLabel(session.type, session.technique),
                style      = MaterialTheme.typography.bodyMedium,
                color      = colors.title,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text  = formatDateTime(session.effectiveDate),
                style = MaterialTheme.typography.labelSmall,
                color = colors.subtitle,
            )
        }
        val mins = session.sessionLength?.toInt() ?: if (session.duration > 0) session.duration / 60 else 0
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text       = "$mins min",
                style      = MaterialTheme.typography.bodySmall,
                color      = colors.primary,
                fontWeight = FontWeight.Bold,
            )
            val cycles = session.cycles ?: 0
            if (cycles > 0) {
                Text(
                    text  = "$cycles cycles",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.subtitle,
                )
            }
        }
    }
}

// ── Progress tab ──────────────────────────────────────────────────────────────

@Composable
internal fun ProgressTab(colors: AppColors, state: ProfileState) {
    val weeklyTarget  = 70f
    val progress      = (state.totalMinutes.toFloat() / weeklyTarget).coerceIn(0f, 1f)
    val avgPerSession = if (state.totalSessions > 0) state.totalMinutes / state.totalSessions else 0

    Column(
        modifier            = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard(stringResource(R.string.sessions_key_metrics), colors) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SessionStatCard(stringResource(R.string.sessions_stat_sessions),       "${state.totalSessions}",  colors, Modifier.weight(1f))
                SessionStatCard(stringResource(R.string.sessions_stat_minutes),        "${state.totalMinutes}",   colors, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SessionStatCard(stringResource(R.string.sessions_stat_current_streak), "${state.currentStreak}d", colors, Modifier.weight(1f))
                SessionStatCard(stringResource(R.string.sessions_stat_best_streak),    "${state.longestStreak}d", colors, Modifier.weight(1f))
            }
        }

        SectionCard(stringResource(R.string.sessions_weekly_goal), colors) {
            Text(stringResource(R.string.sessions_weekly_goal_pct, (progress * 100).toInt()), color = colors.title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(10.dp)
                    .background(colors.background.copy(alpha = 0.6f), CircleShape)
            ) {
                Box(
                    Modifier.fillMaxWidth(progress).height(10.dp)
                        .background(colors.primary, CircleShape)
                )
            }
            Text(
                text     = stringResource(R.string.sessions_weekly_goal_target, avgPerSession),
                color    = colors.subtitle,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SessionStatCard(label: String, value: String, colors: AppColors, modifier: Modifier) {
    Column(
        modifier.background(colors.background.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = colors.primary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.subtitle)
    }
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

private fun typeIcon(t: String?): ImageVector = when (t) {
    "box"    -> Icons.Default.CropSquare
    "deep"   -> Icons.Default.Spa
    "energy" -> Icons.Default.Bolt
    else     -> Icons.Default.Air
}

private fun typeLabel(t: String?, tech: String?): String = tech ?: when (t) {
    "box"   -> "Box Breathing"
    "4-7-8" -> "4-7-8 Breathing"
    else    -> "Breathing Session"
}

private fun formatDateTime(iso: String?): String = try {
    if (iso == null) "" else {
        Instant.parse(iso).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }
} catch (_: Exception) { iso ?: "" }
