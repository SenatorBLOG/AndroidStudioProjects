# Sleep Statistics Redesign — Design Spec

**Date:** 2026-04-16
**Scope:** Breathe Android app + Breathe API (minor)
**Author:** Brainstormed with Claude Code

## 1. Goal

Replace the existing "Sleep statistics" tab inside `StatsScreen` with a full, richly
visualised sleep experience inspired by Xiaomi Mi Health (see reference images
in `Downloads/images example app/`). The meditation half of `StatsScreen` is
left behind unchanged structurally — we only relocate its code into smaller
files as part of splitting the 1879-line monolith.

Priority of v1: **Day / Week / Month tabs for Sleep**, plus local interpretation
with clickable prompt-chips that open the existing AI coach bottom sheet.

## 2. Architecture

### 2.1 File layout

Target package: `com.breatheonline.breathe.ui.screens.stats`.

```
ui/screens/stats/
├── StatsScreen.kt                    root: top-tabs Meditation/Sleep + routing
├── common/
│   ├── StatsTopTabs.kt               top-level Meditation/Sleep selector
│   ├── PeriodTabs.kt                 D/W/M and Week/Month/Year bar
│   └── ChartAxis.kt                  grid-lines, dashed baselines, labels
├── meditation/
│   ├── MeditationStatsContent.kt     period routing + section ordering
│   ├── MeditationCharts.kt           MinutesBarChart, MoodLineChart
│   ├── MeditationBreakdown.kt        DurationBucketGrid, InsightCard
│   └── MeditationHero.kt             hero card
└── sleep/
    ├── SleepStatsContent.kt          D/W/M routing, hero always visible
    ├── SleepDayContent.kt            Day view
    ├── SleepWeekContent.kt           Week view
    ├── SleepMonthContent.kt          Month view
    ├── SleepHeroCard.kt              hero (duration, quality, delta)
    ├── SleepHypnogram.kt             Deep/Light/REM waves chart (Day)
    ├── SleepStagesDonut.kt           ring chart (Day)
    ├── SleepStagesBreakdown.kt       % + duration rows (Day)
    ├── SleepScoreChart.kt            bar chart w/ baseline (W/M)
    ├── SleepScheduleSection.kt       fall-asleep/wake lines + regularity
    ├── IdealSleepDurationChart.kt    stacked bar chart (W/M)
    ├── SleepInsightBlock.kt          local interpretation + chips
    └── SleepPromptChips.kt           chip row → opens AiCoachBottomSheet
```

The old `ui/screens/StatsScreen.kt` becomes a **thin re-export wrapper** so
`NavGraph.kt` doesn't need to change:

```kotlin
@Composable
fun StatsScreen(colors: AppColors, navController: NavController, modifier: Modifier) =
    com.breatheonline.breathe.ui.screens.stats.StatsScreen(colors, navController, modifier)
```

### 2.2 ViewModel

`StatsViewModel` stays as the single source of truth (no feature-split). We
**extend** it:

- Add `SleepView` enum: `DAY`, `WEEK`, `MONTH`.
- Add `selectedSleepDate: LocalDate` (defaults to today). Changes via a date
  picker in the tab header.
- Add new derived state shapes (see §3.3).

### 2.3 Non-goals

- No Gradle feature modules.
- No refactor of meditation logic (only file-move).
- No new AI-cost paths — Gemini is only hit when the user explicitly taps a
  prompt-chip.

## 3. Data model

### 3.1 Android DTOs — extend `SleepDayDto`

File: `data/models/ApiModels.kt`.

```kotlin
data class SleepStageSegment(
    val startMin: Int,   // minutes from session start
    val endMin:   Int,
    val stage:    SleepStage,
)

enum class SleepStage { DEEP, LIGHT, REM, AWAKE }

data class SleepDayDto(
    val date:          String,
    val duration:      Int,
    val deepSleepMin:  Int? = null,
    val remSleepMin:   Int? = null,
    val lightSleepMin: Int? = null,
    val awakeMin:      Int? = null,
    // NEW — nullable so legacy payloads still parse
    val bedtime:       String? = null,           // ISO timestamp
    val wakeTime:      String? = null,           // ISO timestamp
    val stages:        List<SleepStageSegment>? = null,
    val score:         Int? = null,              // 0–100; client can also derive
)
```

