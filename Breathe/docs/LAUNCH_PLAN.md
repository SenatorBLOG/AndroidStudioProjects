# Breathe V1 · 3-Week Launch Sprint

**Start:** Mon Apr 27, 2026 · **Target launch:** Sat May 16, 2026
**Rule:** No new features for 3 weeks. Only fixes + polish.

---

## Week 1 · Triage & Critical Fixes (Apr 27 – May 3)

| Date          | Slot          | Task                                       |
| ------------- | ------------- | ------------------------------------------ |
| Mon Apr 27    | 19:00–21:00   | 🔍 Bug audit + triage list                |
| Tue Apr 28    | 19:00–21:00   | 📱 Pixel 8 Pro pickup + USB debug setup   |
| Wed Apr 29    | 19:00–21:00   | 🛠 Critical fixes 1/3                     |
| Thu Apr 30    | 19:00–21:00   | 🛠 Critical fixes 2/3                     |
| Fri May 1     | 19:00–21:00   | 🛠 Critical fixes 3/3                     |
| Sat May 2     | 11:00–15:00   | 🌊 Weekend deep work — UX pass            |
| Sun May 3     | 11:00–14:00   | 🪞 Week 1 review + plan adjust            |

## Week 2 · Polish + Internal Testing (May 4 – May 10)

| Date         | Slot         | Task                                              |
| ------------ | ------------ | ------------------------------------------------- |
| Mon May 4    | 19:00–21:00  | 🎨 Extract design tokens from breatheonline.app  |
| Tue May 5    | 19:00–21:00  | 🎨 Apply tokens to Theme.kt + Color.kt           |
| Wed May 6    | 19:00–21:00  | 🔤 Typography pass — uniform fonts                |
| Thu May 7    | 19:00–21:00  | 🏪 Play Console — Internal Testing setup          |
| Fri May 8    | 19:00–21:00  | 📦 Build signed AAB + upload                      |
| Sat May 9    | 11:00–15:00  | 👥 Send to 5 closest testers                      |
| Sun May 10   | 11:00–14:00  | 📊 Process tester feedback                        |

## Week 3 · Beta + Launch (May 11 – May 16)

| Date         | Slot         | Task                                  |
| ------------ | ------------ | ------------------------------------- |
| Mon May 11   | 19:00–21:00  | 🛠 Fix critical beta feedback 1/2     |
| Tue May 12   | 19:00–21:00  | 🛠 Fix critical beta feedback 2/2     |
| Wed May 13   | 19:00–21:00  | 👥 Closed Beta — 20 testers           |
| Thu May 14   | 19:00–21:00  | ✨ Beta polish + screenshots          |
| Fri May 15   | 19:00–21:00  | 📋 Production submit prep             |
| Sat May 16   | 11:00–14:00  | 🚀 LAUNCH — Submit to Play Store      |

---

## Если отстаём от графика

Режем по приоритету:
1. **Сначала режем V1.5 идеи** (sibling design parity → V2)
2. **Потом nice-to-have фиксы** (косметика → V2)
3. **Никогда не режем:** crash bugs, broken core flows, Play Store submission readiness

## Definition of Done для V1

- [ ] Запускается без крашей на Pixel 8 Pro (Android 14+)
- [ ] Все 4 основных экрана работают: Home, Breathe, History, Profile
- [ ] Sign-in flow работает (или offline mode)
- [ ] Локализация RU/EN корректна
- [ ] Privacy policy опубликована (можно простую static page на breatheonline.app)
- [ ] Подписанный AAB загружен в Production track
- [ ] Минимум 5 тестеров подтвердили: «работает, заметных багов нет»
