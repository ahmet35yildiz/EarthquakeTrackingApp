# QuakeAlert — Development Plan

Source of truth for **what to do next**. Product rules: `docs/SPEC.md`. Technical design: `docs/ARCHITECTURE.md`.
Decisions: `docs/DECISIONS.md`. Tests: `docs/TESTING.md`. Events: `docs/ANALYTICS.md`.

## How to use this plan (every session)
1. Read `CLAUDE.md`, `CLAUDE.local.md` (if present) and this file; check **Open items** and the **Work log**.
2. Take the first unchecked task in order (dependencies flow top-down). Do not skip ahead to stretch items.
3. Meet the task's acceptance criteria and the definition of done (`docs/TESTING.md` §4).
4. Tick the task, add a work-log row (date, start–end, duration, summary), record any new decision as an ADR,
   update the README draft if the change affects run steps, decisions, scope or limitations.
5. Leave changes uncommitted unless the user asks for a commit.

## Timeline
| Day | Target |
|---|---|
| Fri 2026-09-25 (evening) | Planning ✅, Phase 0 |
| Sat 2026-09-26 | Phase 1 (core + earthquakes list/detail) |
| Sun 2026-09-27 | Phase 2 (alerts, geocoder, worker, notifications, onboarding) |
| Mon 2026-09-28 | Phase 3 (settings, event log, polish, QA matrix) + Phase 4 (docs) |
| Tue 2026-09-29 (morning) | Buffer, final checks. **Everything done by this point.** Stretch only if time remains. |

---

## Phase 0 — Foundation
- [x] **0.1 Toolchain upgrade.** Look up the current stable versions (Google Maven / official release pages) of
  AGP, Gradle wrapper, Kotlin, KSP (matching Kotlin), Compose BOM, and all AndroidX libs; update
  `gradle/libs.versions.toml` and the wrapper. Java 17 toolchain. Replace deprecated `kotlinOptions` with
  `kotlin { compilerOptions { … } }` (and follow AGP 9 migration notes if AGP 9 is the latest stable, e.g. built-in
  Kotlin support).
  *Done when:* `./gradlew assembleDebug testDebugUnitTest lintDebug` passes with no deprecation errors **and** the
  project syncs in Android Studio.
  *Result:* Gradle 9.8.0, AGP 9.4.1 (built-in Kotlin, `kotlin-android` plugin removed), Kotlin 2.4.20, KSP 2.3.12
  (declared, applied in 0.3), Compose BOM 2026.06.01, core-ktx 1.18.0, lifecycle 2.10.0, Java 17. Newer Compose BOM,
  core and lifecycle releases need compileSdk 37 and stay pinned (ADR-015). `kotlinOptions` removed: with built-in
  Kotlin `jvmTarget` follows `compileOptions`. Remaining warning: `Configuration.setVisible` deprecation raised
  inside AGP 9.4.1 itself (not our build scripts). Requires Android Studio Quail 4 (2026.1.4)+.
- [x] **0.2 Rename and base config.** namespace + applicationId `com.ahmetyildiz.quakealert`; move main/test/androidTest
  sources to the new package; `rootProject.name = "QuakeAlert"`; `app_name` = "QuakeAlert"; theme `Theme.QuakeAlert`;
  minSdk 26, target/compile 36; `buildConfig = true`.
  *Done when:* app installs and launches on API 34 emulator under the new id; no `com.example` left (`grep -r`).
  *Result:* theme files moved to `core/ui/theme` (ARCHITECTURE §3), `EarthquakeTrackingAppTheme` → `QuakeAlertTheme`,
  `app_name` marked `translatable="false"`, `mipmap-anydpi-v26` → `mipmap-anydpi` (v26 qualifier redundant with
  minSdk 26). Verified on Pixel_7_API_34.