Rules:
- `stages` is null on older data → Day-view falls back to proportional
  stacked-bar (Deep/Light/REM/Awake minutes split across night width).
- `score` null → computed client-side from stages + duration + awake events
  using a deterministic rule (§3.4).

### 3.2 Health Connect ingest

`HealthConnectImporter` (find its current site, patch to include stages):
- Read `SleepSessionRecord.stages` list, map `SleepSessionRecord.Stage` values
  to our `SleepStage`.
- Convert absolute timestamps into `startMin` / `endMin` relative to the
  session's `startTime`.
- Attach `bedtime` / `wakeTime` as ISO strings.

### 3.3 ViewModel state — new shapes

```kotlin
enum class SleepView { DAY, WEEK, MONTH }

data class SleepDayView(
    val date: LocalDate,
    val durationMin: Int,
    val bedtime: String,            // "22:21"
    val wakeTime: String,           // "06:53"
    val stages: List<SleepStageSegment>,     // empty → fallback bar
    val stageTotals: StageTotals,            // minutes per stage
    val score: Int,
    val qualityLabel: String,                // Poor/Fair/Good/Excellent
    val avgSleepingHrBpm: Int?,
    val deltaVsAvg7dMin: Int?,
)

data class StageTotals(
    val deepMin: Int, val lightMin: Int, val remMin: Int, val awakeMin: Int,
) {
    val totalMin get() = deepMin + lightMin + remMin + awakeMin
}

data class SleepWeekView(
    val rangeLabel: String,                   // "Apr 13–19, 2026"
    val scorePoints: List<DayScorePoint>,     // 7 entries
    val scoreAvg: Int,
    val scoreDeltaVsPrevWeek: Int?,
    val schedule: SleepScheduleAggregate,
    val idealDuration: List<DayStageStack>,   // 7 entries
    val idealAvgMin: Int,
    val idealDeltaVsPrevWeekMin: Int?,
)

data class SleepMonthView(
    val monthLabel: String,                   // "Apr 2026"
    val scorePoints: List<DayScorePoint>,     // 28–31 entries
    val scoreAvg: Int,
    val scoreBaselineForAge: Int?,            // local baseline — see §3.4
    val scoreDeltaVsPrevMonth: Int?,
    val schedule: SleepScheduleAggregate,
    val idealDuration: List<DayStageStack>,
    val idealAvgMin: Int,
    val idealDeltaVsPrevMonthMin: Int?,
)

data class DayScorePoint(val date: LocalDate, val score: Int?)

data class DayStageStack(
    val date: LocalDate,
    val deepMin: Int, val lightMin: Int, val remMin: Int, val awakeMin: Int,
)

data class SleepScheduleAggregate(
    val avgFallAsleepMinOfDay: Int?,          // e.g. 23*60+52
    val latestFallAsleepMinOfDay: Int?,
    val avgWakeMinOfDay: Int?,
    val earliestWakeMinOfDay: Int?,
    val fallAsleepSeries: List<DayClockPoint>,  // for line chart
    val wakeSeries: List<DayClockPoint>,
    val fallAsleepRegularity: Regularity,       // Regular/Irregular
    val wakeRegularity: Regularity,
)

data class DayClockPoint(val date: LocalDate, val minOfDay: Int?)

enum class Regularity { REGULAR, IRREGULAR, UNKNOWN }
```

The existing `SleepStats` datatype stays — it remains the source used by the
`HealthStatsScreen.kt`. The new `SleepDayView` / `SleepWeekView` /
`SleepMonthView` are purpose-built for the new sleep tab.

### 3.4 Client-side derivations

- **`score`** (0–100):
  `60·(duration/idealDuration, capped at 1.0) + 20·(deepRatio/0.2, capped) + 15·(remRatio/0.25, capped) + 5·(1 − awakeRatio, capped)`,
  with `idealDuration = 8 h`. Each component ensures we don't reward over-long
  deep/REM beyond healthy range. Rounds to int.
- **`qualityLabel`**: `<40 Poor, 40–59 Fair, 60–79 Good, 80–100 Excellent`.
- **`scoreBaselineForAge`**: in v1, **not** age-specific. Computed as the
  rolling 90-day average of the user's own score, shown as "Your 90-day avg".
  If fewer than 14 days of data, we skip the baseline line entirely.
