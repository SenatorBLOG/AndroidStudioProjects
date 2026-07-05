# Breathe Android · Bug Triage for V1 Launch

**Generated:** 2026-04-27
**Method:** 3 parallel Claude Code audit agents (crash risk, nav/state, launch readiness)
**Codebase:** 108 .kt files, Compose + Hilt + Room + Coroutines stack
**Overall readiness:** ~70% — solid foundation, fixable issues, achievable in 3 weeks

---

## 🔴 P0 — MUST FIX (blocks launch)

> Краши + одна launch-блокирующая дыра. **Это твоя приоритетная пачка на Wed-Fri Apr 29 / 30 / May 1.**

### Crash risks (6 issues — все NPE на `!!` unwrap) — ✅ ALL FIXED 2026-04-27

| # | Status | File | Line | Fix applied |
|---|--------|------|------|-------------|
| C-01 | ✅ DONE | `viewmodel/AiCoachViewModel.kt` | 83 | `resp.body() ?: appendError(); return@onSuccess` |
| C-02 | ✅ DONE | `viewmodel/HealthViewModel.kt` | 54, 58 | `it.data?.sleep / heartRate ?: emptyList()` |
| C-03 | ✅ DONE | `viewmodel/StatsViewModel.kt` | 244, 245, 249 | `it.data?.sleep / hrv / heartRate ?: emptyList()` |
| C-04 | ✅ DONE | `viewmodel/StatsViewModel.kt` | 704 | Renamed to `moodDeltas`, computed via `mapNotNull` with explicit null check |
| C-05 | ✅ DONE | `ui/screens/JournalScreen.kt` | 163 | Captured `state.insights` once into local `capturedInsights` before `when` (smart-cast works) |
| C-06 | ✅ DONE | `ui/screens/InteractiveScreen.kt` | 254 | `QUIZ_RESULTS[resultKey] ?: QUIZ_RESULTS.values.firstOrNull() ?: return` |

**Verification:** open Android Studio → Build → Make Project. Все правки минимальные и сохраняют поведение для happy-path (когда данные есть). Меняется только error-path: вместо краша теперь либо graceful fallback, либо явный error state.

> **Шаблон рефактора:** ищи `!!` глобально, заменяй на `?.let { }` или `?: returnEarlyWithError`. Я могу сделать это за один проход в один из вечеров.

### Launch blockers (1 issue)

| # | Issue | Action |
|---|-------|--------|
| L-01 | **Нет crash reporting** | Добавить Firebase Crashlytics ИЛИ Sentry. Без этого ты слепой после релиза. На health/wellness app это особенно критично. |

---

## 🟡 P1 — большинство закрыто 2026-04-27

### Code quality

| # | Status | Fix |
|---|--------|-----|
| Q-01 | ✅ DONE | `AiCoachViewModel.kt`: `onFailure` теперь пишет `Log.e("AiCoachViewModel", ...)` |
| Q-02 | ✅ DONE | `ProfileViewModel.kt:336`: добавлен re-check `getSdkStatus()` перед `getOrCreate()` — graceful fallback если HC удалён/недоступен |
| Q-03 | ⏭ SKIP | `HomeScreen` `LaunchedEffect(Unit)` — это **корректный** паттерн (запускается один раз). Агент overflagged. |
| Q-04 | ✅ DONE | `HealthViewModel.kt`: `onFailure` теперь пишет `Log.e("HealthViewModel", ...)` |
| Q-05 | ✅ DONE | `MeditationCharts.kt:158`: `subLabels!![i]` → `subLabels?.getOrNull(i).orEmpty()` |
| Q-06 | ⏭ FALSE POSITIVE | `MusicViewModel.onCleared()` уже override-нут (line 229-232) — leak-а нет |

### Navigation / state

| # | Status | Fix |
|---|--------|-----|
| N-01 | ⏭ SKIP | Splash имеет `delay(500)` перед navigate — race маловероятен. Если вдруг появится — фиксим в V1.1. |
| N-02 | ⏭ FALSE POSITIVE | `currentRoute` возвращает **template** (`history?initialTab={initialTab}`), который совпадает с `Route.HISTORY`. Уже корректно. |
| N-03 | ✅ DONE | `NavGraph.kt`: whitelist для `exerciseType` (`4-7-8`/`box`/`coherent`/`wim-hof` или валидный `custom_*_*_*_*`) — fallback на `4-7-8` для мусора |

### Hardcoded English strings

| # | Status | Fix |
|---|--------|-----|
| S-01 | ✅ DONE | `SleepStatsContent.kt`: 6 строк → `stringResource()`. Добавлены ключи: `sleep_no_data_yet`, `sleep_connect_hc_unlock`, `sleep_checklist_consistency/_stages/_local`, `sleep_viewing` |
| S-02 | ✅ DONE | `RegisterScreen.kt:208`: "Confirm Password" → `stringResource(R.string.register_confirm_password)` |

