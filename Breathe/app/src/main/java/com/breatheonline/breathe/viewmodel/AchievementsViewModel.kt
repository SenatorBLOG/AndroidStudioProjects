package com.breatheonline.breathe.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.AchievementDto
import com.breatheonline.breathe.data.models.AchievementLevelDto
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.R
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AchievementsState(
    val isLoading: Boolean = true,
    val achievements: List<AchievementDto> = emptyList(),
    val highlights: List<AchievementDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val meditationRepository: MeditationRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementsState())
    val state: StateFlow<AchievementsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val achievementsDeferred = async { runCatching { apiService.getAchievements() }.getOrNull() }
            val highlightsDeferred = async { runCatching { apiService.getAchievementHighlights() }.getOrNull() }

            val achievementsResponse = achievementsDeferred.await()
            val highlightsResponse = highlightsDeferred.await()

            val achievements = achievementsResponse
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.takeIf { it.isNotEmpty() }

            if (achievements != null) {
                val highlights = highlightsResponse
                    ?.takeIf { it.isSuccessful }
                    ?.body()
                    ?.items
                    ?.takeIf { it.isNotEmpty() }
                    ?: achievements.sortedByDescending { it.currentLevel }.take(3)

                _state.update {
                    it.copy(
                        isLoading = false,
                        achievements = achievements.sortedBy { item -> item.title },
                        highlights = highlights,
                    )
                }
                return@launch
            }

            val fallback = buildFallbackAchievements()
            _state.update {
                it.copy(
                    isLoading = false,
                    achievements = fallback,
                    highlights = fallback.sortedByDescending { achievementSortScore(it) }.take(3),
                    error = achievementsResponse?.let { context.getString(R.string.achievements_api_unavailable, it.code()) },
                )
            }
        }
    }

    fun getAchievement(slug: String, onResult: (AchievementDto?) -> Unit) {
        val cached = _state.value.achievements.firstOrNull { it.slug == slug }
        if (cached != null) {
            onResult(cached)
            return
        }

        viewModelScope.launch {
            val remote = runCatching { apiService.getAchievement(slug) }.getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
            onResult(remote)
        }
    }

    private suspend fun buildFallbackAchievements(): List<AchievementDto> {
        val localSessions = meditationRepository.getAllSessions().first()
        val integrations = runCatching { apiService.getIntegrationStatus() }
            .getOrNull()
            ?.takeIf { it.isSuccessful }
            ?.body()
            .orEmpty()
            .filter { it.connected && it.data != null }

        val zone = ZoneId.systemDefault()
        val sessionDates = localSessions.map { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }.toSet()
        val currentStreak = computeCurrentStreak(sessionDates, LocalDate.now(zone))
        val totalMinutes = localSessions.sumOf { it.duration } / 60.0
        val totalSessions = localSessions.size.toDouble()
        val connectedProviders = integrations.size.toDouble()
        val sleepDays = mergeSleepDays(integrations.flatMap { it.data?.sleep.orEmpty() })
        val heartDays = mergeHeartRateDays(integrations.flatMap { it.data?.heartRate.orEmpty() })
        val avgSleepHours = sleepDays.takeLast(14).map { it.duration }.average().takeIf { !it.isNaN() }?.div(60.0) ?: 0.0
        val syncedNights = sleepDays.size.toDouble()
        val heartSyncDays = heartDays.size.toDouble()
        val latestProgressAt = listOfNotNull(
            localSessions.maxByOrNull { it.date }?.date?.let { Instant.ofEpochMilli(it).toString() },
            integrations.mapNotNull { it.lastSyncAt }.maxOrNull(),
        ).maxOrNull()

        return listOf(
            createAchievement(
                slug = "first-breath",
                title = resString(R.string.achievement_first_breath_title),
                description = resString(R.string.achievement_first_breath_desc),
                category = "meditation",
                unit = resString(R.string.achievement_unit_sessions),
                iconKey = "achievement_first_breath",
                currentValue = totalSessions,
                levels = listOf(
                    level(1, R.string.achievement_first_breath_level_1_title, 1.0, R.string.achievement_first_breath_level_1_desc),
                    level(2, R.string.achievement_first_breath_level_2_title, 5.0, R.string.achievement_first_breath_level_2_desc),
                    level(3, R.string.achievement_first_breath_level_3_title, 25.0, R.string.achievement_first_breath_level_3_desc),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("app_sessions", "web_sessions"),
            ),
            createAchievement(
                slug = "streak-builder",
                title = resString(R.string.achievement_streak_builder_title),
                description = resString(R.string.achievement_streak_builder_desc),
                category = "meditation",
                unit = resString(R.string.stat_unit_days),
                iconKey = "achievement_streak",
                currentValue = currentStreak.toDouble(),
                levels = listOf(
                    level(1, R.string.achievement_streak_builder_level_1_title, 3.0, R.string.achievement_reach_day_streak_desc, 3),
                    level(2, R.string.achievement_streak_builder_level_2_title, 7.0, R.string.achievement_reach_day_streak_desc, 7),
                    level(3, R.string.achievement_streak_builder_level_3_title, 30.0, R.string.achievement_reach_day_streak_desc, 30),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("app_sessions", "web_sessions"),
            ),
            createAchievement(
                slug = "breath-minutes",
                title = resString(R.string.achievement_breath_minutes_title),
                description = resString(R.string.achievement_breath_minutes_desc),
                category = "meditation",
                unit = resString(R.string.achievement_unit_minutes),
                iconKey = "achievement_minutes",
                currentValue = totalMinutes,
                levels = listOf(
                    level(1, R.string.achievement_breath_minutes_level_1_title, 120.0, R.string.achievement_meditate_hours_desc, 2),
                    level(2, R.string.achievement_breath_minutes_level_2_title, 600.0, R.string.achievement_meditate_hours_desc, 10),
                    level(3, R.string.achievement_breath_minutes_level_3_title, 1800.0, R.string.achievement_meditate_hours_desc, 30),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("app_sessions", "web_sessions"),
            ),
            createAchievement(
                slug = "health-sync",
                title = resString(R.string.achievement_health_sync_title),
                description = resString(R.string.achievement_health_sync_desc),
                category = "health",
                unit = resString(R.string.achievement_unit_sources),
                iconKey = "achievement_sync",
                currentValue = connectedProviders,
                levels = listOf(
                    level(1, R.string.devices_connected_status, 1.0, R.string.achievement_health_sync_level_1_desc),
                    level(2, R.string.achievement_health_sync_level_2_title, 2.0, R.string.achievement_health_sync_level_2_desc),
                    level(3, R.string.achievement_health_sync_level_3_title, 3.0, R.string.achievement_health_sync_level_3_desc),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("health_connect", "fitbit", "google_fit"),
            ),
            createAchievement(
                slug = "sleep-rhythm",
                title = resString(R.string.achievement_sleep_rhythm_title),
                description = resString(R.string.achievement_sleep_rhythm_desc),
                category = "sleep",
                unit = resString(R.string.achievement_unit_nights),
                iconKey = "achievement_sleep",
                currentValue = syncedNights,
                levels = listOf(
                    level(1, R.string.achievement_sleep_rhythm_level_1_title, 3.0, R.string.achievement_sync_nights_desc, 3),
                    level(2, R.string.achievement_sleep_rhythm_level_2_title, 14.0, R.string.achievement_sync_nights_desc, 14),
                    level(3, R.string.achievement_sleep_rhythm_level_3_title, 60.0, R.string.achievement_sync_nights_desc, 60),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("health_connect", "wearables"),
            ),
            createAchievement(
                slug = "recovery-heart",
                title = resString(R.string.achievement_recovery_heart_title),
                description = resString(R.string.achievement_recovery_heart_desc),
                category = "health",
                unit = resString(R.string.stat_unit_days),
                iconKey = "achievement_recovery",
                currentValue = heartSyncDays,
                levels = listOf(
                    level(1, R.string.achievement_recovery_heart_level_1_title, 3.0, R.string.achievement_capture_heart_rate_days_desc, 3),
                    level(2, R.string.achievement_recovery_heart_level_2_title, 14.0, R.string.achievement_capture_heart_rate_days_desc, 14),
                    level(3, R.string.achievement_recovery_heart_level_3_title, 45.0, R.string.achievement_capture_heart_rate_days_desc, 45),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("health_connect", "wearables"),
            ),
            createAchievement(
                slug = "sleep-hours",
                title = resString(R.string.achievement_sleep_hours_title),
                description = resString(R.string.achievement_sleep_hours_desc),
                category = "sleep",
                unit = resString(R.string.achievement_unit_hours),
                iconKey = "achievement_hours",
                currentValue = avgSleepHours * syncedNights,
                levels = listOf(
                    level(1, R.string.achievement_sleep_hours_level_1_title, 20.0, R.string.achievement_reach_tracked_sleep_hours_desc, 20),
                    level(2, R.string.achievement_sleep_hours_level_2_title, 100.0, R.string.achievement_reach_tracked_sleep_hours_desc, 100),
                    level(3, R.string.achievement_sleep_hours_level_3_title, 300.0, R.string.achievement_reach_tracked_sleep_hours_desc, 300),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("health_connect", "wearables"),
            ),
            createAchievement(
                slug = "session-marathon",
                title = resString(R.string.achievement_session_marathon_title),
                description = resString(R.string.achievement_session_marathon_desc),
                category = "meditation",
                unit = resString(R.string.achievement_unit_sessions),
                iconKey = "achievement_marathon",
                currentValue = totalSessions,
                levels = listOf(
                    countLevel(1, 10, R.string.achievement_unit_sessions, 10.0, R.string.achievement_complete_total_sessions_desc, 10),
                    countLevel(2, 100, R.string.achievement_unit_sessions, 100.0, R.string.achievement_complete_total_sessions_desc, 100),
                    countLevel(3, 500, R.string.achievement_unit_sessions, 500.0, R.string.achievement_complete_total_sessions_desc, 500),
                ),
                lastProgressAt = latestProgressAt,
                sourceTypes = listOf("app_sessions", "web_sessions"),
            ),
        )
    }

    private fun resString(@StringRes resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)

    private fun level(
        order: Int,
        @StringRes titleRes: Int,
        targetValue: Double,
        @StringRes descriptionRes: Int,
        vararg descriptionArgs: Any,
    ): AchievementLevelDto = AchievementLevelDto(
        order,
        resString(titleRes),
        targetValue,
        resString(descriptionRes, *descriptionArgs),
    )

    private fun countLevel(
        order: Int,
        count: Int,
        @StringRes unitRes: Int,
        targetValue: Double,
        @StringRes descriptionRes: Int,
        vararg descriptionArgs: Any,
    ): AchievementLevelDto = AchievementLevelDto(
        order,
        resString(R.string.achievement_count_unit_title, count, resString(unitRes)),
        targetValue,
        resString(descriptionRes, *descriptionArgs),
    )

    private fun createAchievement(
        slug: String,
        title: String,
        description: String,
        category: String,
        unit: String,
        iconKey: String,
        currentValue: Double,
        levels: List<AchievementLevelDto>,
        lastProgressAt: String?,
        sourceTypes: List<String>,
    ): AchievementDto {
        val completedLevel = levels.count { currentValue >= it.targetValue }
        val completedAt = if (completedLevel >= levels.size) lastProgressAt else null
        val status = when {
            completedLevel >= levels.size -> "completed"
            currentValue > 0 -> "in_progress"
            else -> "locked"
        }
        val decoratedLevels = levels.map { level ->
            level.copy(earnedAt = if (currentValue >= level.targetValue) lastProgressAt else null)
        }
        return AchievementDto(
            id = slug,
            slug = slug,
            title = title,
            description = description,
            category = category,
            unit = unit,
            iconKey = iconKey,
            sourceTypes = sourceTypes,
            currentValue = currentValue,
            currentLevel = completedLevel,
            maxLevel = levels.size,
            status = status,
            levels = decoratedLevels,
            lastProgressAt = lastProgressAt,
            completedAt = completedAt,
        )
    }

    private fun achievementSortScore(achievement: AchievementDto): Double {
        val nextTarget = achievement.levels.firstOrNull { it.targetValue > achievement.currentValue }?.targetValue
            ?: achievement.levels.lastOrNull()?.targetValue
            ?: 1.0
        return achievement.currentLevel * 10 + (achievement.currentValue / nextTarget).coerceIn(0.0, 1.0)
    }

    private fun computeCurrentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        var day = when {
            today in dates -> today
            today.minusDays(1) in dates -> today.minusDays(1)
            else -> return 0
        }
        var count = 0
        while (day in dates) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