- [x] **0.3 Dependencies.** Hilt (+ navigation-compose, work), KSP, Room, Retrofit, OkHttp (+ logging, debug only),
  kotlinx.serialization (+ plugin), DataStore Preferences, WorkManager, Navigation Compose, lifecycle-runtime-compose,
  AppCompat, material-icons-extended (or a minimal icon set), test libs (android-junit5 plugin, JUnit 5, MockK,
  coroutines-test, Turbine, work-testing, hilt-android-testing).
  *Done when:* build passes; a sample JUnit 5 test runs in `testDebugUnitTest`.
  *Result:* all libraries at the latest version that supports compileSdk 36 (policy of ADR-015; navigation 2.10,
  androidx.hilt 1.4 and OkHttp 5.5 need compileSdk 37, so pinned: navigation 2.9.8, androidx.hilt 1.3.0, OkHttp
  5.4.0). `hiltViewModel()` comes from `hilt-lifecycle-viewmodel-compose` (moved there from `hilt-navigation-compose`
  in androidx.hilt 1.3). No `room-ktx` (merged into `room-runtime` since Room 2.7).
  Room Gradle plugin exports schemas to `app/schemas`. Icons: `material-icons-core` (ADR-016). Tests: JUnit Jupiter 6
  (ADR-017). Hilt, Room and serialization code generation verified with Kotlin 2.4.20 using temporary sample classes
  (then removed).
- [x] **0.4 App skeleton.** `QuakeAlertApplication` (`@HiltAndroidApp`, `Configuration.Provider` with
  `HiltWorkerFactory`, default WorkManager initializer removed in manifest); `MainActivity : AppCompatActivity`,
  edge-to-edge, `QuakeAlertTheme`, NavHost with 3 placeholder tabs (Earthquakes, Alerts, Settings) in a bottom bar;
  empty package tree per ARCHITECTURE §3.
  *Done when:* tabs switch; rotation keeps the selected tab.
  *Result:* `navigation/` holds routes (`TopLevelRoute`), `TopLevelDestination`, bottom bar, NavHost and the app
  shell; placeholder screens live in each feature's `presentation/screen`. Packages are created by the task that
  first puts a file in them (git does not track empty directories). Tab labels added in EN and TR
  (`values-tr/strings.xml` created early; 0.5 adds the locale infrastructure). Template `colors.xml` removed.
  Verified on API 34: tab switching, selected tab kept across rotation, back from a tab returns to Earthquakes,
  default WorkManager initializer absent from the merged manifest.
- [x] **0.5 i18n infrastructure.** `values-tr/strings.xml`; `generateLocaleConfig = true` +
  `res/resources.properties`; Gradle task that writes `BuildConfig.SUPPORTED_LANGUAGE_TAGS` from `values-*` folders;
  `AppLocalesMetadataHolderService` (`autoStoreLocales=true`); `core/locale/AppLanguageManager`.
  *Done when:* switching EN ↔ TR (temporary debug button or adb) changes placeholder texts on API 34 **and** API 31/32.
  *Result:* the language list is computed when Gradle configures the build (`findSupportedLanguageTags()` in
  `app/build.gradle.kts`, reads `res/resources.properties` + `values-*/strings.xml`) instead of a separate task — same
  outcome, less code. `AppLanguageManager` (interface) + `AppCompatLanguageManager`, bound in `core/di/LocaleModule`.
  Verified with a temporary toggle button (removed afterwards) on API 34 and API 31: instant switch EN ↔ TR, choice
  kept after the app is killed, selected tab kept; on API 34 also via the system per-app language setting.
- [ ] **0.6 Design system.** Light/dark color schemes (dynamic color off), typography, shapes, spacing tokens,
  `LocalSeverityColors`; shared `LoadingState`, `EmptyState`, `ErrorState`, `OfflineBanner`, `MagnitudeBadge` with
  previews.
  *Done when:* previews render in light and dark.
- [x] **0.7 Emulator for API < 33.** The user installs it manually when needed (API 31 or 32, Google APIs,
  arm64). Remind the user before the first task that needs it (0.5 at the latest); do not download images yourself.
  *Done when:* the app launches on it.
  *Result:* AVD `Pixel_6` (API 31, Google APIs, arm64).

## Phase 1 — Core and Earthquakes
- [ ] **1.1 Core basics.** `AppResult`/`AppError`, `Clock` (+ `FakeClock` for tests), dispatcher qualifiers,
  network module (Retrofit + Json `ignoreUnknownKeys`, timeouts), `QuakeAlertDatabase`, DataStore
  `UserPreferencesDataSource` + `UserPreferencesRepository` (ADR-006), `GeoPoint`, `AlertArea`, haversine.
  *Done when:* unit tests for haversine / `AlertArea.contains` / preferences mapping pass.
- [ ] **1.2 Analytics core.** `AnalyticsEvent` sealed class (all events of ANALYTICS.md §2), `AnalyticsTracker`,
  `LocalAnalyticsTracker` (Room + Logcat), DAO, Hilt binding.
  *Done when:* unit test verifies events are persisted with params; `app_opened` logged from `MainActivity`.
