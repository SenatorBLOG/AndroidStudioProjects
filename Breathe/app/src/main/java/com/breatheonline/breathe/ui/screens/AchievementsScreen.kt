package com.breatheonline.breathe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.breatheonline.breathe.R
import com.breatheonline.breathe.data.models.AchievementDto
import com.breatheonline.breathe.data.models.AchievementLevelDto
import com.breatheonline.breathe.ui.icons.LucideAppIcons
import com.breatheonline.breathe.ui.theme.AppColors
import com.breatheonline.breathe.viewmodel.AchievementsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun AchievementsScreen(
    colors: AppColors,
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
            }
            Column {
                Text(stringResource(R.string.achievements_title_text), style = MaterialTheme.typography.headlineSmall, color = colors.title)
                Text(
                    stringResource(R.string.achievements_subtitle_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.subtitle,
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            state.achievements.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.achievements_no_achievements), color = colors.subtitle)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                item {
                    AchievementOverview(state.highlights, state.achievements, colors)
                }
                items(state.achievements) { achievement ->
                    AchievementListCard(
                        achievement = achievement,
                        colors = colors,
                        onClick = { navController.navigate(Route.achievement(achievement.slug)) },
                    )
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun AchievementDetailScreen(
    slug: String,
    colors: AppColors,
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var achievement by remember(slug) { mutableStateOf<AchievementDto?>(null) }
    var isResolved by remember(slug) { mutableStateOf(false) }

    LaunchedEffect(slug) {
        viewModel.getAchievement(slug) {
            achievement = it
            isResolved = true
        }
    }

    LaunchedEffect(slug, state.achievements) {
        if (achievement == null) {
            state.achievements.firstOrNull { it.slug == slug }?.let {
                achievement = it
                isResolved = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
            }
            Text(stringResource(R.string.achievements_detail_title), style = MaterialTheme.typography.headlineSmall, color = colors.title)
        }

        val item = achievement
        when {
            !isResolved -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            item == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.achievements_not_found), color = colors.subtitle)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            ) {
                item {
                    AchievementHeroCard(item, colors)
                }
                item {
                    AchievementMetaCard(item, colors)
                }
                item {
                    Text(stringResource(R.string.achievements_levels), style = MaterialTheme.typography.titleMedium, color = colors.title, fontWeight = FontWeight.SemiBold)
                }
                items(item.levels) { level ->
                    AchievementLevelCard(item, level, colors)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun AchievementHighlightsSection(
    colors: AppColors,
    navController: NavController,
    title: String = "ACHIEVEMENTS",
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (!state.isLoading && state.highlights.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = colors.subtitle,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(94.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        } else {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.highlights) { achievement ->
                    AchievementHighlightCard(
                        achievement = achievement,
                        colors = colors,
                        onClick = { navController.navigate(Route.achievement(achievement.slug)) },
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(112.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                            .clickable { navController.navigate(Route.ACHIEVEMENTS) }
                            .padding(14.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Text(stringResource(R.string.achievements_view_all), color = colors.title, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.achievements_count_in_progress, state.achievements.count { it.currentLevel > 0 }, state.achievements.size), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementOverview(highlights: List<AchievementDto>, achievements: List<AchievementDto>, colors: AppColors) {
    val completed = achievements.count { it.status == "completed" }
    val active = achievements.count { it.status == "in_progress" }
    val avgProgress = achievements.map { achievementProgress(it) }.average().takeIf { !it.isNaN() } ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.achievements_one_system), color = colors.title, style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OverviewMetric("${completed}/${achievements.size}", stringResource(R.string.achievements_completed), colors, Modifier.weight(1f))
            OverviewMetric(active.toString(), stringResource(R.string.achievements_in_progress), colors, Modifier.weight(1f))
            OverviewMetric("${avgProgress.roundToInt()}%", stringResource(R.string.achievements_avg_progress), colors, Modifier.weight(1f))
        }
        if (highlights.isNotEmpty()) {
            Text(stringResource(R.string.achievements_highlights), color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
            highlights.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AchievementIcon(item, colors, Modifier.size(34.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = colors.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(progressSummary(item), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewMetric(value: String, label: String, colors: AppColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Text(value, color = colors.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Text(label.uppercase(), color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun AchievementHighlightCard(
    achievement: AchievementDto,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(208.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(accentColor(achievement, colors).copy(alpha = 0.14f), colors.surface),
                )
            )
            .border(1.dp, accentColor(achievement, colors).copy(alpha = 0.20f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AchievementIcon(achievement, colors, Modifier.size(38.dp))
                Column(Modifier.weight(1f)) {
                    Text(achievement.title, color = colors.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(progressSummary(achievement), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
            AchievementProgressBar(achievement, colors)
        }
    }
}

@Composable
private fun AchievementListCard(
    achievement: AchievementDto,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementIcon(achievement, colors, Modifier.size(54.dp))
            Column(Modifier.weight(1f)) {
                Text(achievement.title, color = colors.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(achievement.description, color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.subtitle)
        }
        AchievementProgressBar(achievement, colors)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(progressSummary(achievement), color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
            Text(levelSummary(achievement), color = accentColor(achievement, colors), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AchievementHeroCard(achievement: AchievementDto, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accentColor(achievement, colors).copy(alpha = 0.18f), colors.surface),
                )
            )
            .border(1.dp, accentColor(achievement, colors).copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AchievementIcon(achievement, colors, Modifier.size(72.dp))
            Column(Modifier.weight(1f)) {
                Text(achievement.title, style = MaterialTheme.typography.headlineSmall, color = colors.title, fontWeight = FontWeight.Bold)
                Text(achievement.description, color = colors.subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
        AchievementProgressBar(achievement, colors, height = 10.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            HeroPill(levelSummary(achievement), colors)
            HeroPill(progressSummary(achievement), colors)
        }
    }
}

@Composable
private fun HeroPill(text: String, colors: AppColors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, color = colors.subtitle, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AchievementMetaCard(achievement: AchievementDto, colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.subtitle.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetaRow("Sources", achievement.sourceTypes.joinToString(" · ").ifBlank { "app · web" }, colors)
        MetaRow("Last progress", formatAchievementDate(achievement.lastProgressAt) ?: "Not started yet", colors)
        MetaRow("Completed", formatAchievementDate(achievement.completedAt) ?: "Not completed yet", colors)
        val nextLevel = achievement.levels.firstOrNull { it.targetValue > achievement.currentValue }
        MetaRow(
            "Next target",
            nextLevel?.let { "${formatValue(it.targetValue)} ${achievement.unit} · ${it.label}" } ?: "All levels complete",
            colors,
        )
    }
}

@Composable
private fun MetaRow(label: String, value: String, colors: AppColors) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label.uppercase(), color = colors.subtitle, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(16.dp))
        Text(value, color = colors.title, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
    }
}

@Composable
private fun AchievementLevelCard(achievement: AchievementDto, level: AchievementLevelDto, colors: AppColors) {
    val unlocked = achievement.currentValue >= level.targetValue
    val levelProgress = (achievement.currentValue / level.targetValue).coerceIn(0.0, 1.0)
    val tint = if (unlocked) accentColor(achievement, colors) else colors.subtitle

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AchievementLevelStars(level = level.level, tint = tint)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(level.label, color = colors.title, fontWeight = FontWeight.SemiBold)
                Text(level.description ?: "", color = colors.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (unlocked) "Unlocked" else "${(levelProgress * 100).roundToInt()}%",
                color = tint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(levelProgress.toFloat())
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(tint),
            )
        }
        Text(
            "${formatValue(achievement.currentValue)} / ${formatValue(level.targetValue)} ${achievement.unit}",
            color = colors.subtitle,
            style = MaterialTheme.typography.bodySmall,
        )
        level.earnedAt?.let {
            formatAchievementDate(it)?.let { formatArgs -> Text(stringResource(R.string.achievements_unlocked_on, formatArgs), color = tint, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun AchievementProgressBar(achievement: AchievementDto, colors: AppColors, height: androidx.compose.ui.unit.Dp = 8.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(colors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((achievementProgress(achievement) / 100f).coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor(achievement, colors)),
        )
    }
}

@Composable
private fun AchievementIcon(achievement: AchievementDto, colors: AppColors, modifier: Modifier = Modifier) {
    val grayscale = achievement.status == "locked"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (grayscale) colors.subtitle.copy(alpha = 0.12f)
                else accentColor(achievement, colors).copy(alpha = 0.14f),
            )
            .border(
                1.dp,
                if (grayscale) colors.subtitle.copy(alpha = 0.16f) else accentColor(achievement, colors).copy(alpha = 0.22f),
                RoundedCornerShape(18.dp),
            ),
    ) {
        if (achievement.iconUrl != null) {
            AsyncImage(
                model = achievement.iconUrl,
                contentDescription = achievement.title,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
            )
        } else {
            Icon(
                imageVector = achievementIcon(achievement),
                contentDescription = achievement.title,
                tint = if (grayscale) colors.subtitle else accentColor(achievement, colors),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun achievementProgress(achievement: AchievementDto): Int {
    val maxTarget = achievement.levels.maxOfOrNull { it.targetValue } ?: return 0
    return ((achievement.currentValue / maxTarget).coerceIn(0.0, 1.0) * 100).roundToInt()
}

private fun progressSummary(achievement: AchievementDto): String {
    val next = achievement.levels.firstOrNull { it.targetValue > achievement.currentValue }
    return if (next == null) {
        "Completed ${achievement.maxLevel}/${achievement.maxLevel} levels"
    } else {
        "${formatValue(achievement.currentValue)} / ${formatValue(next.targetValue)} ${achievement.unit}"
    }
}

private fun levelSummary(achievement: AchievementDto): String =
    "Level ${achievement.currentLevel}/${achievement.maxLevel}"

private fun accentColor(achievement: AchievementDto, colors: AppColors): Color = when (achievement.category) {
    "sleep" -> Color(0xFF5E81FF)
    "health" -> Color(0xFFE85D75)
    "meditation" -> colors.primary
    else -> Color(0xFFF5B700)
}

private fun levelStars(level: Int): String = when (level) {
    1 -> "★"
    2 -> "★★"
    else -> "★★★"
}

@Composable
private fun AchievementLevelStars(level: Int, tint: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(level.coerceIn(1, 3)) {
            Icon(
                imageVector = LucideAppIcons.Star,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun achievementIcon(achievement: AchievementDto): ImageVector = when (achievement.iconKey) {
    "achievement_first_breath" -> LucideAppIcons.Wind
    "achievement_streak" -> LucideAppIcons.Sparkles
    "achievement_minutes" -> LucideAppIcons.AlarmClock
    "achievement_sync" -> LucideAppIcons.HeartPulse
    "achievement_sleep" -> LucideAppIcons.Bed
    "achievement_recovery" -> LucideAppIcons.Heart
    "achievement_hours" -> LucideAppIcons.MoonStar
    "achievement_marathon" -> LucideAppIcons.Trophy
    else -> when (achievement.category) {
        "sleep" -> LucideAppIcons.MoonStar
        "health" -> LucideAppIcons.HeartPulse
        "meditation" -> LucideAppIcons.Wind
        else -> LucideAppIcons.Trophy
    }
}

private fun formatValue(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)

private fun formatAchievementDate(raw: String?): String? = raw?.let {
    runCatching {
        DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(it))
    }.getOrNull()
}
