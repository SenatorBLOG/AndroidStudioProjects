package com.breatheonline.breathe.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breatheonline.breathe.data.api.ApiService
import com.breatheonline.breathe.data.models.HrDayDto
import com.breatheonline.breathe.data.models.HrvDayDto
import com.breatheonline.breathe.data.models.MeditationSession
import com.breatheonline.breathe.data.models.NlpInsights
import com.breatheonline.breathe.data.models.RemoteSession
import com.breatheonline.breathe.data.models.SleepDayDto
import com.breatheonline.breathe.data.models.SleepInsightFeedback
import com.breatheonline.breathe.data.repository.IntegrationRepository
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.data.repository.SleepInsightFeedbackRepository
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import com.breatheonline.breathe.utils.parseHealthDate
import com.breatheonline.breathe.utils.SessionCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

// ── Supporting types ───────────────────────────────────────────────────────────

data class DayMinutes(val date: LocalDate, val minutes: Int)

data class MoodPoint(
    val date:  LocalDate,
    val delta: Int?,
)

data class InsightCard(
    val emoji: String,
    val title: String,
    val body:  String,
)

data class HealthSummary(
    val avgSleep7dMin: Int?,
    val avgHrv7d:      Int?,
    val restingHr:     Int?,
    val recoveryScore: Int?,
    val sources:       List<String>,
)

data class MonthPoint(
    val name:         String,
    val totalMinutes: Int,
    val sessions:     Int,
    val isCurrent:    Boolean,
)

data class DurationBuckets(
    val short:      Int   = 0,
    val medium:     Int   = 0,
    val long:       Int   = 0,
    val extended:   Int   = 0,
    val avgMinutes: Float = 0f,
) {
    val total get() = short + medium + long + extended
}

data class MoodStats(
    val total:         Int       = 0,
    val positivePct:   Int       = 0,
    val neutralPct:    Int       = 0,
    val negativePct:   Int       = 0,
    val positiveCount: Int       = 0,
    val neutralCount:  Int       = 0,
    val negativeCount: Int       = 0,
    val avgScore:      Float     = 0f,
    val distribution:  List<Int> = List(10) { 0 },
    val topScore:      Int       = 0,
    val topScoreCount: Int       = 0,
    val uniqueScores:  Int       = 0,
)

data class HeatCell(
    val label:     String,
    val minutes:   Int,
    val intensity: Float,
)

data class SleepTimelinePoint(
    val date: LocalDate,
    val durationMin: Int,
    val bedtimeLabel: String,
    val wakeLabel: String,
    val isEstimatedSchedule: Boolean,
)

data class SleepStats(
    val timeline: List<SleepTimelinePoint> = emptyList(),
    val avgSleep7dMin: Int? = null,
    val latestSleepMin: Int? = null,
    val avgHrv7d: Int? = null,
    val sleepHeartRate: Int? = null,
    val avgTemperatureC: Float? = null,
    val deepSleepMin: Int? = null,
    val remSleepMin: Int? = null,
    val awakeCount: Int? = null,
)

// ── UI State ───────────────────────────────────────────────────────────────────