- [ ] **1.3 Earthquakes data.** GeoJSON DTOs, `UsgsApi` (query + eventid), mapper, `EarthquakeEntity` + DAO,
  `EarthquakeRepositoryImpl` (refresh = replace cache in a transaction, observe, get by id with network fallback,
  `fetchUpdatedSince` for alerts).
  *Done when:* mapper + repository unit tests pass (null mag/place, 404, failure keeps cache).
- [ ] **1.4 Earthquakes domain.** `Earthquake`, `EarthquakeRepository`, `EarthquakeQuery`, use cases:
  `ObserveRecentEarthquakesUseCase` (filters + distance), `RefreshEarthquakesUseCase`, `GetEarthquakeUseCase`.
  *Done when:* filter/distance unit tests pass.
- [ ] **1.5 List screen.** ViewModel + `EarthquakeListUiState`, list items, region/magnitude chips, pull-to-refresh,
  stale-cache refresh (> 5 min), loading/empty/error/offline states, analytics (viewed, refreshed, filter changed).
  *Done when:* SPEC §4.2 fully met on emulator incl. airplane mode; ViewModel tests pass.
- [ ] **1.6 Detail screen.** ViewModel, all fields of SPEC §4.3, maps/USGS/share actions, not-found state,
  analytics (viewed with source, action clicked).
  *Done when:* SPEC §4.3 met; opening an unknown id shows not-found.
- [ ] **1.7 Navigation.** Type-safe routes, detail route with `navDeepLink` `quakealert://earthquake/{id}`, back
  behaviour to list, onboarding/main graph split (onboarding placeholder until 2.7).
  *Done when:* `adb shell am start -d "quakealert://earthquake/<id>"` opens the detail.

## Phase 2 — Alerts
- [ ] **2.1 Alert domain.** `AlertConfig` (threshold range 2.5–8.0 step 0.5, default 4.5; radius options
  50/100/250/500/1000, default 250; check interval 15 min; max event age 6 h; overlap 10 min; notified-id retention
  30 days; max individual notifications 3), `AlertMatcher` implementing SPEC §5.1.
  *Done when:* every rule in SPEC §5.1 has a unit test (incl. boundaries).
- [ ] **2.2 City search (Geocoder).** `CitySearchRepository` (domain), `AndroidCityGeocoder` with the API 33 split
  (ARCHITECTURE §4.3), `SearchCitiesUseCase`, country list provider (`Locale.getISOCountries()`, localized, Collator
  sorted).
  *Done when:* unit tests with a fake geocoder pass **and** "Izmir"/"Tokyo" searches work on **API 31/32 and API 34**;
  `isPresent() == false` path hides city search.
- [ ] **2.3 Area components.** Reusable composables: threshold slider, country picker (searchable), city search
  field + results ("type and search", no live suggestions), radius selector, "Whole world" warning card.
  *Done when:* previews + usable in both onboarding and settings.
- [ ] **2.4 Alert settings screen.** Toggle, threshold, area, warning when no area, permission status row with
  "Open settings", last checked time; saving resets baseline; schedules/cancels work; analytics.
  *Done when:* SPEC §4.4 met; ViewModel tests pass.
- [ ] **2.5 Notifications.** Channel creation at startup, `AlertNotifier` interface + `EarthquakeAlertNotifier`
  (single, up to 3 individual, summary for more; deep link PendingIntent; localized title with distance),
  `NotificationPermissionChecker`, suppressed path, `alert_notification_opened` with delay.
  *Done when:* notification from a test trigger opens the right detail (cold + warm start) on API 31/32 and 34.
- [ ] **2.6 Worker.** `NotifiedEarthquakeRepository` (Room, prune), `CheckForNewAlertsUseCase` (SPEC §5.2),
  thin `AlertCheckWorker` (`@HiltWorker`), `AlertWorkScheduler` (unique periodic, UPDATE policy, network
  constraint, schedule on app start/settings change, cancel on disable).
  *Done when:* use case unit tests pass; `dumpsys jobscheduler` shows the job; "Run check now" logs
  `background_check_completed`; reboot keeps the schedule.
- [ ] **2.7 Onboarding.** Welcome (with not-an-early-warning disclaimer) → alert setup (reuses 2.3) → notification
  permission (API 33+ request; < 33 confirm only) → finish; completion flag; start destination logic; analytics.
  *Done when:* SPEC §4.1 met on API 31/32 and 34; denial path works.
