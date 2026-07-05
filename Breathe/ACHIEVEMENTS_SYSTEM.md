# Achievements System

One achievement system should serve:
- Android app
- website
- wearable/imported health data
- email reminder jobs

## Core model

Use one set of `achievement_definitions` shared by all clients.

Suggested fields:
- `slug`
- `title`
- `description`
- `category`
- `iconKey`
- `iconUrl`
- `unit`
- `sourceTypes`
- `levels[]`

Each level should contain:
- `level`
- `label`
- `targetValue`
- `description`

## User progress

Store progress separately in `user_achievement_progress`.

Suggested fields:
- `userId`
- `achievementSlug`
- `currentValue`
- `currentLevel`
- `status`
- `lastProgressAt`
- `completedAt`
- `sourceBreakdown`

`sourceBreakdown` should track contributions from:
- `app_sessions`
- `web_sessions`
- `health_connect`
- `fitbit`
- `google_fit`

This allows one user to progress the same achievement from multiple products without double counting.

## Event log

Store immutable updates in `achievement_events`.

Suggested fields:
- `userId`
- `achievementSlug`
- `sourceType`
- `delta`
- `valueAfter`
- `createdAt`
- `metadata`

Use this for:
- audit/debugging
- timeline on the achievement detail page
- reminder jobs
- analytics

## API contract

Client endpoints expected by the Android app:
- `GET /achievements`
- `GET /achievements/highlights?limit=3`
- `GET /achievements/:slug`

## Reminder jobs

Email/push reminders should query:
- `status = in_progress`
- `lastProgressAt` older than reminder threshold
- unfinished next level exists

Recommended email content:
- achievement title
- current progress
- next target
- direct link to website achievement detail page

## Visual rules

- Locked: grayscale icon
- In progress: grayscale or muted icon with progress bar
- Completed: full color icon
- Levels: 1, 2, 3 stars

## Sync rules

- Web and app both write session events to the same backend
- Health imports write events, not raw badge state
- Badge state is always computed on the backend from canonical progress
- Clients render server state, not their own final truth