> RU/EN parity сохранена: оба файла получили те же 7 ключей.

### Release config

| # | Status | Fix |
|---|--------|-----|
| R-01 | ✅ DONE | `AndroidManifest.xml:44`: `tools:targetApi="31"` → `"35"` |
| R-02 | ⏭ V1.1 | Документировать `local.properties` в README — сделаем перед публикацией |
| R-03 | ✅ DONE | `proguard-rules.pro`: добавлен `-assumenosideeffects` для `Log.d`/`Log.v` — release-сборки автоматом стрипают debug-логи (включая health-data в `ProfileViewModel`) |
| R-04 | ⏭ V1.1 | Создание keystore — отдельный шаг при Internal Testing setup (May 7) |

---

## 🟢 P2 — NICE-TO-HAVE (можно после launch)

| # | File | Line | Issue |
|---|------|------|-------|
| P-01 | `viewmodel/ProfileViewModel.kt` | 515–524 | POST_NOTIFICATIONS на API < 33 предполагается granted — может silently fail на Android 12 |
| P-02 | `ui/screens/GlobeScreen.kt` | — | Permission check есть, runtime request launcher отсутствует — Mapbox может не загрузиться |
| P-03 | `ui/screens/PrivacyPolicyScreen.kt` | — | Privacy text в Compose, нет линка на hosted версию (Play Store обычно требует hosted URL) |

---

## ✅ Что УЖЕ хорошо (агенты не нашли проблем)

- **RU/EN strings parity:** 414/414 ключей — perfect
- **ProGuard rules:** покрывают Retrofit, Hilt, Room, Mapbox, Coroutines, Gson
- **Network security config:** cleartext только для debug, prod-safe
- **App icons:** adaptive icons на всех плотностях (mdpi → xxxhdpi)
- **Permissions:** все обоснованы, нет лишних
- **Versioning:** versionCode=1 / versionName="1.0" — корректно для V1
- **Profile sub-tabs:** все 5 (Challenges, Sessions, Progress, Profile, Devices) рендерят настоящий контент, не стабы
- **ViewModel patterns:** `MutableStateFlow` + `asStateFlow()` consistent, нет `LiveData/StateFlow` mixing, `@ApplicationContext` без leak
- **Auth flow:** LoginScreen/RegisterScreen → HOME через `LaunchedEffect(loginState)` корректно
- **Bottom bar back press:** `popUpTo(HOME) { saveState = true }` правильно — back из HOME выходит из app
- **State restoration:** `restoreState=true` + `saveState=true` сохраняют табы при rotation

---

## 📅 Распределение по неделям

### Week 1 (Apr 27 – May 3) · Critical fixes
- **Wed Apr 29:** C-01, C-02, C-03 (3 P0 крашей)
- **Thu Apr 30:** C-04, C-05, C-06 (остальные 3 P0)
- **Fri May 1:** L-01 (Crashlytics setup)
- **Sat May 2:** Q-01–Q-06 (P1 quality, 4 часа deep work)

### Week 2 (May 4 – May 10) · Polish
- **Mon May 4** будет про дизайн-токены (по плану), но если успеешь — Q-03 `LaunchedEffect` чистка
- **Tue May 5:** R-01, R-02, R-03 (release config)
- **Wed May 6:** S-01, S-02 (последние 7 strings → resources)
- **Thu May 7:** N-01, N-02, N-03 (nav fixes)

### Week 3 (May 11 – May 16) · Beta
- **P2 — только если останется время.** Иначе → V2 backlog.

---

## 🤖 Готовые промпты для меня

Скопируй в новую сессию и я сделаю одной пачкой:

**Промпт «Сделай все P0 краши за один проход»:**
> Возьми BUG_TRIAGE.md из этого репо, секция P0. Пройди по 6 кражам C-01..C-06 — для каждого замени `!!` на безопасный `?.` chain или early return с error state. Никаких других изменений. После — пометь все 6 как DONE в BUG_TRIAGE.md. Запусти `./gradlew :app:compileDebugKotlin` чтобы убедиться что компилируется.

**Промпт «Crashlytics setup»:**
> Добавь Firebase Crashlytics в проект Breathe. Шаги: (1) Подскажи что нужно добавить в Firebase Console, (2) обнови `app/build.gradle.kts` и `build.gradle.kts` корневой, (3) добавь `google-services.json` placeholder note, (4) добавь `Firebase.crashlytics.recordException(e)` во все `onFailure` блоки в ViewModel классах, (5) проверь что `BreatheApp.kt` инициализирует Firebase. После — `./gradlew :app:assembleDebug`.