- [ ] **2.8 Developer tools.** Debug-only section: Simulate alert (fake matching event through matcher + notifier),
  Run check now (expedited one-time worker).
  *Done when:* simulate → notification → tap → detail; repeated simulate of the same id → no duplicate.

## Phase 3 — Settings, event log, polish, QA
- [ ] **3.1 Settings screen.** Language picker (System default + generated list, names in their own language),
  about (USGS attribution, disclaimer, version), permission status, developer entries (debug only).
- [ ] **3.2 Event log screen.** List, filter by name, clear, share as text.
- [ ] **3.3 Polish.** Adaptive launcher icon, copy review (EN + TR), accessibility labels, dark mode, landscape,
  font scale, empty/error texts, consistent spacing.
- [ ] **3.4 Instrumented tests.** Compose UI tests for list states, onboarding happy path, alert settings;
  worker test with fakes.
- [ ] **3.5 Full QA matrix.** Every item of `docs/TESTING.md` §3 on API 31/32 and API 34/35; fix findings.

## Phase 4 — Documentation and delivery
- [ ] **4.1 README (Turkish) complete.** All sections filled (see README template), screenshots in `docs/images/`.
- [ ] **4.2 AI usage report.** Fill `docs/AI_USAGE.md` with real `npx ccusage@latest` output for this project's
  sessions (`--since 20260925`, per-model breakdown).
- [ ] **4.3 Translate README to English** (keep it short).
- [ ] **4.4 Final checks.** Clean clone builds; all tests pass; lint clean; wording review of every file and commit
  message; `docs/PLAN.md` keep/remove decision with the user.

## Phase 5 — Stretch (only when Phases 0–4 are done, in this order)
- [ ] **5.1 OneTimeWork chain** (~5 min while device active) on top of periodic work (ADR-003).
- [ ] **5.2 In-app auto refresh** every 60 s while the list is visible.
- [ ] **5.3 GPS-based area** ("Use my location" → center; reverse geocode for the name).
- [ ] **5.4 Alert preview** "In the last 7 days you would have received N alerts" (from cached data).
- [ ] **5.5 GitHub Actions CI** (build + unit tests + lint).

---

## Open items (need the user's decision/approval)
| Item | Status |
|---|---|
| Rewrite the initial commit's author email | ✅ done (4291b49, force-pushed 2026-09-25) |
| API 31/32 emulator | ✅ `Pixel_6` (API 31) installed 2026-09-25 |
| Update Android Studio to Quail 4 (2026.1.4)+ (required by AGP 9.4) | ✅ done 2026-09-25 |
| App display name "QuakeAlert" | ✅ confirmed |
| Keep `docs/PLAN.md` in the repo at delivery | decide in 4.4 |

## Work log
| Date | Time | Duration | Work |
|---|---|---|---|
| 2026-09-25 | 15:30–16:45 | ~1h15 | Requirement analysis, USGS/AFAD/geocoding research, product & architecture decisions, planning docs |
| 2026-09-25 | 16:55–17:20 | ~25m | 0.1 attempt: version research, upgrade built on CLI but IDE sync failed (Studio too old for AGP 9.4); reverted, postponed |
| 2026-09-25 | 17:20–17:30 | ~10m | 0.2 Rename and base config: new package/id, QuakeAlert naming, minSdk 26, BuildConfig; verified on API 34 |
| 2026-09-25 | 17:30–17:45 | ~15m | 0.1 Toolchain upgrade (after Studio update): Gradle 9.8.0, AGP 9.4.1, Kotlin 2.4.20, KSP 2.3.12, Java 17 (ADR-015) |
| 2026-09-25 | 17:45–17:55 | ~10m | 0.3 Dependencies: Hilt, Room, DataStore, WorkManager, Navigation, Retrofit/OkHttp, serialization, test stack (ADR-016, ADR-017) |
| 2026-09-25 | 17:55–18:05 | ~10m | Design brief prompt for the UI design tool (all screens, states, design system) |
| 2026-09-25 | 18:05–18:12 | ~7m | 0.4 App skeleton: Hilt application + WorkManager factory, AppCompat MainActivity, 3-tab navigation |
| 2026-09-25 | 18:12–18:30 | ~18m | 0.5 i18n infrastructure + 0.7 API 31 emulator: generated language list and locale config, AppLanguageManager; EN/TR verified on API 31 and 34 |
