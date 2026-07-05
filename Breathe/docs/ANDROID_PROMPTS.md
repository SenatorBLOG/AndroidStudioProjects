# Breathe Android · Promтpts Library

> Этот файл — мой "сценарий" для каждого сеанса с Claude Code. Скопируй промпт, начни новую сессию (или продолжи текущую) — сделаю работу.
> Для Google Sheets: создай вкладку "Android Prompts", скопируй таблицы ниже в неё.

---

## 📋 Tab: Android Prompts

| Date | Session | Prompt |
|------|---------|--------|
| Apr 27 | Bug audit | Просканируй весь Android-проект Breathe в `app/src/main/java/com/breatheonline/breathe/`. Найди: (1) явные runtime crashes (uncaught exceptions, NPE risks, missing @Nullable), (2) сломанные навигации (NavController routes не совпадают), (3) сломанные ViewModels (StateFlow без collect, race conditions), (4) деформации UI (overflow, hardcoded sizes на вместо dp). Выдай BUG_TRIAGE.md с приоритетами P0/P1/P2 и file:line ссылками. |
| Apr 29 | Fix P0 #1 | Возьми top-1 P0 баг из BUG_TRIAGE.md. Прочитай файл, пойми контекст, напиши минимальный fix. Никакого рефакторинга. После — пометь как DONE в BUG_TRIAGE.md. |
| Apr 30 | Fix P0 #2 | Top-2 P0 баг. То же что вчера. Минимум кода, максимум фокуса. |
| May 1  | Fix P0 #3 | Top-3 P0 баг. Если P0 закончились — переходим на P1. |
| May 2  | UX pass | Запусти приложение на эмуляторе или Pixel. Пройди все 4 экрана. Зафиксируй: где spacing неровный, где шрифт прыгает, где цвет не из палитры. Внеси правки, сохрани скриншоты в `docs/screenshots/v1-pre-polish/`. |
| May 4  | Tokens · web | Прочитай `C:\Users\Mikhail Senatorov\Documents\Douglas\Breathe`. Извлеки из CSS/JS: основные цвета, шрифты, spacing-токены, иконки. Запиши в `docs/TOKENS.md` в формате таблицы. |
| May 5  | Tokens · apply | Возьми TOKENS.md и обнови: `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`. Сделай светлую и тёмную тему совместимой с Material3. Не трогай экраны — только тему. |
| May 6  | Typography | Пройди по всем `Text(...)` вызовам в `ui/screens/*`. Замени любые inline `fontSize=` / `fontWeight=` на использование `MaterialTheme.typography.bodyMedium`/`titleSmall` etc. Сделай единую иерархию. |
| May 7  | Play Console | Подскажи пошагово: (1) как создать Internal Testing track в Play Console, (2) что нужно для App Listing (privacy, screenshots, feature graphic), (3) как сгенерировать signing key и его сохранить безопасно. |
| May 8  | Build AAB | Покажи команду `gradlew bundleRelease`, где найти .aab, как подписать (если не configured) и как загрузить через Play Console UI. |
| May 11 | Beta fix #1 | Прочитай FEEDBACK.md. Возьми top-1 «критично» из feedback. Fix → test → push. |
| May 12 | Beta fix #2 | Top-2 «критично» из FEEDBACK.md. То же что вчера. |
| May 14 | Screenshots | Сгенерируй: (1) feature graphic 1024×500, (2) 2 phone screenshots для Play Store, (3) короткое описание (80 chars), (4) длинное описание (4000 chars) на RU и EN. |
| May 15 | Submit prep | Чеклист всего что нужно для Production submit. Пройди по нему — что зелёное, что красное. |
| May 16 | LAUNCH | Финальная проверка. Roll out. Поздравительный пост в твой канал. |

---

## 🐛 Tab: Bug Tracker

| ID  | Priority | Screen | Description | Status | Fixed in |
|-----|----------|--------|-------------|--------|----------|
| 001 | P0       | —      | (заполнится Apr 27 во время bug audit) | OPEN | — |

---

## 💬 Tab: Tester Feedback

| Tester | Date | Severity | Quote | Action |
|--------|------|----------|-------|--------|
| —      | —    | —        | —     | —      |

---

## 🚦 Daily Status (заполняй сам)

| Date | Planned | Done | Blockers |
|------|---------|------|----------|
| Apr 27 |  |  |  |
| Apr 28 |  |  |  |
| Apr 29 |  |  |  |
| Apr 30 |  |  |  |
| May 1  |  |  |  |
| May 2  |  |  |  |
| May 3  |  |  |  |
| May 4  |  |  |  |
| May 5  |  |  |  |
| May 6  |  |  |  |
| May 7  |  |  |  |
| May 8  |  |  |  |
| May 9  |  |  |  |
| May 10 |  |  |  |
| May 11 |  |  |  |
| May 12 |  |  |  |
| May 13 |  |  |  |
| May 14 |  |  |  |
| May 15 |  |  |  |
| May 16 |  |  |  |