- **Regularity**: stdev of fall-asleep/wake times over the window. `< 45 min`
  stdev → REGULAR, else IRREGULAR. `UNKNOWN` when fewer than 5 samples.

## 4. UI — screens & composables

### 4.1 `StatsScreen` (root)

Keeps:
- Existing top-tabs Meditation / Sleep (unchanged).
- Existing `TopAppBar` with "Stats" title and subtitle.

Changes:
- Delegate the per-tab body to `MeditationStatsContent` or `SleepStatsContent`.
- **New:** read optional deep-link hint `?initialTab=sleep` — when present,
  `topTab` starts at 1.

### 4.2 `SleepStatsContent`

Renders:
1. `SleepHeroCard` — always visible at top. Uses Day/Week/Month aggregate
   (e.g. Week hero shows average).
2. `PeriodTabs(D/W/M)` — sticky below hero.
3. Content: `SleepDayContent` / `SleepWeekContent` / `SleepMonthContent`.
4. `SleepInsightBlock` with prompt-chips — always rendered below the period
   content.

### 4.3 Day view composition (`SleepDayContent`)

Vertical list:
1. `SleepHypnogram` — waves. If `stages.isEmpty()`, show proportional
   `FallbackStageBar` instead, with tiny label "Detailed stages not available".
2. `SleepStagesDonut` — ring with center text "8h 32m".
3. `SleepStagesBreakdown` — 3 rows (REM / Light / Deep) with `%`, reference
   range, absolute `HhMm`. Awake is omitted from rows but included in donut
   for ≥3 min of awake.
4. Clickable row **"Avg sleeping HR"** → for v1, opens a small bottom sheet
   with a placeholder line: "Avg HR: 54 bpm across 22:21–06:53". No
   landscape chart in v1.
5. Placeholder rows for **"Average blood oxygen"** and **"Breathing score"** —
   disabled style, grey chevron, tap shows toast "Coming soon".

### 4.4 Week view composition (`SleepWeekContent`)

1. `SleepScoreChart` — 7 bars, Y axis 0–100, tooltip on tap.
2. `SleepScheduleSection` — two sub-cards:
   - Fall-asleep: avg time + regularity label + line chart with highlighted
     "Latest fall-asleep this week".
   - Wake-up: avg time + regularity label + line chart with "Earliest wake
     this week".
3. `IdealSleepDurationChart` — stacked bars Deep/Light/REM/Awake, 7 days,
   Y axis 0–10 h.

### 4.5 Month view composition (`SleepMonthContent`)

Same blocks as Week, with ranges over 28–31 days and axis ticks
`01, 08, 15, 22, 30`. `SleepScoreChart` draws a horizontal dashed baseline
at `scoreBaselineForAge`.

### 4.6 `SleepInsightBlock`

- Local-rule paragraph (§5.1).
- Row of `SleepPromptChips`:
  - "Порекомендуй технику дыхания перед сном"
  - "Почему я плохо сплю?"
  - "Как углубить Deep sleep?"
- Chip tap → `AiCoachBottomSheet` opens (existing component), seeded with
  that text via a new `AiCoachViewModel.sendPrefilled(text: String)` helper
  (internally calls existing `send(text)` after the sheet mounts).

### 4.7 Colors

Add to `ui/theme/Color.kt` — blue gradient palette:
- `SleepDeep`  = `0xFF3840E1`
- `SleepLight` = `0xFF0E8BF5`
- `SleepRem`   = `0xFF21C6FF`
- `SleepAwake` = `0xFFB7E6FF`
- `SleepAccent` stays `0xFF7C4DFF` for score-related UI.

### 4.8 Hero card behaviour

- Day: shows `"8h 32m"` + date + quality badge + delta vs 7-day avg.
- Week/Month: shows `"Avg: 7h 12m"` + range + average quality + delta.

## 5. Business logic

### 5.1 `LocalSleepInsight` rule engine

New file `utils/LocalSleepInsight.kt`. Pure function:

```kotlin
fun buildLocalInsight(day: SleepDayView, trend: SleepTrend): String
```

Rules (first matching wins, kept short and humane):

