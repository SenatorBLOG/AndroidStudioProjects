package com.breatheonline.breathe.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.example.breathe.data.models.*
import com.breatheonline.breathe.data.repository.AuthRepository
import com.example.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.utils.NotificationScheduler
import com.breatheonline.breathe.utils.UserPrefsKeys
import com.breatheonline.breathe.utils.userPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.edit
import com.breatheonline.breathe.data.models.BodyProfile
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.IntegrationStatusDto
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.data.models.UpdateProfileRequest
import com.breatheonline.breathe.data.models.UserChallengeDto
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

data class ProfileState(
    val userName: String = "",
    val userEmail: String = "",
    val avatar: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val age: Int? = null,
    val gender: String? = null,
    val goal: String? = null,
    
    // Stats & Sessions
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val sessions: List<RemoteSession> = emptyList(),

    // Challenges
    val availableChallenges: List<ChallengeDto> = emptyList(),
    val myChallenges: List<UserChallengeDto> = emptyList(),
    val recommendation: ChallengeRecommendationDto? = null,

    // Integrations
    val integrations: List<IntegrationStatusDto> = emptyList(),
    val isSyncing: Boolean = false,

    // UI State
    val notificationsEnabled:   Boolean = false,
    val reminderHour:           Int     = 8,
    val reminderMinute:         Int     = 0,
    val reminderIsExact:        Boolean = true,
    val dataCollectionEnabled:  Boolean = true,
    val isSavingProfile:        Boolean = false,
    val isLoading:              Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val meditationRepo: MeditationRepository,
    private val scheduler: NotificationScheduler,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        observeLocalSessions()
        refreshAll()
    }

    /** Show Room sessions immediately so the tab is never empty while the API loads. */
    private fun observeLocalSessions() {
        viewModelScope.launch {
            meditationRepo.getAllSessions().collect { local ->
                // Don't overwrite a successful API response
                if (_state.value.sessions.isNotEmpty()) return@collect
                if (local.isEmpty()) return@collect
                val zone  = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val mapped = local.sortedByDescending { it.date }.map { s ->
                    RemoteSession(
                        id = s.remoteId ?: s.id.toString(),
                        type = s.type,
                        duration = s.duration,
                        completedAt = Instant.ofEpochMilli(s.date).toString(),
                        sessionLength = (s.duration / 60).toDouble(),
                    )
                }
                val dates = local.map { s ->
                    Instant.ofEpochMilli(s.date).atZone(zone).toLocalDate()
                }.toSet()
                _state.update {
                    it.copy(
                        sessions      = mapped,
                        totalSessions = mapped.size,
                        totalMinutes  = mapped.sumOf { (it.sessionLength?.toInt() ?: 0) },
                        currentStreak = computeCurrentStreak(dates, today),
                        longestStreak = computeLongestStreak(dates),
                    )
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            fetchProfile()
            fetchSessions()
            fetchChallenges()
            fetchIntegrations()
            loadReminderSettings()
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun fetchProfile() {
        runCatching { apiService.getProfile() }.onSuccess { resp ->
            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    _state.update {
                        it.copy(
                            userName = user.nickname ?: user.name ?: "User",
                            userEmail = user.email,
                            avatar = user.avatar,
                            gender = user.bodyProfile?.gender,
                            height = user.bodyProfile?.heightCm,
                            weight = user.bodyProfile?.weightKg,
                            age = user.bodyProfile?.age,
                            goal = user.bodyProfile?.goal
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchSessions() {
        runCatching { apiService.getSessions() }.onSuccess { resp ->
            if (resp.isSuccessful) {
                val sessions = resp.body() ?: emptyList()
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                
                val dates = sessions.mapNotNull { s ->
                    val dateStr = s.sessionDate ?: s.completedAt
                    runCatching { Instant.parse(dateStr).atZone(zone).toLocalDate() }.getOrNull()
                }.toSet()

                _state.update {
                    it.copy(
                        sessions      = sessions,
                        totalSessions = sessions.size,
                        totalMinutes  = sessions.sumOf { s -> s.sessionLength?.toInt() ?: 0 },
                        currentStreak = computeCurrentStreak(dates, today),
                        longestStreak = computeLongestStreak(dates),
                    )
                }
            }
        }
    }

    private suspend fun fetchChallenges() {
        runCatching { apiService.getChallenges() }.onSuccess { r -> 
            if (r.isSuccessful) _state.update { it.copy(availableChallenges = r.body() ?: emptyList()) }
        }
        runCatching { apiService.getMyChallenges() }.onSuccess { r -> 
            if (r.isSuccessful) _state.update { it.copy(myChallenges = r.body() ?: emptyList()) }
        }
        runCatching { apiService.getChallengeRecommendation() }.onSuccess { r ->
            if (r.isSuccessful) _state.update { it.copy(recommendation = r.body()) }
        }
    }

    private suspend fun fetchIntegrations() {
        runCatching { apiService.getIntegrationStatus() }.onSuccess { r ->
            if (r.isSuccessful) _state.update { it.copy(integrations = r.body() ?: emptyList()) }
        }
    }

    fun saveProfile(nickname: String, avatar: String?, height: Int?, weight: Int?, age: String, gender: String, goal: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSavingProfile = true) }
            val bodyProfile = BodyProfile(
                heightCm = height,
                weightKg = weight,
                age = age.toIntOrNull(),
                gender = gender.ifBlank { null },
                goal = goal.ifBlank { null }
            )
            val req = UpdateProfileRequest(
                nickname = nickname,
                avatar = avatar,
                bodyProfile = bodyProfile
            )
            runCatching { apiService.updateProfile(req) }.onSuccess { refreshAll() }
            _state.update { it.copy(isSavingProfile = false) }
        }
    }

    fun joinChallenge(slug: String) {
        viewModelScope.launch {
            runCatching { apiService.joinChallenge(slug) }.onSuccess { fetchChallenges() }
        }
    }

    fun checkInChallenge(id: String) {
        viewModelScope.launch {
            runCatching { apiService.checkIn(id) }.onSuccess { fetchChallenges() }
        }
    }

    fun syncIntegrations() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            runCatching { apiService.syncIntegrations() }.onSuccess { fetchIntegrations() }
            _state.update { it.copy(isSyncing = false) }
        }
    }

    private fun loadReminderSettings() {
        viewModelScope.launch {
            val prefs = context.userPrefs.data.first()
            _state.update {
                it.copy(
                    notificationsEnabled  = prefs[UserPrefsKeys.NOTIFICATIONS_ENABLED]   == true,
                    reminderHour          = prefs[UserPrefsKeys.REMINDER_HOUR]            ?: 8,
                    reminderMinute        = prefs[UserPrefsKeys.REMINDER_MINUTE]          ?: 0,
                    reminderIsExact       = scheduler.canScheduleExact(),
                    dataCollectionEnabled = prefs[UserPrefsKeys.DATA_COLLECTION_ENABLED]  != false,
                )
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.userPrefs.edit { it[UserPrefsKeys.NOTIFICATIONS_ENABLED] = enabled }
            if (enabled) scheduler.schedule(_state.value.reminderHour, _state.value.reminderMinute)
            else scheduler.cancel()
            _state.update { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            context.userPrefs.edit {
                it[UserPrefsKeys.REMINDER_HOUR]   = hour
                it[UserPrefsKeys.REMINDER_MINUTE] = minute
            }
            if (_state.value.notificationsEnabled) scheduler.schedule(hour, minute)
            _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        }
    }

    fun setDataCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.userPrefs.edit { it[UserPrefsKeys.DATA_COLLECTION_ENABLED] = enabled }
            _state.update { it.copy(dataCollectionEnabled = enabled) }
        }
    }

    fun logout() = authRepository.logout()

    private fun computeCurrentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        val start = when {
            dates.contains(today) -> today
            dates.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var n = 0; var day = start
        while (dates.contains(day)) { n++; day = day.minusDays(1) }
        return n
    }

    private fun computeLongestStreak(dates: Set<LocalDate>): Int {
        val sorted = dates.sorted(); if (sorted.isEmpty()) return 0
        var best = 0; var cur = 0; var prev: LocalDate? = null
        for (date in sorted) {
            cur = if (prev == null || date == prev.plusDays(1)) cur + 1 else 1
            best = maxOf(best, cur)
            prev = date
        }
        return best
    }
}
