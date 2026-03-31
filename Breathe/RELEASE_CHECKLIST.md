# Breathe — Google Play Release Checklist

## ✅ Done in code (this sprint)

- [x] Release build type with `isMinifyEnabled = true`, `isShrinkResources = true`
- [x] ProGuard/R8 rules for Retrofit, Gson, Hilt, Room, WorkManager, Mapbox, Compose
- [x] `isDebuggable = false` explicitly set in release build type
- [x] Debug logging in `DatabaseModule` gated behind `BuildConfig.DEBUG`
- [x] Network Security Config added — blocks cleartext HTTP to `breatheonline.app`
- [x] `android:networkSecurityConfig` wired in Manifest
- [x] `android:windowSoftInputMode="adjustResize"` added to MainActivity
- [x] Manifest cleaned — permissions annotated with purpose comments
- [x] Signing config reads from `local.properties` (never hardcoded)
- [x] `kotlinx-coroutines-test` added for ViewModel testing
- [x] `SessionCalculations.kt` — extracted pure streak/BMI/format logic
- [x] `SessionCalculationsTest.kt` — 20 unit tests covering streak, BMI, formatting
- [x] `BreathPresetsTest.kt` — preset integrity tests
- [x] OkHttp duplicate dependency removed (single BOM declaration)
- [x] `packaging { resources { excludes } }` added to avoid META-INF conflicts

---

## ⚠️ YOU MUST DO BEFORE PUBLISHING

### 🔐 Security — CRITICAL
- [ ] **Revoke exposed Mapbox token** at https://account.mapbox.com/access-tokens/
  - Token was committed to `gradle.properties` and `local.properties`
  - Current token: starts with `pk.eyJ1IjoibWlraGFpbDIyMjIi...`
- [ ] Generate a new restricted Mapbox token (scope: styles:read, tiles:read only)
- [ ] Add new token to `local.properties` (this file must be in `.gitignore`)
- [ ] Verify `local.properties` is listed in `.gitignore`
- [ ] Run `git rm --cached local.properties gradle.properties` if either is tracked
- [ ] Clean git history if token was ever committed: use BFG Repo-Cleaner

### 🔑 App Signing
- [ ] Generate a release keystore:
  ```
  keytool -genkey -v -keystore breathe-release.jks \
    -alias breathe -keyalg RSA -keysize 2048 -validity 10000
  ```
- [ ] Add to `local.properties` (never commit this file!):
  ```
  RELEASE_STORE_FILE=path/to/breathe-release.jks
  RELEASE_STORE_PASSWORD=yourPassword
  RELEASE_KEY_ALIAS=breathe
  RELEASE_KEY_PASSWORD=yourKeyPassword
  ```
- [ ] Back up the keystore securely — losing it means you can never update the app

### 📋 Play Console — Account & App Setup
- [ ] Create app in Google Play Console (https://play.google.com/console)
- [ ] Set app category: Health & Fitness
- [ ] Set `applicationId` to your final package name
  - Current: `com.example.breathe` — **this looks unprofessional and may be rejected**
  - Change to: `com.yourdomain.breathe` or similar
  - Note: once published, applicationId CANNOT be changed

### 📄 Legal & Privacy (REQUIRED for login + data collection)
- [ ] Write a Privacy Policy (required — the app collects email, health/breathing data)
  - Host it at a public URL, e.g. https://breatheonline.app/privacy
- [ ] Add Privacy Policy URL in Play Console → App Content → Privacy Policy
- [ ] If Health Connect is added in future: complete Health Connect declaration

### 📊 Play Console — App Content
- [ ] Complete Content Rating questionnaire (IARC)
  - App is non-violent, wellness — should get Everyone or 3+
- [ ] Complete Data Safety form:
  - Data collected: email address (required, account management)
  - Data collected: approximate location (optional, for globe feature)
  - Data collected: health/fitness data — session duration, breathing stats
  - Data shared: none with third parties except Mapbox (location for maps)
  - Data encrypted in transit: YES (HTTPS)
  - User can request data deletion: add this capability or note in privacy policy
- [ ] Complete App Access declaration (the app requires login — provide test credentials)

### 🖼️ Store Listing Assets
- [ ] App icon: 512×512 PNG (high-res icon for store listing)
- [ ] Feature graphic: 1024×500 PNG
- [ ] Screenshots: minimum 2 phone screenshots (1080×1920 or similar)
- [ ] Short description: ≤ 80 characters
- [ ] Full description: ≤ 4000 characters

### ✅ Final Pre-Submit Checklist
- [ ] Bump `versionCode` and `versionName` in `build.gradle.kts`
- [ ] Test release APK on a real device (not emulator)
- [ ] Test: login, breathing session, notification scheduling, navigation
- [ ] Test: app behaviour with no internet connection
- [ ] Test: permission denial (notifications, location) does not crash

---

## 🏗️ How to Build

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (requires signing config in local.properties)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Release AAB — required for Play Store
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
# Upload this file to Play Console → Production → Create new release
```

### Run unit tests
```bash
./gradlew test
# Results: app/build/reports/tests/testDebugUnitTest/index.html
```

---

## 📦 Recommended Next Steps (Post-Launch)
- Add Firebase Crashlytics for production crash reporting
- Add Firebase Analytics for basic usage tracking (optional)
- Implement Health Connect API for richer heart rate data
- Add app review prompt after 5 completed sessions (Play In-App Review API)