| Condition                                                     | Text (RU)                                                  |
|---------------------------------------------------------------|-------------------------------------------------------------|
| `durationMin < 360`                                           | "Ты спал меньше 6 часов — попробуй лечь пораньше сегодня." |
| `stageTotals.deepMin < durationMin * 0.12`                    | "Мало глубокого сна. Снизь экран за час до сна."           |
| `regularity == IRREGULAR`                                     | "Сон нерегулярный — попробуй фиксированное время отбоя."    |
| `score >= 80`                                                 | "Отличный сон — удерживай текущий режим."                  |
| default                                                       | generic "Хороший сон" line                                 |

### 5.2 Aggregations

Aggregations live in `StatsViewModel` in a private section `// sleep v2
aggregations`. Each transform is a **pure function** that takes
`List<SleepDayDto>` + optional `List<HrDayDto>` and returns the relevant view
type. Transforms are unit-testable without the VM.

### 5.3 Deep link from `HomeScreen`

The existing `💤 Sleep` card in `HomeScreen.kt` wraps its root box with
`.clickable { navController.navigate("${Route.HISTORY}?initialTab=sleep") }`.

`Route.HISTORY` becomes:
```kotlin
const val HISTORY = "history?initialTab={initialTab}"
fun history(initialTab: String? = null) =
    if (initialTab != null) "history?initialTab=$initialTab" else "history"
```

Navigation argument is parsed inside `StatsScreen`.

## 6. Backend

Scope for v1 is minimal — no new aggregation endpoints. Backend changes:

- `POST /api/integrations/apple-health` (and the Health Connect variant)
  accept the new optional fields on each `SleepDayDto`:
  `bedtime`, `wakeTime`, `stages[]`, `score`.
- `Integration.data.sleep` schema (Mongoose) is extended to allow the new
  keys. Schemaless `Mixed` already allows it, but we add explicit validators
  in `services/integrations.js` so we reject malformed `stages` arrays.
- No new routes.

## 7. Error, loading, empty states

- Loading: `ShimmerStatScreen` (already exists). Show for the initial fetch
  only; switching D/W/M is instant since data is client-aggregated.
- Empty (no Health Connect / no sleep days in window):
  - Hero shows "No sleep data yet".
  - Charts render a placeholder card "Connect Health Connect in Profile →
    Devices" with a button deep-linking to `Route.PROFILE`.
- Partial (stages missing): §4.3 fallback bar.
- Chip tap while offline: AI coach sheet surfaces existing offline banner
  (already implemented in `AiCoachViewModel`).

## 8. Testing

### 8.1 Unit tests (new)

New test file `app/src/test/java/…/sleep/SleepAggregationsTest.kt` covering:
- `buildDayView`: stages missing → fallback bar marker set.
- `buildWeekView`: delta computed against last week.
- Score formula edge cases: 0-min sleep, 15-h sleep, zero deep.
- `Regularity`: boundary at 45-min stdev.
- `LocalSleepInsight`: each rule branch.

Use JUnit4 + kotlin-test assertions — matches existing test style in repo.

### 8.2 Screenshot / preview

Add `@Preview` composables for each of the sleep sub-screens with fake data so
`Preview` pane in Android Studio works without running the app.

### 8.3 Manual QA checklist

- D-tab renders with and without stage segments.
- D-tab date picker goes back 30 days but not into the future.
- W-tab range swipes back ≥4 weeks.
- M-tab date picker picks previous months, disables future.
- Tap from Home sleep card lands on Sleep tab, not Meditation.
- Prompt-chip tap opens AI coach bottom sheet **only on tap** (no background
  fetch).
- With Health Connect disconnected: empty states render correctly.

## 9. Out of scope (explicit)

- Landscape HR + SpO₂ overlay on hypnogram.
- Dedicated screens for Avg HR / Avg blood oxygen / Breathing score.
- 21-day "Smart sleep improvement plan".
- Sleep society badges.
- "Learn more" website integration.
- Server-side sleep aggregation endpoints.
- Age-cohort baseline ("Average for users your age").

## 10. Migration / rollout

- No DB migration required (Mongoose `Mixed` schema already permissive).
- Android min SDK unchanged.
- Feature lands in one version of the app; no feature flag.
- Existing `SleepStats` model left in place → `HealthStatsScreen` continues
  to work.
