package com.example.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.MeditationSession
import com.example.breathe.data.models.NlpInsights
import com.example.breathe.data.models.RemoteSession
import com.example.breathe.data.repository.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
)

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MeditationRepository,
    private val apiService: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private val _sessions = MutableStateFlow<List<MeditationSession>>(emptyList())
    val sessions: StateFlow<List<MeditationSession>> = _sessions.asStateFlow()

    init {
        observeLocal()
        viewModelScope.launch { fetchRemote() }
        viewModelScope.launch { fetchNlp() }
        viewModelScope.launch { fetchHealth() }
    }

    private fun observeLocal() {
        viewModelScope.launch {
            repository.getAllSessions().collect { list ->
                _sessions.value = list
                updateFromLocal(list)
            }
        }
    }

    private suspend fun fetchRemote() {
        runCatching { apiService.getSessions() }
            .onSuccess { resp ->
                if (resp.isSuccessful) updateFromRemote(resp.body() ?: emptyList())
                else _state.update { it.copy(isLoading = false) }
            }
            .onFailure { _state.update { it.copy(isLoading = false) } }
    }

    fun refresh() = viewModelScope.launch { fetchRemote() }

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

                val allSleep = connected.flatMap { it.data!!.sleep ?: emptyList() }
                    .sortedByDescending { it.date }.take(7)
                val allHrv   = connected.flatMap { it.data!!.hrv ?: emptyList() }
                    .sortedByDescending { it.date }.take(7)
                val allHr    = connected.flatMap { it.data!!.heartRate ?: emptyList() }
                    .sortedByDescending { it.date }

                val avgSleep  = if (allSleep.isEmpty()) null else allSleep.map { it.duration }.average().toInt()
                val avgHrv    = if (allHrv.isEmpty())  null else allHrv.mapNotNull { it.rmssd }.average().toInt()
                val restingHr = allHr.firstOrNull { it.restingRate != null }?.restingRate
                val recovery  = avgHrv?.let { hrv -> ((hrv - 20f) / 80f * 100f).toInt().coerceIn(0, 100) }

                if (avgSleep == null && avgHrv == null && restingHr == null) return@onSuccess
                _state.update {
                    it.copy(health = HealthSummary(
                        avgSleep7dMin = avgSleep,
                        avgHrv7d      = avgHrv,
                        restingHr     = restingHr,
                        recoveryScore = recovery,
                        sources       = connected.map { conn -> conn.provider },
                    ))
                }
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

    private fun updateFromLocal(list: List<MeditationSession>) {
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
                bestStreakDays         = computeBestStreak(dates),
                currentStreak          = computeCurrentStreak(dates, today),
                sessionsThisWeek       = list.count { s -> epochToDate(s.date) >= today.with(DayOfWeek.MONDAY) },
                todayMinutes           = list.filter { s -> epochToDate(s.date) == today }.sumOf { s -> s.duration } / 60,
                weeklyData             = buildWeeklyLocal(list, today),
                monthlyData            = buildMonthlyLocal(list, today),
                yearlyData             = buildYearlyLocal(list, today),
                annualMonthData        = buildAnnualMonthLocal(list, today),
                durationBuckets        = DurationBuckets(bs, bm, bl, be),
                consistencyPct         = activeDays7 * 100 / 7,
                sessionsPrev7d         = prev7.size,
                avgSessionMin7d        = if (last7.isEmpty()) 0 else last7.sumOf { it.duration } / last7.size / 60,
                avgSessionMinPrev7d    = if (prev7.isEmpty()) 0 else prev7.sumOf { it.duration } / prev7.size / 60,
                weekdayAverages        = buildWeekdayAveragesLocal(list, today),
                heatmap28              = buildHeatmap28Local(list, today),
            )
        }
    }

    // ── Stats from API ─────────────────────────────────────────────────────────

    private fun updateFromRemote(list: List<RemoteSession>) {
        if (list.isEmpty()) { _state.update { it.copy(isLoading = false) }; return }

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
                bestStreakDays         = computeBestStreak(dates),
                currentStreak          = computeCurrentStreak(dates, today),
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
                avgSessionMin7d        = if (last7.isEmpty()) 0
                    else last7.sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) } / last7.size,
                avgSessionMinPrev7d    = if (prev7.isEmpty()) 0
                    else prev7.sumOf { s -> (s.sessionLength?.toInt() ?: (s.duration / 60)) } / prev7.size,
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

    private fun computeCurrentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        val start = when {
            dates.contains(today)              -> today
            dates.contains(today.minusDays(1)) -> today.minusDays(1)
            else                               -> return 0
        }
        var n = 0; var day = start
        while (dates.contains(day)) { n++; day = day.minusDays(1) }
        return n
    }

    private fun computeBestStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var best = 0; var cur = 0; var prev: LocalDate? = null
        for (d in dates.sorted()) {
            cur = if (prev == null || d == prev!!.plusDays(1)) cur + 1 else 1
            if (cur > best) best = cur
            prev = d
        }
        return best
    }

    private fun epochToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun remoteToDate(completedAt: String): LocalDate = try {
        Instant.parse(completedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (_: Exception) { LocalDate.now() }

    fun formatMinutesToClock(min: Int): String {
        val h = min / 60; val m = min % 60
        return "%02d:%02d".format(h, m)
    }
}
