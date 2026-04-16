package com.breatheonline.breathe.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.*
import com.breatheonline.breathe.data.repository.AuthRepository
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.utils.NotificationScheduler
import com.breatheonline.breathe.utils.SessionCalculations
import com.breatheonline.breathe.utils.TokenManager
import com.breatheonline.breathe.utils.UserPrefsKeys
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import com.breatheonline.breathe.utils.userPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.edit
import com.breatheonline.breathe.data.models.BodyProfile
import com.breatheonline.breathe.data.models.ChallengeDto
import com.breatheonline.breathe.data.models.ChallengeRecommendationDto
import com.breatheonline.breathe.data.models.HealthConnectImportRequest
import com.breatheonline.breathe.data.models.HrDayDto
import com.breatheonline.breathe.data.models.HrvDayDto
import com.breatheonline.breathe.data.models.IntegrationStatusDto
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.data.models.SleepDayDto
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
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
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
    val isConnecting: Boolean = false,
    val healthConnectAvailable: Boolean = false,
    val healthConnectError: String? = null,
    val needsHcPermissions: Boolean = false,
    val syncStep: String? = null,

    // UI State
    val notificationsEnabled:         Boolean = false,
    val reminderHour:                 Int     = 8,
    val reminderMinute:               Int     = 0,
    val reminderIsExact:              Boolean = true,
    val dataCollectionEnabled:        Boolean = true,
    val isSavingProfile:              Boolean = false,
    val isLoading:                    Boolean = false,
    val error:                        String? = null,
    val notificationPermissionNeeded: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val meditationRepo: MeditationRepository,
    private val scheduler: NotificationScheduler,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val API_BASE = "https://breathe-api-amut.onrender.com/api"
        val HC_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
        )
    }

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        observeLocalSessions()
        refreshAll()
        checkHealthConnectAvailability()
    }

    private fun checkHealthConnectAvailability() {
        val status = HealthConnectClient.getSdkStatus(context)
        _state.update {
            it.copy(healthConnectAvailable = status == HealthConnectClient.SDK_AVAILABLE)
        }
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
                        currentStreak = SessionCalculations.computeCurrentStreak(dates, today),
                        longestStreak = SessionCalculations.computeLongestStreak(dates),
                    )
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            fetchProfile()
            fetchSessions()
            fetchChallenges()
            fetchIntegrations()
            loadReminderSettings()
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private suspend fun fetchProfile() {
        runCatching { apiService.getProfile() }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    resp.body()?.let { user ->
                        _state.update {
                            it.copy(
                                userName  = user.nickname ?: user.name ?: "User",
                                userEmail = user.email,
                                avatar    = user.avatar,
                                gender    = user.bodyProfile?.gender,
                                height    = user.bodyProfile?.heightCm,
                                weight    = user.bodyProfile?.weightKg,
                                age       = user.bodyProfile?.age,
                                goal      = user.bodyProfile?.goal,
                            )
                        }
                    }
                } else {
                    _state.update { it.copy(error = "Failed to load profile (${resp.code()})") }
                }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Network error loading profile") }
            }
    }

    private suspend fun fetchSessions() {
        runCatching { apiService.getSessions() }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    val sessions = resp.body() ?: emptyList()
                    val zone  = ZoneId.systemDefault()
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
                            currentStreak = SessionCalculations.computeCurrentStreak(dates, today),
                            longestStreak = SessionCalculations.computeLongestStreak(dates),
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Failed to load sessions (${resp.code()})") }
                }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Network error loading sessions") }
            }
    }

    private suspend fun fetchChallenges() {
        runCatching { apiService.getChallenges() }
            .onSuccess { r ->
                if (r.isSuccessful) _state.update { it.copy(availableChallenges = r.body() ?: emptyList()) }
                else _state.update { it.copy(error = "Failed to load challenges (${r.code()})") }
            }
            .onFailure { e -> _state.update { it.copy(error = e.message ?: "Network error loading challenges") } }
        runCatching { apiService.getMyChallenges() }
            .onSuccess { r ->
                if (r.isSuccessful) _state.update { it.copy(myChallenges = r.body() ?: emptyList()) }
            }
        runCatching { apiService.getChallengeRecommendation() }
            .onSuccess { r ->
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

    /** Open Chrome Custom Tab to OAuth connect URL. Token passed as query param (backend reads it). */
    fun connectIntegration(provider: String, context: Context) {
        val token = tokenManager.getToken() ?: return
        // provider values: "fitbit" or "google-fit"
        val url = "$API_BASE/integrations/$provider/connect?token=$token"
        _state.update { it.copy(isConnecting = true) }
        CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()
            .launchUrl(context, url.toUri())
    }

    /** Call this from Activity.onResume after OAuth tab closes. */
    fun onResumeFromOAuth() {
        if (_state.value.isConnecting) {
            _state.update { it.copy(isConnecting = false) }
            viewModelScope.launch { fetchIntegrations() }
        }
    }

    fun disconnectIntegration(provider: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            runCatching { apiService.disconnectIntegration(provider) }
                .onSuccess { fetchIntegrations() }
                .onFailure { _state.update { s -> s.copy(error = "Failed to disconnect: ${it.message}") } }
            _state.update { it.copy(isSyncing = false) }
        }
    }

    /** Read from Health Connect and POST to /integrations/apple-health. */
    fun importFromHealthConnect() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, healthConnectError = null, syncStep = context.getString(com.breatheonline.breathe.R.string.hc_step_checking_permissions)) }
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val granted = client.permissionController.getGrantedPermissions()
                val canReadHeartRate = HealthPermission.getReadPermission(HeartRateRecord::class) in granted
                val canReadRestingHeartRate = HealthPermission.getReadPermission(RestingHeartRateRecord::class) in granted
                val canReadSleep = HealthPermission.getReadPermission(SleepSessionRecord::class) in granted
                val canReadSteps = HealthPermission.getReadPermission(StepsRecord::class) in granted

                if (!canReadHeartRate && !canReadRestingHeartRate && !canReadSleep && !canReadSteps) {
                    _state.update { it.copy(isSyncing = false, needsHcPermissions = true, syncStep = null) }
                    return@launch
                }

                val end   = Instant.now()
                // Увеличили до 30 дней - последний sync был 3 недели назад!
                val start = end.minus(30, ChronoUnit.DAYS)
                val range = TimeRangeFilter.between(start, end)
                val zone = ZoneId.systemDefault()

                android.util.Log.d("HealthConnect", "Reading data from $start to $end")

                // ── Sleep ─────────────────────────────────────────────────────
                _state.update { it.copy(syncStep = context.getString(com.breatheonline.breathe.R.string.hc_step_reading_sleep)) }
                val sleepRecords = if (canReadSleep) {
                    client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, range)).records
                } else {
                    emptyList()
                }
                android.util.Log.d("HealthConnect", "Found ${sleepRecords.size} sleep records")
                val sleep = mergeSleepDays(sleepRecords.map { s ->
                    val stageMin = s.stages.groupBy { it.stage }
                        .mapValues { (_, list) ->
                            list.sumOf { (it.endTime.epochSecond - it.startTime.epochSecond) / 60 }.toInt()
                        }
                    val sessionStart = s.startTime
                    val stageSegments = s.stages.mapNotNull { stage ->
                        val start = java.time.Duration.between(sessionStart, stage.startTime).toMinutes().toInt()
                        val end   = java.time.Duration.between(sessionStart, stage.endTime).toMinutes().toInt()
                        if (end <= start) return@mapNotNull null
                        val mapped = when (stage.stage) {
                            SleepSessionRecord.STAGE_TYPE_DEEP  -> SleepStage.DEEP
                            SleepSessionRecord.STAGE_TYPE_REM   -> SleepStage.REM
                            SleepSessionRecord.STAGE_TYPE_LIGHT -> SleepStage.LIGHT
                            SleepSessionRecord.STAGE_TYPE_AWAKE,
                            SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED -> SleepStage.AWAKE
                            else -> return@mapNotNull null
                        }
                        SleepStageSegment(startMin = start, endMin = end, stage = mapped)
                    }.sortedBy { it.startMin }
                    SleepDayDto(
                        date          = s.startTime.atZone(zone).toLocalDate().toString(),
                        duration      = ((s.endTime.epochSecond - s.startTime.epochSecond) / 60).toInt(),
                        deepSleepMin  = stageMin[SleepSessionRecord.STAGE_TYPE_DEEP],
                        remSleepMin   = stageMin[SleepSessionRecord.STAGE_TYPE_REM],
                        lightSleepMin = stageMin[SleepSessionRecord.STAGE_TYPE_LIGHT],
                        awakeMin      = (stageMin[SleepSessionRecord.STAGE_TYPE_AWAKE] ?: 0) +
                                        (stageMin[SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED] ?: 0),
                        bedtime  = s.startTime.toString(),
                        wakeTime = s.endTime.toString(),
                        stages   = stageSegments.takeIf { it.isNotEmpty() },
                        score    = null,
                    )
                }, zone)

                // ── Resting heart rate ────────────────────────────────────────
                _state.update { it.copy(syncStep = context.getString(com.breatheonline.breathe.R.string.hc_step_reading_heart_rate)) }
                val heartRateRecords = if (canReadHeartRate) {
                    client.readRecords(ReadRecordsRequest(HeartRateRecord::class, range)).records
                } else {
                    emptyList()
                }
                val restingHeartRateRecords = if (canReadRestingHeartRate) {
                    client.readRecords(ReadRecordsRequest(RestingHeartRateRecord::class, range)).records
                } else {
                    emptyList()
                }
                val avgHeartRateDays = heartRateRecords
                    .flatMap { record ->
                        android.util.Log.d(
                            "HealthConnect",
                            "HeartRate origin: ${record.metadata.dataOrigin.packageName}, samples=${record.samples.size}"
                        )
                        record.samples.map { sample ->
                            sample.time.atZone(zone).toLocalDate().toString() to sample.beatsPerMinute.toDouble()
                        }
                    }
                    .groupBy({ it.first }, { it.second })
                    .map { (date, samples) ->
                        HrDayDto(
                            date = date,
                            restingRate = null,
                            avgRate = samples.average().roundToInt(),
                        )
                    }
                val restingHeartRateDays = restingHeartRateRecords.map { record ->
                    android.util.Log.d(
                        "HealthConnect",
                        "RestingHeartRate origin: ${record.metadata.dataOrigin.packageName}, bpm=${record.beatsPerMinute}"
                    )
                    HrDayDto(
                        date = record.time.atZone(zone).toLocalDate().toString(),
                        restingRate = record.beatsPerMinute.toInt(),
                        avgRate = null,
                    )
                }
                val heartRate = mergeHeartRateDays(avgHeartRateDays + restingHeartRateDays, zone)
                android.util.Log.d(
                    "HealthConnect",
                    "Merged ${heartRateRecords.size} heart-rate series and ${restingHeartRateRecords.size} resting records into ${heartRate.size} day buckets"
                )

                // ── Steps ─────────────────────────────────────────────────────
                _state.update { it.copy(syncStep = context.getString(com.breatheonline.breathe.R.string.hc_step_reading_steps)) }
                val stepsRecords = if (canReadSteps) {
                    client.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records
                } else {
                    emptyList()
                }
                val totalSteps = stepsRecords.sumOf { it.count }.toInt().takeIf { canReadSteps }
                android.util.Log.d("HealthConnect", "Found ${stepsRecords.size} steps records, total: $totalSteps")

                // ── HRV — not natively available in Health Connect v1 ─────────
                val hrv = emptyList<HrvDayDto>()

                android.util.Log.d("HealthConnect", "Sending: ${sleep.size} sleep, ${heartRate.size} HR, $totalSteps steps")

                _state.update { it.copy(syncStep = context.getString(com.breatheonline.breathe.R.string.hc_step_sending)) }
                val hasData = sleep.isNotEmpty() || heartRate.isNotEmpty() || (totalSteps != null && totalSteps > 0)
                if (!hasData) {
                    _state.update { it.copy(healthConnectError = context.getString(com.breatheonline.breathe.R.string.hc_no_data_error)) }
                    return@launch
                }

                val response = apiService.importHealthData(
                    HealthConnectImportRequest(sleep = sleep, hrv = hrv, heartRate = heartRate, steps = totalSteps)
                )
                if (response.isSuccessful) {
                    fetchIntegrations()
                } else {
                    _state.update { it.copy(healthConnectError = "Server error: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(healthConnectError = e.message ?: "Sync failed") }
            } finally {
                _state.update { it.copy(isSyncing = false, syncStep = null) }
            }
        }
    }

    fun clearHealthConnectError() { _state.update { it.copy(healthConnectError = null) } }

    fun clearHcPermissionsRequest() { _state.update { it.copy(needsHcPermissions = false) } }

    fun onHcPermissionsDenied() {
        _state.update { it.copy(needsHcPermissions = false, healthConnectError = "Health Connect permissions not granted. Check app settings.") }
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

    fun clearNotificationPermissionRequest() {
        _state.update { it.copy(notificationPermissionNeeded = false) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    _state.update { it.copy(notificationPermissionNeeded = true) }
                    return@launch
                }
            }
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

    fun abandonChallenge(id: String) {
        viewModelScope.launch {
            runCatching { apiService.abandonChallenge(id) }.onSuccess { fetchChallenges() }
        }
    }

    fun logout() = authRepository.logout()
}