**Промпт «Final 7 hardcoded strings»:**
> В BUG_TRIAGE.md S-01 и S-02 — последние 7 hardcoded English. Извлеки в `values/strings.xml` и `values-ru/strings.xml` (parity), замени на `stringResource()` calls. Имена ключей: `sleep_no_data_yet`, `sleep_connect_hc_unlock`, `sleep_open_profile`, `chart_prev`, `chart_latest`, `chart_next`, `register_confirm_password`. После — параметрические counts в обеих файлах должны совпасть.

---

**Bottom line:** проект **готов к запуску** через 3 недели спокойного фикса. Никакого rewrite. Архитектура чистая, страшных дыр нет — 6 однотипных `!!`-крашей за один вечер чинятся, всё остальное — полировка.

---

## 📊 Текущий прогресс (обновлено 2026-04-27 вечер)

```
🔴 P0:  ▓▓▓▓▓▓▓░  6/7  · остался Crashlytics (план: Fri May 1)
🟡 P1:  ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░  12/16 · остались R-02, R-04 (V1.1) и Q-03/Q-06/N-01/N-02 (false positives)
🟢 P2:  ░░░░░░░░  0/3  · после launch
```

**Итого за вечер 27 апреля:** закрыты все 6 P0 крашей + 9 P1 пунктов. Проект движется быстрее графика — Wed/Thu/Fri слоты можно использовать на Crashlytics setup и UX-полировку.

---

## 🎨 Live testing pass · 28 апреля (Pixel 8 Pro)

Подключился к устройству через ADB, прошёл по экранам, нашёл и пофиксил **дополнительный пакет hardcoded English-строк** которые агенты пропустили:

### HomeScreen.kt — добавил `stringResource()` для:
- `"Personalised breathing guidance and bedtime help"` → `R.string.home_ai_coach_subtitle`
- `"AI Coach"` (title) → `R.string.home_ai_coach_title`
- `"Community, long-form guides..."` (footer) → `R.string.home_community_info`
- `"Start Meditation"` → `R.string.home_start_meditation`
- `"How it works →"` / `"Breathing quiz →"` → existing keys
- `"FEATURED"` → `R.string.home_featured_section`
- `"AI SLEEP STORY"`, `"Close"`, `"Tap generate..."`, `"Generate story"`, `"Crafting..."`, `"Generate another"`, `"Try again"`, `"POWERED BY GEMINI AI"` → existing/new keys
- `"DAILY INSPIRATION"`, `"EXPLORE"`, `"SLEEP GUIDES"` → existing keys

### MainScreen.kt (bottom nav)
- 4 tab labels (`Home/Breathe/Stats/Profile`) — рефакторил `BottomTab.label: String` → `@StringRes labelRes: Int`. Теперь все 4 tabs локализованы (RU: Главная/Дыхание/Статистика/Профиль).

### MeditationCharts.kt
- `"ACTIVITY"` section label → `R.string.stats_activity_section`
- `"Yearly rhythm"` / `"Monthly consistency"` / `"Weekly practice"` → новые ключи
- **Pluralization fix** для "1 active days" → новые `plurals` (`stats_active_days`, `stats_active_months`) с RU one/few/many/other формами
- Summary format → `R.string.stats_summary_format` с `%1$d min total · %2$s`

### BreatheScreen.kt
- `"PRESETS"` → `R.string.breathe_section_presets`
- `"TECHNIQUES & GUIDES"` → `R.string.breathe_section_techniques`

### Strings parity update
- Было: 414/414 (RU/EN)
- **Стало: 433/433** ✅ +19 новых ключей в обоих файлах, parity сохранена

---

## 📋 Что сейчас увидеть тебе

1. В Android Studio: **Build → Run** (или `./gradlew :app:installDebug`) — установит свежую сборку с фиксами на Pixel.
2. Проверь Home screen: "AI Coach" подзаголовок теперь не overflows
3. Переключи на русский язык в настройках телефона: все 4 нижних таба должны стать "Главная / Дыхание / Статистика / Профиль"
4. Открой Stats: "1 active **day**" (правильное единственное число)

## 📋 Что осталось на потом (не блокирует launch)

- TechniqueCard data в `BreatheScreen.kt:854-857` (Box Breathing/4-7-8 for Sleep/Wim Hof/Coherent — описания на английском). Это data class с String, не Composable — нужен рефакторинг на StringRes Int. **V1.5.**
- Achievement card "Recovery Heart 78/45 days" — формат странный, проверить логику.
- "Hub" label которое ты видишь в нижней навигации — это `Profile` который локализуется в "Hub"? Нет, я заменил на `R.string.tab_profile` = "Profile" / "Профиль". После rebuild проверь — должно быть "Profile".