data class StatsState(
    val isLoading:              Boolean           = true,
    val totalMeditationMinutes: Int               = 0,
    val bestStreakDays:          Int               = 0,
    val sessionsThisWeek:       Int               = 0,
    val totalSessions:          Int               = 0,
    val totalCycles:            Int               = 0,
    val currentStreak:          Int               = 0,
    val averageSessionDuration: Int               = 0,
    val todayMinutes:           Int               = 0,
    val avgCalmness:            Float             = 0f,
    val avgFocus:               Float             = 0f,
    // Sleep D/W/M views
    val sleepView:              SleepView         = SleepView.DAY,
    val selectedSleepDate:      LocalDate         = LocalDate.now(),
    val sleepDayView:           SleepDayView?     = null,
    val sleepWeekView:          SleepWeekView?    = null,
    val sleepMonthView:         SleepMonthView?   = null,
    val earliestSleepDate:      LocalDate?        = null,
    val latestSleepDate:        LocalDate?        = null,
    // Existing chart data
    val weeklyData:             List<DayMinutes>  = emptyList(),
    val monthlyData:            List<DayMinutes>  = emptyList(),
    val yearlyData:             List<DayMinutes>  = emptyList(),
    // New chart data
    val annualMonthData:        List<MonthPoint>  = emptyList(),
    val allYearsData:           List<MonthPoint>  = emptyList(),
    val durationBuckets:        DurationBuckets   = DurationBuckets(),
    val moodStats:              MoodStats         = MoodStats(),
    val consistencyPct:         Int               = 0,
    val sessionsPrev7d:         Int               = 0,
    val avgSessionMin7d:        Int               = 0,
    val avgSessionMinPrev7d:    Int               = 0,
    val weekdayAverages:        List<Float>       = List(7) { 0f },
    val heatmap28:              List<HeatCell>    = emptyList(),
    // Mood grid
    val moodPoints:             List<MoodPoint>   = emptyList(),
    // AI insights
    val insights:               List<InsightCard> = emptyList(),
    val nlpInsights:            NlpInsights?      = null,
    val health:                 HealthSummary?    = null,
    val sleepStats:             SleepStats        = SleepStats(),
    val sleepInsight:           String            = "",
    val sleepInsightFeedback:   Int?              = null,
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MeditationRepository,
    private val apiService: ApiService,
    private val sleepInsightFeedbackRepository: SleepInsightFeedbackRepository,
    private val integrationRepository: IntegrationRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private val _sessions = MutableStateFlow<List<MeditationSession>>(emptyList())
    val sessions: StateFlow<List<MeditationSession>> = _sessions.asStateFlow()

    private val sleepInsightKey = "daily_sleep_insight"

    // Caches to skip recomputation when input data hasn't changed
    private var lastLocalList: List<MeditationSession>? = null
    private var lastRemoteList: List<RemoteSession>? = null

    // Sleep aggregation caches
    private var allSleepCache: List<com.breatheonline.breathe.data.models.SleepDayDto> = emptyList()
    private var allHrCache: List<com.breatheonline.breathe.data.models.HrDayDto> = emptyList()

    init {
        observeLocal()
        observeSleepInsightFeedback()
        observeHealth()
        viewModelScope.launch { integrationRepository.refresh() }
        viewModelScope.launch { fetchRemote() }
        viewModelScope.launch { fetchNlp() }
    }

    private fun observeLocal() {
        viewModelScope.launch {
            repository.getAllSessions().collect { list ->
                _sessions.value = list
                if (list == lastLocalList) return@collect
                lastLocalList = list
                updateFromLocal(list)
            }
        }
    }

    private suspend fun fetchRemote() {
        runCatching { apiService.getSessions() }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body() ?: emptyList()
                    if (body != lastRemoteList) {
                        lastRemoteList = body
                        updateFromRemote(body)
                    }
                } else _state.update { it.copy(isLoading = false) }
            }
            .onFailure { _state.update { it.copy(isLoading = false) } }
    }

    fun refresh() = viewModelScope.launch { fetchRemote() }

    private fun observeSleepInsightFeedback() {
        viewModelScope.launch {
            sleepInsightFeedbackRepository.observeFeedback(sleepInsightKey).collect { feedback ->
                _state.update { it.copy(sleepInsightFeedback = feedback?.sentiment) }
            }
        }
    }

    private suspend fun fetchNlp() {
        runCatching { apiService.getNlpInsights() }
            .onSuccess { resp ->
                if (resp.isSuccessful) _state.update { it.copy(nlpInsights = resp.body()) }
            }
    }

    private fun observeHealth() {
        viewModelScope.launch {
            integrationRepository.integrations.collect { body -> processHealth(body) }
        }
    }

    private suspend fun processHealth(body: List<com.breatheonline.breathe.data.models.IntegrationStatusDto>) {
        val connected = body.filter { it.connected && it.data != null }
        if (connected.isEmpty()) return

        val allSleep = mergeSleepDays(connected.flatMap { it.data?.sleep ?: emptyList() }).takeLast(14)
        val allHrv   = connected.flatMap { it.data?.hrv ?: emptyList() }
            .distinctBy { it.date }
            .sortedBy { it.date }
            .takeLast(14)
        val allHr    = mergeHeartRateDays(connected.flatMap { it.data?.heartRate ?: emptyList() }).takeLast(14)

        allSleepCache = allSleep
        allHrCache = allHr
        val latestSleepDate = allSleep.lastOrNull()?.let { parseHealthDate(it.date) }
        if (latestSleepDate != null) {
            _state.update { state ->
                val hasSelected = allSleep.any { it.date == state.selectedSleepDate.toString() }
                state.copy(
                    selectedSleepDate = if (hasSelected) state.selectedSleepDate else latestSleepDate
                )
            }
        }
        recomputeSleepViews()

        val last7Sleep = allSleep.takeLast(7)
        val avgSleep   = if (last7Sleep.isEmpty()) null else last7Sleep.map { it.duration }.average().toInt()
        val avgHrv     = allHrv.takeLast(7).mapNotNull { it.rmssd }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        val restingHr  = allHr.lastOrNull { it.restingRate != null }?.restingRate
        val recovery   = avgHrv?.let { hrv -> ((hrv - 20f) / 80f * 100f).toInt().coerceIn(0, 100) }
        val sleepStats = buildSleepStats(allSleep, allHrv, allHr)
        val sleepInsight = buildSleepInsight(sleepStats)

        if (avgSleep == null && avgHrv == null && restingHr == null && sleepStats.timeline.isEmpty()) return
        _state.update {
            it.copy(
                health = HealthSummary(
                    avgSleep7dMin = avgSleep,
                    avgHrv7d      = avgHrv,
                    restingHr     = restingHr,
                    recoveryScore = recovery,
                    sources       = connected.map { conn -> conn.provider },
                ),
                sleepStats   = sleepStats,
                sleepInsight = sleepInsight,
            )
        }
    }

    fun setSleepView(view: SleepView) {
        _state.update { it.copy(sleepView = view) }
        recomputeSleepViews()
    }

    fun moveSleepSelection(step: Int) {
        val availableDates = allSleepCache.mapNotNull { parseHealthDate(it.date) }.sorted()
        if (availableDates.isEmpty() || step == 0) return

        val current = _state.value.selectedSleepDate
        val target = when (_state.value.sleepView) {
            SleepView.DAY -> moveToAdjacentDate(availableDates, current, step)
            SleepView.WEEK -> resolveAvailableDateFor(current.plusDays(step * 7L), availableDates)
            SleepView.MONTH -> resolveAvailableDateFor(current.plusMonths(step.toLong()), availableDates)
        } ?: return

        setSleepDate(target)
    }

    fun jumpToLatestSleep() {
        allSleepCache.lastOrNull()?.let { latest ->
            parseHealthDate(latest.date)?.let(::setSleepDate)
        }
    }

    fun setSleepDate(date: LocalDate) {
        _state.update { it.copy(selectedSleepDate = date) }
        recomputeSleepViews()
    }

    private fun recomputeSleepViews() {
        val days = allSleepCache
        if (days.isEmpty()) return
        val hrBpm: Int? = allHrCache.lastOrNull()?.restingRate

        val selected = _state.value.selectedSleepDate
        val selectedStr = selected.toString()
        val dayDto = days.firstOrNull { it.date == selectedStr } ?: days.last()
        val availableDates = days.mapNotNull { parseHealthDate(it.date) }.sorted()
        val history7d = days.takeLast(7)
        val dayView = com.breatheonline.breathe.utils.buildSleepDayView(dayDto, history7d, hrBpm)

        val weekDays = days.filter { com.breatheonline.breathe.utils.parseHealthDate(it.date)?.let { d -> d >= selected.minusDays(6) && d <= selected } == true }
        val prevWeekDays = days.filter { com.breatheonline.breathe.utils.parseHealthDate(it.date)?.let { d -> d >= selected.minusDays(13) && d < selected.minusDays(6) } == true }
        val rangeLabel = "${selected.minusDays(6)} – $selected"
        val weekView = com.breatheonline.breathe.utils.buildSleepWeekView(
            days = weekDays,
            prevWeekAvgDurationMin = prevWeekDays.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            prevWeekAvgScore = prevWeekDays.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            rangeLabel = rangeLabel,
        )

        val monthStart = selected.withDayOfMonth(1)
        val monthEnd = selected.withDayOfMonth(selected.lengthOfMonth())
        val monthDays = days.filter { com.breatheonline.breathe.utils.parseHealthDate(it.date)?.let { d -> d in monthStart..monthEnd } == true }
        val prevMonthStart = monthStart.minusMonths(1)
        val prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth())
        val prevMonthDays = days.filter { com.breatheonline.breathe.utils.parseHealthDate(it.date)?.let { d -> d in prevMonthStart..prevMonthEnd } == true }
        val monthLabel = selected.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"))
        val baselineScore = days.takeLast(90).mapNotNull { it.score }.takeIf { it.size >= 14 }?.average()?.toInt()
        val monthView = com.breatheonline.breathe.utils.buildSleepMonthView(
            days = monthDays,
            baselineScore = baselineScore,
            prevMonthAvgDurationMin = prevMonthDays.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            prevMonthAvgScore = prevMonthDays.mapNotNull { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            monthLabel = monthLabel,
        )

        _state.update {
            it.copy(
                sleepDayView = dayView,
                sleepWeekView = weekView,
                sleepMonthView = monthView,
                earliestSleepDate = availableDates.firstOrNull(),
                latestSleepDate = availableDates.lastOrNull(),
            )
        }
    }

    private fun moveToAdjacentDate(
        availableDates: List<LocalDate>,
        current: LocalDate,
        step: Int,
    ): LocalDate? {
        val currentIndex = availableDates.indexOfLast { !it.isAfter(current) }.takeIf { it >= 0 } ?: return null
        val targetIndex = (currentIndex + step).coerceIn(0, availableDates.lastIndex)
        return availableDates.getOrNull(targetIndex)
    }

    private fun resolveAvailableDateFor(target: LocalDate, availableDates: List<LocalDate>): LocalDate? {
        val sameMonth = availableDates.filter { it.year == target.year && it.month == target.month }
        if (sameMonth.isNotEmpty()) {
            return sameMonth.minByOrNull { kotlin.math.abs(it.dayOfMonth - target.dayOfMonth) }
        }
        return availableDates
            .filter { !it.isAfter(target) }
            .maxOrNull()
            ?: availableDates.minOrNull()
    }

    fun setSleepInsightFeedback(isPositive: Boolean) {
        val insightText = _state.value.sleepInsight.ifBlank { return }
        viewModelScope.launch {
            sleepInsightFeedbackRepository.upsert(
                SleepInsightFeedback(
                    insightKey = sleepInsightKey,
                    insightText = insightText,
                    sentiment = if (isPositive) 1 else -1,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
    }

    fun saveSession(duration: Long, date: Long, type: String = "deep") {
        viewModelScope.launch {
            repository.insertSession(
                MeditationSession(duration = duration.toInt(), date = date, type = type)
            )
        }
    }

    // ── Stats from local Room ──────────────────────────────────────────────────

    private suspend fun updateFromLocal(list: List<MeditationSession>) = withContext(Dispatchers.Default) {
        val today = LocalDate.now(ZoneId.systemDefault())
        val dates = list.map { epochToDate(it.date) }.toSet()
        val last7 = list.filter { epochToDate(it.date) >= today.minusDays(6) }
        val prev7 = list.filter {
            val d = epochToDate(it.date)
            d >= today.minusDays(13) && d < today.minusDays(6)
        }
        val activeDays7 = (0..6).count { n ->
            list.any { epochToDate(it.date) == today.minusDays(n.toLong()) }
        }
        var bs = 0; var bm = 0; var bl = 0; var be = 0
        var totalLocalMins = 0
        list.forEach { s ->
            val mins = s.duration / 60
            totalLocalMins += mins
            when { mins <= 5 -> bs++; mins <= 15 -> bm++; mins <= 30 -> bl++; else -> be++ }
        }
        val localAvgMin = if (list.isEmpty()) 0f else totalLocalMins.toFloat() / list.size

        _state.update {
            it.copy(
                isLoading              = false,
                totalMeditationMinutes = list.sumOf { s -> s.duration } / 60,
                totalSessions          = list.size,
                averageSessionDuration = if (list.isEmpty()) 0 else list.sumOf { s -> s.duration } / list.size,
                bestStreakDays         = SessionCalculations.computeLongestStreak(dates),
                currentStreak          = SessionCalculations.computeCurrentStreak(dates, today),
                sessionsThisWeek       = list.count { s -> epochToDate(s.date) >= today.with(DayOfWeek.MONDAY) },
                todayMinutes           = list.filter { s -> epochToDate(s.date) == today }.sumOf { s -> s.duration } / 60,
                weeklyData             = buildWeeklyLocal(list, today),
                monthlyData            = buildMonthlyLocal(list, today),
                yearlyData             = buildYearlyLocal(list, today),
                annualMonthData        = buildAnnualMonthLocal(list, today),
                allYearsData           = buildAllYearsDataLocal(list),
                moodPoints             = buildMoodPointsLocal(list),
                durationBuckets        = DurationBuckets(bs, bm, bl, be, localAvgMin),
                consistencyPct         = activeDays7 * 100 / 7,
                sessionsPrev7d         = prev7.size,
                avgSessionMin7d        = if (last7.isEmpty()) 0 else last7.sumOf { s -> s.duration } / last7.size / 60,
                avgSessionMinPrev7d    = if (prev7.isEmpty()) 0 else prev7.sumOf { s -> s.duration } / prev7.size / 60,
                weekdayAverages        = buildWeekdayAveragesLocal(list, today),
                heatmap28              = buildHeatmap28Local(list, today),
            )
        }
    }

    // ── Stats from API ─────────────────────────────────────────────────────────

    private suspend fun updateFromRemote(rawList: List<RemoteSession>) = withContext(Dispatchers.Default) {
        if (rawList.isEmpty()) { _state.update { it.copy(isLoading = false) }; return@withContext }

        val today = LocalDate.now(ZoneId.systemDefault())

        // Pre-parse every session's date once, using all available fields.
        // Sessions with no parseable date are dropped — they'd cluster on today otherwise.
        val datesById = buildMap<String, LocalDate> {
            rawList.forEach { s -> parseRemoteSessionDate(s)?.let { put(s.id, it) } }
        }
        val list = rawList.filter { it.id in datesById }
        if (list.isEmpty()) { _state.update { it.copy(isLoading = false) }; return@withContext }

        // All date lookups go through this map — no more remoteToDate(completedAt) in chart logic
        fun RemoteSession.d(): LocalDate = datesById.getValue(id)

        val startWeek   = today.with(DayOfWeek.MONDAY)
        val dates       = list.map { it.d() }.toSet()
        val totalMin    = list.sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) }
        val totalCyc    = list.sumOf { s -> s.cycles ?: 0 }
        val calmList    = list.mapNotNull { it.calmnessScore }
        val focusList   = list.mapNotNull { it.focusLevel }

        val last7 = list.filter { it.d() >= today.minusDays(6) }
        val prev7 = list.filter { val d = it.d(); d >= today.minusDays(13) && d < today.minusDays(6) }
        val activeDays7 = (0..6).count { n -> list.any { it.d() == today.minusDays(n.toLong()) } }

        val moodPoints = list.sortedByDescending { it.d() }.take(35).map { s ->
            MoodPoint(
                date  = s.d(),
                delta = if (s.moodBefore != null && s.moodAfter != null) s.moodAfter - s.moodBefore else null,
            )
        }

        _state.update {
            it.copy(
                isLoading              = false,
                totalMeditationMinutes = totalMin,
                totalSessions          = list.size,
                totalCycles            = totalCyc,
                averageSessionDuration = run {
                    val validSecs = list.mapNotNull { s ->
                        s.sessionLength?.let { minD -> (minD * 60).roundToInt() }
                    }
                    if (validSecs.isEmpty()) 0 else validSecs.sum() / validSecs.size
                },
                bestStreakDays         = SessionCalculations.computeLongestStreak(dates),
                currentStreak          = SessionCalculations.computeCurrentStreak(dates, today),
                sessionsThisWeek       = list.count { s -> s.d() >= startWeek },
                todayMinutes           = list.filter { s -> s.d() == today }
                    .sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) },
                avgCalmness            = if (calmList.isEmpty()) 0f else calmList.average().toFloat(),
                avgFocus               = if (focusList.isEmpty()) 0f else focusList.average().toFloat(),
                weeklyData             = buildWeeklyRemote(list, today, datesById),
                monthlyData            = buildMonthlyRemote(list, today, datesById),
                yearlyData             = buildYearlyRemote(list, today, datesById),
                annualMonthData        = buildAnnualMonthData(list, today, datesById),
                allYearsData           = buildAllYearsData(list, datesById),
                durationBuckets        = buildDurationBuckets(list),
                moodStats              = buildMoodStats(list),
                consistencyPct         = activeDays7 * 100 / 7,
                sessionsPrev7d         = prev7.size,
                avgSessionMin7d        = run {
                    val valid = last7.map { s -> s.sessionLength?.roundToInt() ?: (s.duration / 60) }.filter { it > 0 }
                    if (valid.isEmpty()) 0 else valid.sum() / valid.size
                },
                avgSessionMinPrev7d    = run {
                    val valid = prev7.map { s -> s.sessionLength?.roundToInt() ?: (s.duration / 60) }.filter { it > 0 }
                    if (valid.isEmpty()) 0 else valid.sum() / valid.size
                },
                weekdayAverages        = buildWeekdayAverages(list, datesById),
                heatmap28              = buildHeatmap28(list, today, datesById),
                moodPoints             = moodPoints,
                insights               = buildInsights(list),
            )
        }
    }

    // ── Chart builders — local ─────────────────────────────────────────────────

    private fun buildWeeklyLocal(list: List<MeditationSession>, today: LocalDate) =
        (6 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { epochToDate(it.date) == d }.sumOf { it.duration } / 60)
        }

    private fun buildMonthlyLocal(list: List<MeditationSession>, today: LocalDate) =
        (29 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { epochToDate(it.date) == d }.sumOf { it.duration } / 60)
        }

    private fun buildYearlyLocal(list: List<MeditationSession>, today: LocalDate) =
        (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            DayMinutes(m, list.filter {
                val d = epochToDate(it.date)
                d.year == m.year && d.monthValue == m.monthValue
            }.sumOf { it.duration } / 60)
        }

    private fun buildAnnualMonthLocal(list: List<MeditationSession>, today: LocalDate): List<MonthPoint> {
        val locale = Locale.getDefault()
        return (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            val s = list.filter {
                val d = epochToDate(it.date)
                d.year == m.year && d.monthValue == m.monthValue
            }
            MonthPoint(m.month.getDisplayName(java.time.format.TextStyle.SHORT, locale), s.sumOf { it.duration } / 60, s.size, n == 0)
        }
    }

    private fun buildWeekdayAveragesLocal(list: List<MeditationSession>, today: LocalDate): List<Float> {
        val totals = FloatArray(7); val counts = IntArray(7)
        list.groupBy { epochToDate(it.date) }.forEach { (date, sessions) ->
            val wd = date.dayOfWeek.value % 7
            val dayMins = sessions.sumOf { it.duration }.toFloat() / 60f
            if (dayMins > 0f) { totals[wd] += dayMins; counts[wd]++ }
        }
        return List(7) { i -> if (counts[i] > 0) totals[i] / counts[i] else 0f }
    }

    private fun buildHeatmap28Local(list: List<MeditationSession>, today: LocalDate): List<HeatCell> {
        val fmt = DateTimeFormatter.ofPattern("M/d")
        val daysSinceSunday = today.dayOfWeek.value % 7   // Sun=0 … Sat=6
        val start = today.minusDays((daysSinceSunday + 21).toLong())  // 4 full Sun-Sat weeks
        val dayMins = (0..27).associate { n ->
            val d = start.plusDays(n.toLong())
            d to list.filter { epochToDate(it.date) == d }.sumOf { it.duration } / 60
        }
        val maxMins = dayMins.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        return (0..27).map { n ->
            val d = start.plusDays(n.toLong())
            val mins = dayMins[d] ?: 0
            HeatCell(d.format(fmt), mins, mins.toFloat() / maxMins)
        }
    }

    // ── Chart builders — remote ────────────────────────────────────────────────

    private fun buildWeeklyRemote(list: List<RemoteSession>, today: LocalDate, dates: Map<String, LocalDate>) =
        (6 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { dates[it.id] == d }
                .sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) })
        }

    private fun buildMonthlyRemote(list: List<RemoteSession>, today: LocalDate, dates: Map<String, LocalDate>) =
        (29 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { dates[it.id] == d }
                .sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) })
        }

    private fun buildYearlyRemote(list: List<RemoteSession>, today: LocalDate, dates: Map<String, LocalDate>) =
        (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            DayMinutes(m, list.filter {
                val d = dates[it.id] ?: return@filter false
                d.year == m.year && d.monthValue == m.monthValue
            }.sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) })
        }

    private fun buildAnnualMonthData(list: List<RemoteSession>, today: LocalDate, dates: Map<String, LocalDate>): List<MonthPoint> {
        val locale = Locale.getDefault()
        return (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            val s = list.filter {
                val d = dates[it.id] ?: return@filter false
                d.year == m.year && d.monthValue == m.monthValue
            }
            MonthPoint(m.month.getDisplayName(java.time.format.TextStyle.SHORT, locale), s.sumOf { (it.sessionLength?.roundToInt() ?: (it.duration / 60)) }, s.size, n == 0)
        }
    }

    private fun buildAllYearsData(list: List<RemoteSession>, dates: Map<String, LocalDate>): List<MonthPoint> {
        val curYear = LocalDate.now(ZoneId.systemDefault()).year
        return list.groupBy { dates[it.id]?.year ?: return@groupBy -1 }
            .filter { it.key != -1 }
            .entries.sortedBy { it.key }
            .map { (year, s) ->
                MonthPoint(year.toString(), s.sumOf { (it.sessionLength?.roundToInt() ?: (it.duration / 60)) }, s.size, year == curYear)
            }
    }

    private fun buildDurationBuckets(list: List<RemoteSession>): DurationBuckets {
        var s = 0; var m = 0; var l = 0; var e = 0
        var totalMins = 0f
        list.forEach { sess ->
            val mins = sess.sessionLength?.toFloat() ?: (sess.duration / 60f)
            totalMins += mins
            when {
                mins <= 5f  -> s++
                mins <= 15f -> m++
                mins <= 30f -> l++
                else        -> e++
            }
        }
        val avg = if (list.isEmpty()) 0f else totalMins / list.size
        return DurationBuckets(s, m, l, e, avg)
    }

    private fun buildMoodStats(list: List<RemoteSession>): MoodStats {
        val vals = list.mapNotNull { it.moodAfter ?: it.moodBefore }.filter { it in 1..10 }
        val total = vals.size
        if (total == 0) return MoodStats()
        val pos  = vals.count { it >= 8 }
        val neu  = vals.count { it in 4..7 }
        val neg  = vals.count { it < 4 }
        val pct  = { n: Int -> n * 100 / total }
        val dist = List(10) { i -> vals.count { it == i + 1 } }
        val topIdx = dist.indices.maxByOrNull { dist[it] } ?: 0
        return MoodStats(
            total = total,
            positivePct = pct(pos), neutralPct = pct(neu), negativePct = pct(neg),
            positiveCount = pos, neutralCount = neu, negativeCount = neg,
            avgScore = vals.average().toFloat(), distribution = dist,
            topScore = topIdx + 1, topScoreCount = dist[topIdx],
            uniqueScores = dist.count { it > 0 },
        )
    }

    private fun buildWeekdayAverages(list: List<RemoteSession>, dates: Map<String, LocalDate>): List<Float> {
        val totals = FloatArray(7); val counts = IntArray(7)
        // list is already filtered to sessions in dates, so dates[id] is always non-null here
        list.groupBy { dates[it.id] }.forEach { (date, sessions) ->
            date ?: return@forEach
            val wd = date.dayOfWeek.value % 7
            val dayMins = sessions.sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) }.toFloat()
            if (dayMins > 0f) { totals[wd] += dayMins; counts[wd]++ }
        }
        return List(7) { i -> if (counts[i] > 0) totals[i] / counts[i] else 0f }
    }

    private fun buildHeatmap28(list: List<RemoteSession>, today: LocalDate, dates: Map<String, LocalDate>): List<HeatCell> {
        val fmt = DateTimeFormatter.ofPattern("M/d")
        val daysSinceSunday = today.dayOfWeek.value % 7   // Sun=0 … Sat=6
        val start = today.minusDays((daysSinceSunday + 21).toLong())  // 4 full Sun-Sat weeks
        val dayMins = (0..27).associate { n ->
            val d = start.plusDays(n.toLong())
            d to list.filter { dates[it.id] == d }
                .sumOf { s -> (s.sessionLength?.roundToInt() ?: (s.duration / 60)) }
        }
        val maxMins = dayMins.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        return (0..27).map { n ->
            val d = start.plusDays(n.toLong())
            val mins = dayMins[d] ?: 0
            HeatCell(d.format(fmt), mins, mins.toFloat() / maxMins)
        }
    }

    // ── AI Insights ────────────────────────────────────────────────────────────

    private fun buildInsights(list: List<RemoteSession>): List<InsightCard> {
        if (list.isEmpty()) return emptyList()
        val cards = mutableListOf<InsightCard>()

        list.groupingBy { it.type ?: "unknown" }.eachCount()
            .maxByOrNull { it.value }?.let { (type, count) ->
                cards += InsightCard("🌟", "Favourite technique",
                    "${typeLabel(type)} is your go-to — you've done it $count time(s).")
            }

        list.mapNotNull { it.timeOfDay?.takeIf { t -> t.isNotBlank() } }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }?.let { (time, _) ->
                cards += InsightCard(timeEmoji(time), "Best time of day",
                    "You practise most in the $time. Keep that rhythm!")
            }

        val moodDeltas = list.mapNotNull { s ->
            val before = s.moodBefore
            val after  = s.moodAfter
            if (before != null && after != null) after - before else null
        }
        if (moodDeltas.isNotEmpty()) {
            val avg = moodDeltas.average()
            cards += InsightCard(if (avg >= 0) "😌" else "📉", "Mood lift",
                "Average mood change per session: ${if (avg >= 0) "+" else ""}${"%.1f".format(avg)} pts.")
        }

        val sorted = list.sortedBy { it.effectiveDate }; val half = sorted.size / 2
        val firstCalm  = sorted.take(half).mapNotNull { it.calmnessScore }
        val secondCalm = sorted.drop(half).mapNotNull { it.calmnessScore }
        if (firstCalm.isNotEmpty() && secondCalm.isNotEmpty()) {
            val diff = secondCalm.average() - firstCalm.average()
            if (diff > 0.3) cards += InsightCard("📈", "Calmness improving",
                "Your calmness score has grown +${"%.1f".format(diff)} pts over time!")
        }
        return cards
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun buildSleepStats(
        sleepDays: List<SleepDayDto>,
        hrvDays: List<HrvDayDto>,
        hrDays: List<HrDayDto>,
    ): SleepStats {
        val timeline = sleepDays.takeLast(7).mapNotNull { day ->
            val date = parseApiDate(day.date) ?: return@mapNotNull null
            SleepTimelinePoint(
                date = date,
                durationMin = day.duration,
                bedtimeLabel = "",
                wakeLabel = "",
                isEstimatedSchedule = false,
            )
        }
        val avgSleep7d   = sleepDays.takeLast(7).map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        val avgHrv7d     = hrvDays.takeLast(7).mapNotNull { it.rmssd }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        val hr           = hrDays.lastOrNull()?.restingRate ?: hrDays.lastOrNull()?.avgRate
        val lastNight    = sleepDays.lastOrNull()
        // Use the most recent night's stage data; fall back to 7-day sum if stages present
        val deepSleepMin  = lastNight?.deepSleepMin
            ?: sleepDays.takeLast(7).mapNotNull { it.deepSleepMin }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        val remSleepMin   = lastNight?.remSleepMin
            ?: sleepDays.takeLast(7).mapNotNull { it.remSleepMin }.takeIf { it.isNotEmpty() }?.average()?.toInt()
        val awakeMin      = lastNight?.awakeMin?.takeIf { it > 0 }
        return SleepStats(
            timeline        = timeline,
            avgSleep7dMin   = avgSleep7d,
            latestSleepMin  = lastNight?.duration,
            avgHrv7d        = avgHrv7d,
            sleepHeartRate  = hr,
            avgTemperatureC = null,
            deepSleepMin    = deepSleepMin,
            remSleepMin     = remSleepMin,
            awakeCount      = awakeMin,
        )
    }

    private fun buildSleepInsight(stats: SleepStats): String {
        val avgSleep = stats.avgSleep7dMin
        val latest = stats.latestSleepMin
        return when {
            avgSleep == null && latest == null ->
                "Daily sleep insight: connect sleep data to start building your nightly baseline."
            avgSleep != null && avgSleep >= 480 ->
                "Daily sleep insight: your recent sleep is holding near ${formatSleepDuration(avgSleep)}. Keep bedtime consistent."
            avgSleep != null ->
                "Daily sleep insight: you're averaging ${formatSleepDuration(avgSleep)}. Aim for a slightly earlier wind-down tonight."
            else ->
                "Daily sleep insight: last night landed at ${formatSleepDuration(latest!!)}. Try to repeat the same sleep window."
        }
    }

    private fun typeLabel(type: String): String = when {
        type.startsWith("custom_") -> context.getString(com.breatheonline.breathe.R.string.history_session_custom)
        else -> when (type) {
            "4-7-8"     -> context.getString(com.breatheonline.breathe.R.string.history_session_4_7_8)
            "box"       -> context.getString(com.breatheonline.breathe.R.string.history_session_box)
            "wimhof"    -> context.getString(com.breatheonline.breathe.R.string.history_session_wim_hof)
            "coherent"  -> context.getString(com.breatheonline.breathe.R.string.history_session_coherent)
            "belly"     -> context.getString(com.breatheonline.breathe.R.string.history_session_belly)
            "morning"   -> context.getString(com.breatheonline.breathe.R.string.history_session_morning)
            "alternate" -> context.getString(com.breatheonline.breathe.R.string.history_session_alternate)
            "deep"      -> context.getString(com.breatheonline.breathe.R.string.history_session_deep)
            "energy"    -> context.getString(com.breatheonline.breathe.R.string.history_session_energising)
            else        -> type.replaceFirstChar { it.uppercase() }
        }
    }

    private fun buildAllYearsDataLocal(list: List<MeditationSession>): List<MonthPoint> {
        val curYear = LocalDate.now(ZoneId.systemDefault()).year
        return list.groupBy { epochToDate(it.date).year }.entries.sortedBy { it.key }
            .map { (year, sessions) ->
                MonthPoint(year.toString(), sessions.sumOf { it.duration } / 60, sessions.size, year == curYear)
            }
    }

    private fun buildMoodPointsLocal(list: List<MeditationSession>): List<MoodPoint> =
        list.sortedByDescending { it.date }.take(35).map { s ->
            MoodPoint(
                date  = epochToDate(s.date),
                delta = if (s.moodBefore != null && s.moodAfter != null) s.moodAfter - s.moodBefore else null,
            )
        }

    private fun timeEmoji(time: String) = when (time.lowercase()) {
        "morning" -> "🌅"; "afternoon" -> "☀️"; "evening" -> "🌆"; "night" -> "🌙"; else -> "⏰"
    }

    private fun epochToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun parseRemoteSessionDate(s: RemoteSession): LocalDate? =
        // Prefer explicit date-only field; it's unambiguous and tz-free
        s.sessionDate?.let { sd ->
            runCatching { LocalDate.parse(sd) }.getOrNull()
                ?: runCatching { Instant.parse(sd).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
                ?: runCatching { java.time.OffsetDateTime.parse(sd).toLocalDate() }.getOrNull()
        } ?: s.completedAt?.let { parseCompletedAt(it) }

    private fun parseCompletedAt(raw: String): LocalDate? =
        runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            ?: runCatching { java.time.OffsetDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { java.time.LocalDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(raw) }.getOrNull()

    private fun remoteToDate(completedAt: String): LocalDate =
        parseCompletedAt(completedAt) ?: LocalDate.now()

    private fun parseApiDate(raw: String): LocalDate? = parseHealthDate(raw)

    fun formatSleepDuration(min: Int): String {
        val h = min / 60
        val m = min % 60
        return if (m == 0) "${h}h" else "${h}h ${m}m"
    }

    fun formatMinutesToClock(min: Int): String {
        val h = min / 60; val m = min % 60
        return "%02d:%02d".format(h, m)
    }
}
