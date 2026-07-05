# Profile Tabs UX Redesign
**Date:** 2026-04-22  
**Scope:** ChallengesTab, ProgressTab, SessionsTab, Sign Out placement  
**Files:** `ChallengesTab.kt`, `SessionsAndProgressTabs.kt`, `ProfileScreen.kt`

---

## Problem Summary

| Screen | Issue |
|---|---|
| ChallengesTab | Challenge names have no explicit `style` → wrap to 2 lines, appear huge; all cards are same monotone green; no progress shown for active challenges; no descriptions for available ones |
| ProgressTab | 4 plain numbers + flat progress bar — no visual engagement |
| SessionsTab | Flat unsorted list; no search; `take(15)` hard limit without explanation |
| Sign Out | Button is placed outside the `when(tab)` block → visible under ALL tabs when scrolled down |

---

## ChallengesTab Redesign

### Typography fix
- All `Text(challenge.name)` → add `style = MaterialTheme.typography.bodyMedium`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`

### Category color system
Map challenge slug/icon/name keywords to an accent color:

| Category key | Color |
|---|---|
| sleep / moon / night | Indigo `#6C63FF` |
| energy / zap / bolt | Orange `#FF8C42` |
| focus / target | Amber `#F5A623` |
| calm / stress / wave | Violet `#A855F7` |
| breath / wind / air | Teal (theme primary) |
| default | Theme primary |

The accent color is used for: icon tint, progress bar fill, chip border.

### AI Recommended card
- Full-width hero card with `Brush.linearGradient` from `accentColor.copy(0.18f)` to transparent
- Title: `typography.titleSmall`, `maxLines=1`, `overflow=Ellipsis`
- Reason text: `typography.bodySmall`, color = subtitle
- "Join Now" button: uses accent color as containerColor

### My Active Challenges
Each `UserChallengeDto` row:
- Icon tinted with category accent color
- Name: `typography.bodyMedium`, bold, `maxLines=1`
- Linear progress bar (0..1 = completedDays.size / challenge.duration) with accent color fill
- "X/Y days" label below bar
- Check-in button (circle icon) + abandon button (X icon, subtle)

### Available Challenges
Each `ChallengeDto` row:
- Icon tinted with accent color
- Name: `typography.bodyMedium`, `maxLines=1`
- Subtitle/description: `typography.labelSmall`, `maxLines=1`, color = subtitle (was never shown before)
- Duration badge: small Chip "7 days" in top-right using accent color
- "Join" button: uses accent color

---

## ProgressTab Redesign

### Weekly Goal — Canvas Arc (replaces flat bar)
- Draw a 240° arc (Canvas) at 120dp diameter
- Background arc: `colors.surface` or `subtitle.copy(0.15f)`
- Progress arc: filled with `Brush.sweepGradient(accent → primary)`
- Center text: percentage `%` in `typography.headlineMedium`
- Below arc: "X of 70 min" in labelSmall

### 7-Day Activity Strip
- Row of 7 circles (28dp each), labeled Mon–Sun abbreviations below
- Filled circle = had a session that day (check `state.sessions` dates)
- Today's circle has a ring/border

### Key Metrics — keep but better visual
- 2×2 grid of `BentoStat`-like tiles (same style as Sessions tab)
- Sessions, Minutes, Current Streak, Best Streak

### Personal Bests section
- Small horizontal row: "Longest session: X min", "Best streak: X days"
- Uses the existing data already in `ProfileState`

---

## SessionsTab Redesign

### Search bar
- `OutlinedTextField` or `BasicTextField` wrapped in a pill-shaped box at the top of the column
- Filters `state.sessions` by `typeLabel(type, technique).contains(query, ignoreCase=true)`
- State: `var searchQuery by remember { mutableStateOf("") }`

### Date grouping
Group sessions into buckets computed with `remember(state.sessions)`:

| Bucket | Condition |
|---|---|
| Today | sessionDate is today |
| Yesterday | sessionDate is yesterday |
| This Week | within last 7 days (not today/yesterday) |
| Earlier | everything else |

Each bucket renders:
1. Section header: `Text("Today", typography.labelMedium, subtitle, SemiBold)` — same style as current "Recent sessions" label
2. `SessionItem` rows (existing component, no change needed)

Empty bucket → skip (don't show header).

Remove `take(15)` hard limit — show all sessions under their respective groups. (The outer `LazyColumn` or regular Column with scroll is already handled by the parent `verticalScroll`.)

---

## Sign Out Placement

**Current:** Button is rendered unconditionally after the `when(tab)` block in `ProfileScreen.kt`.

**Fix:** Move the `Spacer(28dp) + Sign Out Box + Spacer(32dp)` block inside the `TAB_PROFILE` branch of the `when(tab)` expression. On all other tabs, Sign Out is simply not rendered.

```kotlin
TAB_PROFILE -> {
    ProfileFormTab(...)
    Spacer(Modifier.height(28.dp))
    // Sign out button here
    SignOutButton(colors, navController, viewModel)
    Spacer(Modifier.height(32.dp))
}
```

Extract the button into a private `SignOutButton()` composable for clarity.

---

## Files Changed

| File | Change |
|---|---|
| `ChallengesTab.kt` | Full redesign — typography fix, category colors, progress bars, improved cards |
| `SessionsAndProgressTabs.kt` | SessionsTab: search + date grouping; ProgressTab: arc + strip + bests |
| `ProfileScreen.kt` | Move Sign Out inside TAB_PROFILE branch |

No new files. No data model changes. No ViewModel changes.

---

## Non-goals
- No new API calls
- No animation polish (can be added later)
- No swipe-to-abandon gesture (can be added later)
- No lazy loading / pagination for sessions
