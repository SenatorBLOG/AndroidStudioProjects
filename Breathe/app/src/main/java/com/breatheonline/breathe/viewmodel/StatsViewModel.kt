package com.breatheonline.breathe.viewmodel

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
import com.breatheonline.breathe.data.repository.MeditationRepository
import com.breatheonline.breathe.data.repository.SleepInsightFeedbackRepository
import com.breatheonline.breathe.utils.mergeHeartRateDays
import com.breatheonline.breathe.utils.mergeSleepDays
import com.breatheonline.breathe.utils.parseHealthDate
import com.breatheonline.breathe.utils.SessionCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

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
    val short:    Int = 0,
    val medium:   Int = 0,
    val long:     Int = 0,
    val extended: Int = 0,
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
        viewModelScope.launch { fetchRemote() }
        viewModelScope.launch { fetchNlp() }
        viewModelScope.launch { fetchHealth() }
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

    private suspend fun fetchHealth() {
        runCatching { apiService.getIntegrationStatus() }
            .onSuccess { resp ->
                if (!resp.isSuccessful) return@onSuccess
                val connected = resp.body()?.filter { it.connected && it.data != null }
                if (connected.isNullOrEmpty()) return@onSuccess

                val allSleep = mergeSleepDays(connected.flatMap { it.data!!.sleep ?: emptyList() }).takeLast(14)
                val allHrv   = connected.flatMap { it.data!!.hrv ?: emptyList() }
                    .distinctBy { it.date }
                    .sortedBy { it.date }
                    .takeLast(14)
                val allHr    = mergeHeartRateDays(connected.flatMap { it.data!!.heartRate ?: emptyList() }).takeLast(14)

                allSleepCache = allSleep
                allHrCache = allHr
                recomputeSleepViews()

                val last7Sleep = allSleep.takeLast(7)
                val avgSleep  = if (last7Sleep.isEmpty()) null else last7Sleep.map { it.duration }.average().toInt()
                val avgHrv    = allHrv.takeLast(7).mapNotNull { it.rmssd }.takeIf { it.isNotEmpty() }?.average()?.toInt()
                val restingHr = allHr.lastOrNull { it.restingRate != null }?.restingRate
                val recovery  = avgHrv?.let { hrv -> ((hrv - 20f) / 80f * 100f).toInt().coerceIn(0, 100) }
                val sleepStats = buildSleepStats(allSleep, allHrv, allHr)
                val sleepInsight = buildSleepInsight(sleepStats)

                if (avgSleep == null && avgHrv == null && restingHr == null && sleepStats.timeline.isEmpty()) return@onSuccess
                _state.update {
                    it.copy(
                        health = HealthSummary(
                            avgSleep7dMin = avgSleep,
                            avgHrv7d      = avgHrv,
                            restingHr     = restingHr,
                            recoveryScore = recovery,
                            sources       = connected.map { conn -> conn.provider },
                        ),
                        sleepStats = sleepStats,
                        sleepInsight = sleepInsight,
                    )
                }
            }
    }

    fun setSleepView(view: SleepView) {
        _state.update { it.copy(sleepView = view) }
        recomputeSleepViews()
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
            it.copy(sleepDayView = dayView, sleepWeekView = weekView, sleepMonthView = monthView)
        }
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
        list.forEach { s ->
            val mins = s.duration / 60
            when { mins <= 5 -> bs++; mins <= 15 -> bm++; mins <= 30 -> bl++; else -> be++ }
        }

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
                durationBuckets        = DurationBuckets(bs, bm, bl, be),
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

    private suspend fun updateFromRemote(list: List<RemoteSession>) = withContext(Dispatchers.Default) {
        if (list.isEmpty()) { _state.update { it.copy(isLoading = false) }; return@withContext }

        val today     = LocalDate.now(ZoneId.systemDefault())
        val startWeek = today.with(DayOfWeek.MONDAY)
        val dates     = list.map { remoteToDate(it.completedAt) }.toSet()
        val totalMin  = list.sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) }
        val totalCyc  = list.sumOf { s -> s.cycles ?: 0 }
        val calmList  = list.mapNotNull { it.calmnessScore }
        val focusList = list.mapNotNull { it.focusLevel }

        val last7 = list.filter { remoteToDate(it.completedAt) >= today.minusDays(6) }
        val prev7 = list.filter {
            val d = remoteToDate(it.completedAt)
            d >= today.minusDays(13) && d < today.minusDays(6)
        }
        val activeDays7 = (0..6).count { n ->
            list.any { remoteToDate(it.completedAt) == today.minusDays(n.toLong()) }
        }

        val moodPoints = list.sortedByDescending { it.completedAt }.take(35).map { s ->
            MoodPoint(
                date  = remoteToDate(s.completedAt),
                delta = if (s.moodBefore != null && s.moodAfter != null) s.moodAfter - s.moodBefore else null,
            )
        }

        _state.update {
            it.copy(
                isLoading              = false,
                totalMeditationMinutes = totalMin,
                totalSessions          = list.size,
                totalCycles            = totalCyc,
                bestStreakDays         = SessionCalculations.computeLongestStreak(dates),
                currentStreak          = SessionCalculations.computeCurrentStreak(dates, today),
                sessionsThisWeek       = list.count { s -> remoteToDate(s.completedAt) >= startWeek },
                todayMinutes           = list.filter { s -> remoteToDate(s.completedAt) == today }
                    .sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) },
                avgCalmness            = if (calmList.isEmpty()) 0f else calmList.average().toFloat(),
                avgFocus               = if (focusList.isEmpty()) 0f else focusList.average().toFloat(),
                weeklyData             = buildWeeklyRemote(list, today),
                monthlyData            = buildMonthlyRemote(list, today),
                yearlyData             = buildYearlyRemote(list, today),
                annualMonthData        = buildAnnualMonthData(list, today),
                allYearsData           = buildAllYearsData(list),
                durationBuckets        = buildDurationBuckets(list),
                moodStats              = buildMoodStats(list),
                consistencyPct         = activeDays7 * 100 / 7,
                sessionsPrev7d         = prev7.size,
                avgSessionMin7d        = run {
                    val valid = last7.map { s -> s.sessionLength?.toInt() ?: (s.duration / 60) }.filter { it > 0 }
                    if (valid.isEmpty()) 0 else valid.sum() / valid.size
                },
                avgSessionMinPrev7d    = run {
                    val valid = prev7.map { s -> s.sessionLength?.toInt() ?: (s.duration / 60) }.filter { it > 0 }
                    if (valid.isEmpty()) 0 else valid.sum() / valid.size
                },
                weekdayAverages        = buildWeekdayAverages(list, today),
                heatmap28              = buildHeatmap28(list, today),
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
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            val s = list.filter {
                val d = epochToDate(it.date)
                d.year == m.year && d.monthValue == m.monthValue
            }
            MonthPoint(months[m.monthValue - 1], s.sumOf { it.duration } / 60, s.size, n == 0)
        }
    }

    private fun buildWeekdayAveragesLocal(list: List<MeditationSession>, today: LocalDate): List<Float> {
        val totals = FloatArray(7); val counts = IntArray(7)
        (0..27).forEach { n ->
            val d = today.minusDays(n.toLong())
            val wd = d.dayOfWeek.value % 7
            val mins = list.filter { epochToDate(it.date) == d }.sumOf { it.duration }.toFloat() / 60f
            if (mins > 0f) { totals[wd] += mins; counts[wd]++ }
        }
        return List(7) { i -> if (counts[i] > 0) totals[i] / counts[i] else 0f }
    }

    private fun buildHeatmap28Local(list: List<MeditationSession>, today: LocalDate): List<HeatCell> {
        val fmt = DateTimeFormatter.ofPattern("M/d")
        val dayMins = (0..27).associate { n ->
            val d = today.minusDays(n.toLong())
            d to list.filter { epochToDate(it.date) == d }.sumOf { it.duration } / 60
        }
        val maxMins = dayMins.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        return (27 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            val mins = dayMins[d] ?: 0
            HeatCell(d.format(fmt), mins, mins.toFloat() / maxMins)
        }
    }

    // ── Chart builders — remote ────────────────────────────────────────────────

    private fun buildWeeklyRemote(list: List<RemoteSession>, today: LocalDate) =
        (6 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { remoteToDate(it.completedAt) == d }
                .sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) })
        }

    private fun buildMonthlyRemote(list: List<RemoteSession>, today: LocalDate) =
        (29 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
            DayMinutes(d, list.filter { remoteToDate(it.completedAt) == d }
                .sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) })
        }

    private fun buildYearlyRemote(list: List<RemoteSession>, today: LocalDate) =
        (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            DayMinutes(m, list.filter {
                val d = remoteToDate(it.completedAt)
                d.year == m.year && d.monthValue == m.monthValue
            }.sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) })
        }

    private fun buildAnnualMonthData(list: List<RemoteSession>, today: LocalDate): List<MonthPoint> {
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        return (11 downTo 0).map { n ->
            val m = today.minusMonths(n.toLong()).withDayOfMonth(1)
            val s = list.filter {
                val d = remoteToDate(it.completedAt)
                d.year == m.year && d.monthValue == m.monthValue
            }
            MonthPoint(months[m.monthValue - 1], s.sumOf { it -> (it.sessionLength?.toInt() ?: (it.duration / 60)) }, s.size, n == 0)
        }
    }

    private fun buildAllYearsData(list: List<RemoteSession>): List<MonthPoint> {
        val curYear = LocalDate.now(ZoneId.systemDefault()).year
        return list.groupBy { remoteToDate(it.completedAt).year }.entries.sortedBy { it.key }
            .map { (year, s) ->
                MonthPoint(year.toString(), s.sumOf { (it.sessionLength?.toInt() ?: (it.duration / 60)) }, s.size, year == curYear)
            }
    }

    private fun buildDurationBuckets(list: List<RemoteSession>): DurationBuckets {
        var s = 0; var m = 0; var l = 0; var e = 0
        list.forEach { sess ->
            val mins = sess.sessionLength?.toInt() ?: (sess.duration / 60)
            when { mins <= 5 -> s++; mins <= 15 -> m++; mins <= 30 -> l++; else -> e++ }
        }
        return DurationBuckets(s, m, l, e)
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

    private fun buildWeekdayAverages(list: List<RemoteSession>, today: LocalDate): List<Float> {
        val totals = FloatArray(7); val counts = IntArray(7)
        (0..27).forEach { n ->
            val d  = today.minusDays(n.toLong())
            val wd = d.dayOfWeek.value % 7
            val mins = list.filter { remoteToDate(it.completedAt) == d }
                .sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) }.toFloat()
            if (mins > 0f) { totals[wd] += mins; counts[wd]++ }
        }
        return List(7) { i -> if (counts[i] > 0) totals[i] / counts[i] else 0f }
    }

    private fun buildHeatmap28(list: List<RemoteSession>, today: LocalDate): List<HeatCell> {
        val fmt = DateTimeFormatter.ofPattern("M/d")
        val dayMins = (0..27).associate { n ->
            val d = today.minusDays(n.toLong())
            d to list.filter { remoteToDate(it.completedAt) == d }
                .sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) }
        }
        val maxMins = dayMins.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        return (27 downTo 0).map { n ->
            val d = today.minusDays(n.toLong())
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

        val moodPairs = list.filter { it.moodBefore != null && it.moodAfter != null }
        if (moodPairs.isNotEmpty()) {
            val avg = moodPairs.map { it.moodAfter!! - it.moodBefore!! }.average()
            cards += InsightCard(if (avg >= 0) "😌" else "📉", "Mood lift",
                "Average mood change per session: ${if (avg >= 0) "+" else ""}${"%.1f".format(avg)} pts.")
        }

        val sorted = list.sortedBy { it.completedAt }; val half = sorted.size / 2
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

    private fun typeLabel(type: String) = when {
        type.startsWith("custom_") -> "Custom Breathing"
        else -> when (type) {
            "4-7-8"     -> "4-7-8 Breathing"
            "box"       -> "Box Breathing"
            "wimhof"    -> "Wim Hof Method"
            "coherent"  -> "Coherent Breathing"
            "belly"     -> "Belly Breathing"
            "morning"   -> "Morning Ritual"
            "alternate" -> "Alternate Nostril"
            "deep"      -> "Deep Relaxation"
            "energy"    -> "Energising Breath"
            else        -> type.replaceFirstChar { it.uppercase() }
        }
    }

    private fun timeEmoji(time: String) = when (time.lowercase()) {
        "morning" -> "🌅"; "afternoon" -> "☀️"; "evening" -> "🌆"; "night" -> "🌙"; else -> "⏰"
    }

    private fun epochToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun remoteToDate(completedAt: String): LocalDate = try {
        Instant.parse(completedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (_: Exception) { LocalDate.now() }

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
