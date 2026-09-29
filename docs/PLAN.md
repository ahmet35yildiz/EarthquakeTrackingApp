# QuakeAlert — Development Plan

Source of truth for **what to do next**. Product rules: `docs/SPEC.md`. Technical design: `docs/ARCHITECTURE.md`.
Decisions: `docs/DECISIONS.md`. Tests: `docs/TESTING.md`. Events: `docs/ANALYTICS.md`.

## How to use this plan (every session)
1. Read `CLAUDE.md`, `CLAUDE.local.md` (if present) and this file; check **Open items** and the **Work log**.
2. Take the first unchecked task in order (dependencies flow top-down). Do not skip ahead to stretch items.
3. Meet the task's acceptance criteria and the definition of done (`docs/TESTING.md` §5).
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
- [x] **0.6 Design system.** Light/dark color schemes (dynamic color off), typography, shapes, spacing tokens,
  `LocalSeverityColors`; shared `LoadingState`, `EmptyState`, `ErrorState`, `OfflineBanner` (renamed
  `StaleDataBanner` in 1.5), `MagnitudeBadge` with previews.
  *Done when:* previews render in light and dark.
  *Result:* tokens taken from the UI design (ADR-018): light scheme = design values, dark scheme generated from the
  same Material 3 palettes (light regeneration matched the design on 34/35 roles), severity scale with a corrected
  dark "major" colour, system Roboto, shapes 4/8/12/16/28, `Spacing` object. `MagnitudeSeverity` (core/model, unit
  tested). Tab icons switched to Material Symbols Rounded drawables. Previews use `@PreviewLightDark`; verified on
  the API 34 emulator in light and dark with a temporary gallery (reverted).
- [x] **0.7 Emulator for API < 33.** The user installs it manually when needed (API 31 or 32, Google APIs,
  arm64). Remind the user before the first task that needs it (0.5 at the latest); do not download images yourself.
  *Done when:* the app launches on it.
  *Result:* AVD `Pixel_6` (API 31, Google APIs, arm64).

## Phase 1 — Core and Earthquakes
- [x] **1.1 Core basics.** `AppResult`/`AppError`, `Clock` (+ `FakeClock` for tests), dispatcher qualifiers,
  network module (Retrofit + Json `ignoreUnknownKeys`, timeouts), `QuakeAlertDatabase`, DataStore
  `UserPreferencesDataSource` + `UserPreferencesRepository` (ADR-006), `GeoPoint`, `AlertArea`, haversine.
  *Done when:* unit tests for haversine / `AlertArea.contains` / preferences mapping pass.
  *Result:* `AlertArea` is sealed (`WholeWorld` | `AroundCity(city, radiusKm)`, edge inclusive); `City` lives in
  `core/model` so city search (2.2) and preferences share it. Preferences: `UserPreferencesRepository` interface +
  one `DataStoreUserPreferencesRepository` (keys and mapping in one class instead of a separate pass-through data
  source); `saveAlertSettings(settings, baselineAt)` always writes the baseline; default threshold 4.5 is
  `AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD` (ADR-019). `safeApiCall` maps exceptions to `AppError`
  (404 → `NotFound`). Only `@IoDispatcher` for now (no other dispatcher is needed yet). HTTP logging is contributed
  by `src/debug` into an interceptor multibinding, so release builds do not reference the debug-only library.
  INTERNET permission added. **`QuakeAlertDatabase` moved to 1.2:** Room rejects a database without entities, so it
  is created together with its first entity. Tests: haversine, `GeoPoint` validation, `AlertArea`, DataStore
  round-trips on a temp file, error mapping. Verified on API 34 and API 31 with temporary injection (reverted): Hilt
  graph resolves, preferences survive an app restart, a USGS request succeeds and is logged in debug.
- [x] **1.2 Analytics core.** `QuakeAlertDatabase` (created here with its first entity, see 1.1),
  `AnalyticsEvent` sealed class (all events of ANALYTICS.md §2), `AnalyticsTracker`, `LocalAnalyticsTracker`
  (Room + Logcat), DAO, Hilt binding.
  *Done when:* unit test verifies events are persisted with params; `app_opened` logged from `MainActivity`.
  *Result:* all 25 events as `AnalyticsEvent` subclasses with typed params (enum values in
  `AnalyticsParameters.kt`); `list_filter_changed` is two classes (`RegionFilterChanged`, `MagnitudeFilterChanged`)
  so a filter can never be logged with the other filter's value. `track()` is fire-and-forget: the timestamp is
  taken at call time, the insert runs on the new `@ApplicationScope` (SupervisorJob + IO, now also used by DataStore)
  and a storage failure is logged, never thrown. Params are stored as a JSON object through a Room
  `TypeConverter`. `QuakeAlertDatabase` v1, schema exported (ADR-020); the leftover sample schema from 0.3 removed.
  `app_opened` (source launcher) is logged only when `MainActivity` is created without saved state, so rotation and
  language switches do not count; the notification source comes with 2.5. Tests: dictionary test for every event
  (names and params match ANALYTICS.md), tracker with a fake DAO, converter, and an instrumented DAO test on
  in-memory Room (passed on API 31 and 34). Verified on API 31 and 34: rows in `analytics_events` with params,
  Logcat `Analytics`, no event on rotation.
- [x] **1.3 Earthquakes data.** GeoJSON DTOs, `UsgsApi` (query + eventid), mapper, `EarthquakeEntity` + DAO,
  `EarthquakeRepositoryImpl` (refresh = replace cache in a transaction, observe, get by id with network fallback,
  `fetchUpdatedSince` for alerts).
  *Done when:* mapper + repository unit tests pass (null mag/place, 404, failure keeps cache).
  *Result:* the domain types the repository implements (`Earthquake` with a nullable `Magnitude(value, type)`,
  `EarthquakeQuery`, `EarthquakeRepository`) were created here; 1.4 keeps the use cases. Repository API:
  `observeCachedEarthquakes`, `refreshCache(query)`, `getEarthquake(id)`, `fetchEarthquakes(query)` (alerts pass
  `updatedAfter`, replacing the planned `fetchUpdatedSince`). No separate remote/local data source classes: the
  repository uses `UsgsApi` and `EarthquakeDao` directly (ADR-021). USGS `updated` is not stored (nothing uses it).
  Query numbers are formatted with `Locale.ROOT` (a Turkish device locale would otherwise send "4,5"). Database v2
  with an `@AutoMigration` from v1 (ADR-020). Tests: DTO parsing from real-shaped JSON fixtures, mapper (null
  mag/place/felt, negative depth, non-earthquake, invalid coordinates), query parameters, repository with fakes
  (refresh replaces cache, failure keeps cache, cache-first detail, network fallback, 404 and non-earthquake →
  `NotFound`), instrumented `EarthquakeDao` test (passed on API 31 and 34). Verified on API 31 and 34 with temporary
  injection (reverted): v1 → v2 upgrade kept existing analytics rows, live refresh cached 322 earthquakes, cache
  lookup, unknown id → `NotFound`, 1000 km around Tokyo returned only Japanese events.
- [x] **1.4 Earthquakes domain.** Use cases (the domain types exist since 1.3): `ObserveRecentEarthquakesUseCase`
  (filters + distance), `RefreshEarthquakesUseCase` (builds the 7-day M2.5+ query from an `EarthquakesConfig`,
  stores `lastRefreshedAt`), `GetEarthquakeUseCase`.
  *Done when:* filter/distance unit tests pass.
  *Result:* `EarthquakesConfig` (7 days, M2.5, stale after 5 min). Filters: `EarthquakeFilters(region: WORLD |
  NEAR_CITY, magnitude: ALL | ABOVE_THRESHOLD)`; "above threshold" includes equal magnitudes and drops unknown ones;
  "near city" includes the radius edge and falls back to world when no area is set. The observe use case returns
  `RecentEarthquakes` with everything the list needs (items with distance, applied filters, cached count for the
  "filtered empty" vs "no data" message, area and threshold for chip labels, last refresh for the offline banner).
  Added `CheckCacheFreshnessUseCase` (MISSING / STALE / FRESH, stale strictly after 5 min) so the list can log
  initial vs stale refreshes. The list refresh is always worldwide, whatever the user's area. `GetEarthquakeUseCase`
  adds the distance to the user's city. Tests with fake repositories (boundaries: threshold equal, 1 m inside/outside
  the radius, 300 s vs 301 s). Verified on API 34 with temporary injection (reverted): Hilt builds all use cases;
  live data filtered to Izmir + 1000 km, M4.0+ returned only Turkish and Greek events with plausible distances.
- [x] **1.5 List screen.** ViewModel + `EarthquakeListUiState`, list items, region/magnitude chips, pull-to-refresh,
  stale-cache refresh (> 5 min), loading/empty/error/offline states, analytics (viewed, refreshed, filter changed).
  *Done when:* SPEC §4.2 fully met on emulator incl. airplane mode; ViewModel tests pass.
  *Result:* `EarthquakeListViewModel` (filters, refresh on start when missing/stale, pull/button/retry refresh,
  analytics incl. `count`), `EarthquakeListUiState` with a derived `content`; `refreshCache` now returns the stored
  count. Screen: top bar with scope + "Updated x ago" and a refresh button (accessible alternative to the pull
  gesture), stale-data banner (offline vs. failed wording), filter chips in two wrapping groups (region / magnitude;
  a scrolling row hid the threshold chip in Turkish), count, cards (badge, place, relative time, depth, distance),
  loading / error / empty / filtered-empty states. Design followed for visual language; notification banner, search,
  profile and day headers left out. Relative times use own plurals (`DateUtils` ignores the in-app language).
  Found and fixed on the emulator: the offline banner inserted as the first lazy item was scrolled out of view, and
  the filtered-empty state inside the lazy list crashed (scrollable in infinite height) — both now sit outside the
  list, and a Compose UI test renders every content state. Tests: 11 ViewModel, 8 relative time, 7 Compose UI
  (API 31 and 34). Verified on API 34 (EN/TR, light/dark, landscape, airplane mode, filters, rotation logs no extra
  view) and API 31 (list, offline banner). Item tap is wired in 1.6/1.7.
- [x] **1.5.1 List sorting + list performance check** (added on request, not in the original plan). Sort menu
  (newest / largest / nearest first) next to the count, above the list; `list_sort_changed` event and `sort` on
  `earthquake_list_viewed`.
  *Done when:* each order is correct incl. ties and unknown values; the cost of showing the full list is measured.
  *Result:* measured on API 31 and 34 with temporary logging (reverted): ~7 cards composed at any time for a
  321-item list, also after scrolling to the end; mapping + filter + sort 1–13 ms per change, previously on the main
  thread. Decision (ADR-023): no paging — an in-memory "load 10 more" step started earlier was removed as it saved
  nothing — and the list computation moved to `@DefaultDispatcher`. Tests: 5 sort cases in the use case, 3 in the
  ViewModel, 2 Compose UI tests for the menu (API 31 and 34). Verified: nearest / largest order with real data, scroll
  back to top on a new order, position kept on rotation, EN/TR labels. One run showed the area chip missing right
  after install; not reproducible in 14 runs (live preference updates confirmed), most likely read before the save
  finished on a busy first launch.
- [x] **1.6 Detail screen.** ViewModel, all fields of SPEC §4.3, maps/USGS/share actions, not-found state,
  analytics (viewed with source, action clicked).
  *Done when:* SPEC §4.3 met; opening an unknown id shows not-found.
  *Result:* `GetEarthquakeUseCase` now returns `EarthquakeDetails` (distance card: km from the city + inside/outside
  the alert area, using `AlertArea.contains`). `EarthquakeDetailViewModel` with assisted injection (ADR-024), states
  loading / loaded / not found / error + retry, `earthquake_detail_viewed` once per opened detail and only on success.
  Screen: header card, distance card (only with an area), facts (magnitude + type, local time with offset, UTC,
  depth, coordinates with localized hemispheres, review status, tsunami flag, felt reports if any), open in maps,
  view on USGS, share (chooser, localized plain text), data note. The design's map, MMI and copy button left out
  (SPEC). Detail route added so the list opens it (`EarthquakeDetailRoute(id, isFromNotification)`; deep link stays
  in 1.7); bottom bar hidden on pushed screens; screen composables renamed `XEntry` to stop clashing with `XRoute`
  keys. Lint led to `toUri()` and to a Boolean route argument instead of an analytics enum (R8 keep issue). Tests:
  6 use case, 5 ViewModel, 6 Compose UI (API 31 and 34). Verified on API 34 and 31: list → detail, share chooser,
  Google Maps, USGS page in Chrome, back, rotation (no second view event), TR (decimal comma, K/D, TR dates), dark,
  distance card, unknown id → real USGS 404 → not found, offline uncached → error → retry, offline cached → loads,
  Maps disabled → "No app found" snackbar (Maps re-enabled afterwards). Emulators were restarted once: their DNS
  had stopped working after the host slept.
- [x] **1.7 Navigation.** Type-safe routes, detail route with `navDeepLink` `quakealert://earthquake/{id}`, back
  behaviour to list, onboarding/main graph split (onboarding placeholder until 2.7). The detail route itself exists
  since 1.6 (`EarthquakeDetailRoute(earthquakeId, isFromNotification)`); a deep link must also land on the list
  when going back.
  *Result:* onboarding / main graphs, start graph from `StartDestinationViewModel` (read once, so finishing
  onboarding never resets the graph), onboarding placeholder in `alerts/presentation` (welcome + "Get started" →
  saves the flag), tabs pop to the main graph, bottom bar only on tabs. Deep link `navDeepLink` + manifest filter +
  `singleTop`. Found by reading Navigation 2.9.8's source: `handleDeepLink(intent)` restarts the whole task when the
  intent has `NEW_TASK` without `CLEAR_TASK` (every `am start` and notification tap) — the activity would be created
  twice and `app_opened` logged twice. A first version handled links after the first frame instead, which briefly
  showed the list and logged `earthquake_list_viewed`; final version marks our deep-link intents `CLEAR_TASK` so the
  library builds the stack in place before anything is drawn (ADR-025). Tests: 3 start destination unit tests.
  Verified on API 34 and 31: first run → onboarding → list, back exits, relaunch → list; cold deep link (one
  activity, source = notification, no list view event), rotation (not re-handled), back → list → exit; warm deep
  link from the Alerts tab (same process, no second `app_opened`); unknown id → not found. Deep link before
  onboarding: detail, then list, then onboarding (documented, not blocked).
  *Done when:* `adb shell am start -d "quakealert://earthquake/<id>"` opens the detail.

## Phase 2 — Alerts
- [x] **2.1 Alert domain.** `AlertConfig` (threshold range 2.5–8.0 step 0.5, default 4.5 = reuse
  `AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD` from core, ADR-019; radius options
  50/100/250/500/1000, default 250; check interval 15 min; max event age 6 h; overlap 10 min; notified-id retention
  30 days; max individual notifications 3), `AlertMatcher` implementing SPEC §5.1.
  *Done when:* every rule in SPEC §5.1 has a unit test (incl. boundaries).
  *Result:* `AlertConfig` and `AlertMatcher` in `alerts/domain`; the matcher is pure and takes one
  `AlertMatchCriteria` (settings, baseline, notified ids, check time) so 2.6 and 2.8 share it (ADR-026). All limits
  inclusive; a missing baseline (settings never saved) matches nothing. Intervals are `Duration`s
  (`CHECK_INTERVAL`, `MAX_EVENT_AGE`, …). 23 unit tests: every rule with its boundary (±0.01 magnitude, ±1 m radius,
  ±1 s baseline and max age), plus config consistency (default on a step, default radius among the options).
- [x] **2.2 City search (Geocoder).** `CitySearchRepository` (domain), `AndroidCityGeocoder` with the API 33 split
  (ARCHITECTURE §4.3), `SearchCitiesUseCase`, country list provider (`Locale.getISOCountries()`, localized, Collator
  sorted).
  *Done when:* unit tests with a fake geocoder pass **and** "Izmir"/"Tokyo" searches work on **API 31/32 and API 34**;
  `isPresent() == false` path hides city search.
  *Result:* domain: `Country`, `CitySearchQuery(name, country, locale)`, `CitySearchRepository`,
  `SearchCitiesUseCase` (trims, blank → no request, `isAvailable()`), `GetCountriesUseCase(displayLocale)` (ISO list,
  localized names, `Collator` order). Data: `CityGeocoder` interface + `AndroidCityGeocoder` (the only class that
  touches `android.location`, API 33 split), `CitySearchRepositoryImpl` owns the rules and is tested with a fake
  geocoder (ADR-027). The geocoder is asked for "name, country name" in the app language, which fixed "Paris" in the
  US and returns localized admin areas. Found on the emulator: a nonsense name or a city of another country then
  returned the country itself as a "city" — country-level results (no locality or admin area) are now dropped
  unless their name equals the typed name (keeps city states such as Singapore and Monaco, typed in the app
  language). Max results in `AlertConfig.CITY_SEARCH_MAX_RESULTS` (10). Tests: 26 unit tests (mapper, repository with
  fake geocoder: availability, country filter, de-duplication, country-level results, error mapping; use cases incl.
  Turkish collation). Verified on API 34 (async path) and API 31 (blocking path) with temporary injection (reverted):
  "Izmir"/"İzmir" → İzmir, "Tokyo" → Tokyo, Paris US → Texas/Illinois/Kentucky, nonsense → empty, airplane mode →
  `Network`; 253/249 countries, TR names sorted with Turkish rules; first request ~1–2.5 s, then ~0.1–0.5 s. The
  hidden-search path is exposed through `SearchCitiesUseCase.isAvailable()` (unit tested); the UI part is in 2.3.
- [x] **2.3 Area components.** Reusable composables: threshold slider, country picker (searchable), city search
  field + results ("type and search", no live suggestions), radius selector, "Whole world" warning card.
  From 2.2: city search hidden when `SearchCitiesUseCase.isAvailable()` is false; default country = device region
  (SPEC §4.4); rebuild the country list when the app language changes; log `city_search_performed` /
  `city_search_failed` from the ViewModel.
  *Done when:* previews + usable in both onboarding and settings.
  *Result:* stateless composables in `alerts/presentation/component`: `MagnitudeThresholdSelector` (stepped slider,
  M badge, range labels, hint; reports the value when the drag ends), `AreaSelector` (Whole world / Near a city
  segmented choice, `WholeWorldWarningCard`, `SelectedCityCard` with Change, `CitySearchPanel` with
  `CountryPickerField` + full-screen searchable `CountryPickerDialog` (flags, accent-insensitive filter),
  `RadiusSelector`), plus the shared `SectionCard` in `core/ui`. The caller owns the choice as an `AreaSelection`
  (mode, city, radius; `toAlertAreaOrNull()` is null while "Near a city" has no city); `AreaSelectorEntry` wires the
  shared `CitySearchViewModel` (countries per app language, default = device region from the new
  `DeviceRegionProvider`, search states, analytics) — ADR-028. The design's dark tertiary container made the
  warning card unreadable in light mode, so it uses the stale-data banner style. Tests: 39 unit tests (ViewModel,
  area selection, country filter, slider steps, flags) and 9 Compose UI tests (passed on API 31 and 34). Verified on
  API 34 and 31 with a temporary wiring into the Alerts tab (reverted): default country from the device region,
  "turk" → Türkiye / Turkmenistan / Turks & Caicos, keyboard search, İzmir picked, radius, Change → Cancel, slider
  tap → 6.5, `city_search_performed` logged, TR + dark (decimal comma, TR texts), airplane mode → network message →
  Retry → Ankara, landscape scrolls; on API 31 the country is "Turkey" (older platform data).
- [x] **2.4 Alert settings screen.** Toggle, threshold, area, warning when no area, permission status row with
  "Open settings", last checked time; saving resets baseline; schedules/cancels work; analytics.
  From 2.3: use `MagnitudeThresholdSelector` + `AreaSelectorEntry`; keep the `AreaSelection` in the ViewModel (it
  must survive rotation and language changes); save only when `toAlertAreaOrNull()` is not null; log
  `alert_threshold_changed` / `alert_area_set` / `alert_area_cleared` from the ViewModel.
  *Done when:* SPEC §4.4 met; ViewModel tests pass.
  *Result:* `AlertSettingsEntry` / `AlertSettingsScreen` on the Alerts tab: summary card with the alerts switch and a
  plain-language summary of the saved settings, threshold, area (2.3 components), status card (notification
  permission with "Open settings" → the app's system notification page, last background check, check interval).
  Every change is saved at once through `UpdateAlertSettingsUseCase` (serialized, new baseline on every saved change,
  no save when nothing changed) with an "Alert settings saved" snackbar; analytics come from the difference between
  the previous and the saved settings (`toAnalyticsEvents(context)`, reusable by onboarding) — ADR-029. "Near a city"
  without a city stays on screen only. `NotificationPermissionChecker` (core/notification,
  `areNotificationsEnabled()`) is re-read on resume. Found on the emulator: `POST_NOTIFICATIONS` was not declared,
  so on API 33+ the system settings could not allow notifications at all — declared now (planned for 2.5). The
  "schedules/cancels work" part moves to 2.6 (the worker and scheduler do not exist yet). Tests: 18 unit tests (use
  cases, analytics mapping, ViewModel) and 6 Compose UI tests (passed on API 31 and 34). Verified on API 34 and 31:
  İzmir picked in Alerts → "Near İzmir" chip and distances in the Earthquakes tab, kept after the app is killed,
  radius / switch saved with the right analytics, Open settings → system page → permission granted → row updates on
  return, TR texts; API 31 shows notifications allowed by default.
- [x] **2.5 Notifications.** Channel creation at startup, `AlertNotifier` interface + `EarthquakeAlertNotifier`
  (single, up to 3 individual, summary for more; deep link PendingIntent; localized title with distance),
  `NotificationPermissionChecker` (exists since 2.4, as does the `POST_NOTIFICATIONS` declaration), suppressed
  path, `alert_notification_opened` with delay, `app_opened` with
  source = notification. The notification `PendingIntent` uses the deep link
  `quakealert://earthquake/{id}?isFromNotification=true` (already handled since 1.7).
  *Done when:* notification from a test trigger opens the right detail (cold + warm start) on API 31/32 and 34.
  *Result:* channel "Earthquake alerts" (high importance) registered in `QuakeAlertApplication.onCreate`.
  `NotifyAlertsUseCase` (domain) owns the policy: nothing to show / permission missing → suppressed event / up to 3 →
  one notification per earthquake (oldest first) / more → one summary; one `alert_notification_posted` per shown
  notification (summary: `event_id=summary`, largest magnitude, batch size) so the open rate stays per notification
  (ADR-030). `EarthquakeAlertNotifier` only renders (`AlertNotificationBuilder`): title "M5.2 earthquake · 15,851 km
  from İzmir" (distance only with an area, from the shared `AlertArea.distanceFromCityOrNull`, also used by the
  detail), text place · local time, inbox-style summary, texts from `LocalizedContextProvider` (app language).
  Tap intents carry `AlertNotificationTap` extras (event id + posting time); the detail uses the deep link from
  `DeepLinkConfig` (moved from `navigation` to `core/navigation` so the feature can build it). `AppOpenTracker`
  (singleton) logs `app_opened` with source notification and `alert_notification_opened` with the delay. Found on the
  emulator: after process death a tapped notification restored the *previous* screen (the new intent arrived before
  the Compose `OnNewIntentListener` existed) and `app_opened` was not logged — new intents now go through a pending
  deep-link state consumed by the NavHost, and restored-after-process-death opens are tracked (ADR-031). Lint led to
  disabling language splits in app bundles (ADR-032) and to one permission-checked `post()`. Tests: 17 unit tests
  (notify policy, app-open tracking), 5 notifier instrumented tests; full instrumented suite 41/41 on API 31 and 34.
  Verified with a temporary trigger (reverted) on API 34 and 31: warm tap, cold tap (activity finished + process
  killed), tap after process death with a restored stack, summary for 5, back → list, rotation after a deep link logs
  nothing, TR texts ("M5,2 deprem · İzmir merkezine 15.851 km", 24 h time). Android bundles 4+ notifications of an app
  automatically; tapping that system group opens the app normally.
- [x] **2.6 Worker.** `NotifiedEarthquakeRepository` (Room, prune), `CheckForNewAlertsUseCase` (SPEC §5.2),
  thin `AlertCheckWorker` (`@HiltWorker`), `AlertWorkScheduler` (unique periodic, UPDATE policy, network
  constraint, schedule on app start/settings change, cancel on disable). From 2.4: settings changes go through
  `UpdateAlertSettingsUseCase` — schedule / cancel from there after a saved change. From 2.5: notify through
  `NotifyAlertsUseCase` (returns POSTED / SUPPRESSED / NOTHING_TO_NOTIFY); decide whether suppressed matches are
  stored as notified; verify on API 31 that notification texts follow the in-app language when the worker runs in a
  process without an activity (`LocalizedContextProvider` reads AppCompat's locales).
  *Done when:* use case unit tests pass; `dumpsys jobscheduler` shows the job; "Run check now" logs
  `background_check_completed`; reboot keeps the schedule.
  *Result:* Room `notified_earthquakes` (DB v3, auto-migration; only the candidate ids are queried) behind
  `NotifiedEarthquakeRepository`. `CheckForNewAlertsUseCase` implements SPEC §5.2: skipped until alert settings were
  saved or while disabled; query = last 6 h, threshold, area, `updatedAfter = (lastCheckedAt ?: baseline) − 10 min`;
  `AlertMatcher` with the stored ids → `NotifyAlertsUseCase`; only posted matches are remembered (suppressed ones can
  still alert within 6 h once allowed); prune after 30 days; `lastCheckedAt` = check start; network/server errors →
  retry, parsing/unknown → failure (ADR-033). Thin `@HiltWorker AlertCheckWorker`; `AlertWorkScheduler` (unique
  periodic 15 min, network, UPDATE) behind `AlertCheckScheduler`; `SyncAlertScheduleUseCase` runs on app start and
  after every saved settings change. Found on API 31: in a worker-only process the notification came in English
  although the app language was Turkish (AppCompat loads the per-app locale only when an activity starts) — the
  language manager now also stores the tag and the localized context falls back to it below API 33 (ADR-034).
  Tests: 20 unit tests (check use case, schedule sync, settings update → schedule), DAO + scheduler instrumented tests;
  full instrumented suite 47/47 on API 31 and 34. Verified: v2 → v3 upgrade keeps analytics rows; enabling alerts
  runs a first check at once (`background_check_completed`, fetched 1 / matched 0) and `dumpsys jobscheduler` shows the
  job with the network constraint; the job survives a reboot; in a killed-process worker run on API 31 (temporary
  preparation code, reverted) 7 matches → one Turkish summary ("7 yeni M4,5+ deprem"), next runs matched 0. A forced
  run of the periodic job before its time is postponed by WorkManager, so manual checks need "Run check now" (2.8).
- [x] **2.7 Onboarding.** Welcome (with not-an-early-warning disclaimer) → alert setup (reuses 2.3) → notification
  permission (API 33+ request; < 33 confirm only) → finish; completion flag; start destination logic; analytics.
  Graph, start destination and completion flag exist since 1.7; replace the placeholder `OnboardingScreen`.
  Alert setup step: same components and `AreaSelection` handling as 2.4. From 2.6: finishing must save the settings
  through `UpdateAlertSettingsUseCase` (sets the baseline and schedules the check); until then no check runs.
  *Done when:* SPEC §4.1 met on API 31/32 and 34; denial path works.
  *Result:* one onboarding route with three pages kept in `OnboardingViewModel` (page and threshold in
  `SavedStateHandle`; system Back and the top arrow go to the previous page): welcome (hero, 3 feature cards, the
  not-an-early-warning disclaimer), alert setup (the 2.3 components; Next disabled with a hint while "Near a city" has
  no city), notifications (API 33+: Allow / Not now → system dialog; denied → message, Finish, Open settings; below
  33: status only, Finish). Finish goes through `CompleteOnboardingUseCase`, which always saves the settings with a
  baseline (even unchanged defaults), sets the completion flag and syncs the schedule (ADR-035). Analytics:
  `onboarding_started`, each `onboarding_step_viewed`, permission requested / result, the settings difference with
  `context=onboarding`, `onboarding_completed`. The page title "Allow notifications" duplicated the button label, so
  it became "Get notified". Tests: 13 unit tests (use case, ViewModel), 6 Compose UI tests; full instrumented suite
  53/53 on API 31 and 34. Verified on API 34: allow path (İzmir → list with "Near İzmir", job scheduled, first check
  logged, full event sequence), deny path (message, Finish, Alerts shows permission off, job still scheduled), back
  button and rotation keep the page, TR texts; API 31: no dialog, "Notifications allowed", Finish, job scheduled.
- [x] **2.8 Developer tools.** Debug-only section: Simulate alert (fake matching event through matcher + notifier),
  Run check now (expedited one-time worker).
  From 2.5: simulate through `NotifyAlertsUseCase` (same policy, analytics and tap handling as real alerts).
  From 2.6: "Run check now" = expedited `OneTimeWorkRequest<AlertCheckWorker>` (a forced periodic job is postponed).
  *Done when:* simulate → notification → tap → detail; repeated simulate of the same id → no duplicate.
  *Result:* "Developer tools" card on the Settings tab, shown only in debug builds (`SettingsScreen` gets it as a
  slot from `QuakeAlertNavHost`, so `settings` does not depend on `alerts`). Simulate alert: `SimulateAlertUseCase`
  builds an event that matches the saved settings (now, threshold + 0.5, 20 km from the city or 0°/0° for the whole
  world, localized "Simulated earthquake (test)"), adds it to the cache so the detail opens, and delivers it through
  the new `DeliverAlertsUseCase` — the same match → notify → remember step the background check now uses. "Simulate
  the same alert again" takes the newest simulated event from the cache (an in-memory reference was lost when the
  notification deep link rebuilt the back stack) and shows "Already notified" when nothing is posted. Run check now
  = `AlertCheckScheduler.runCheckNow()`: a unique one-time `AlertCheckWorker` request with the network constraint —
  not expedited, because expedited work below API 31 needs a foreground notification in the worker (ADR-036).
  Outcome messages for alerts off, notifications off, nothing to repeat and check started. Tests: 17 unit tests
  (deliver, simulate, ViewModel); full suites 336 unit, 53/53 instrumented on API 31 and 34. Verified on API 34 and
  31: simulate → "M5.0 earthquake" → tap → detail of the simulated event → back → simulate again → "Already
  notified", no new notification; run check now → `background_check_completed`.
  *Follow-up (requested):* the simulation takes a magnitude, a distance from the selected city (hidden for the whole
  world) and a delay; "Schedule" enqueues `SimulatedAlertWorker` with these values, which runs even with the app
  closed and goes through the same delivery step, so a matching event is notified and a non-matching one is not
  (`outcome=not_matched`). `developer_simulated_alert` now carries `outcome` and `scheduled` (ADR-037). Tests: +13 unit
  tests (344 unit, 53/53 instrumented on API 31 and 34). Verified with the app process killed: API 34 (whole world,
  M4.5+) M5.0 → notification, M3.0 → no notification; API 31 (İzmir 250 km) 50 km → "M5.0 earthquake · 50 km from
  İzmir", 400 km → no notification; runs arrived 0–1 min after the chosen time.

## Phase 3 — Settings, event log, polish, QA
- [x] **3.1 Settings screen.** Language picker (System default + generated list, names in their own language),
  about (USGS attribution, disclaimer, version), permission status, developer entries (debug only).
  From 2.8: `SettingsScreen` already has a top bar, a scrolling column and the debug-only `developerTools` slot
  filled by `DeveloperToolsEntry`; add the event log entry there (3.2).
  *Result:* `SettingsEntry` / `SettingsScreen` (Scaffold, snackbar) with four cards: Language (current choice → radio
  dialog "System default" + generated languages in their own names, applied at once), Notifications (permission
  status, "Open settings" → the app's system notification page), About (USGS attribution + "Visit the USGS website",
  not-an-early-warning note, version from `BuildConfig`), then the debug-only developer slot. `SettingsViewModel`
  reads `AppLanguageManager` / `NotificationPermissionChecker` directly (no settings domain), re-reads both on resume
  and logs `language_changed` only for a real change. The permission row moved to `core/ui/component`
  (`NotificationPermissionStatus`, also used by Alerts) and the outgoing intents to `core/navigation/ExternalIntents`,
  so `settings` depends on `core` only; `notification_permission_requested` stays onboarding-only (ADR-038). New
  `ic_language` (Material Symbols). Tests: 5 unit tests (ViewModel), 5 Compose UI tests; full suites 349 unit,
  58/58 instrumented on API 31 and 34. Verified on API 34 and 31: EN → Türkçe at once with the Settings tab kept and
  `language_changed {from=system, to=tr}`, choice kept after the app is killed, dialog kept across rotation, back to
  System default (`from=tr, to=system`); API 34: per-app language changed in the system (`cmd locale`) shows on
  return, "Open settings" → system page → permission granted → row updates on return, USGS link opens the browser,
  dark mode.
  *Follow-up (requested):* the developer tools moved to their own screen so the Settings tab stays short: a
  "Developer tools" entry below About (debug builds) opens `DeveloperToolsRoute` (pushed, no bottom bar, back arrow)
  with the alert testing card (renamed "Alert testing", the screen carries the "Developer tools" title) and, since 3.2,
  the event log entry. The Settings screen takes `onOpenDeveloperTools` instead of the card slot (ADR-039). Tests: +2
  Settings and 2 developer screen Compose UI tests. Verified on API 34: entry → screen → back to Settings.
- [x] **3.2 Event log screen.** List, filter by name, clear, share as text.
  *Result:* new `eventlog` feature over the `analytics_events` table: `EventLogRepositoryImpl` (+ `deleteAll()` on the
  DAO), `LoggedEvent`, `filterByName` (trimmed, case-insensitive "contains"), `EventLogText` (one line per event: local
  ISO-8601 time with offset, name, `key=value` params), observe / clear use cases, `EventLogViewModel` (query in
  `SavedStateHandle`). Screen from the developer tools screen: filter field with clear button, "N events" / "N of M
  events", newest-first cards with local date-time (seconds) and monospace param chips, live updates; Share sends the
  visible events through the share sheet; Clear asks for confirmation; empty and no-match states; share / clear
  disabled when there is nothing to act on (ADR-039). `shareIntent` moved to `core/navigation/ExternalIntents`.
  Tests: 12 unit tests (filter, text, repository, ViewModel), 8 Compose UI tests + 1 DAO test; full suites 361 unit,
  71/71 instrumented on API 31 and 34. Verified on API 34: onboarding events listed live (9 → 11), "LIST" → "2 of 11
  events", query kept across rotation, share sheet with the filtered lines, "zzz" → no-match state with share
  disabled; API 31: clear → confirmation "9 recorded events will be deleted" → empty state, back → developer tools →
  Settings, TR texts and date format ("26.09.2026 21:33:12").
- [x] **3.3 Polish.** Adaptive launcher icon, copy review (EN + TR), accessibility labels, dark mode, landscape (list:
  header + bottom bar leave room for ~1 card; consider scrolling the header away or a navigation rail),
  font scale, empty/error texts, consistent spacing.
  *Result:* launcher icon from the design as vector adaptive layers (teal background; rings + seismograph line scaled
  into the safe zone; monochrome = foreground) and the unused template bitmaps removed (minSdk 26). Landscape / large
  text: list controls scroll away with the list, navigation rail from 600 dp window width, one-line tab and segmented
  labels (ADR-040). Copy review: Turkish uses "alarm" for the feature everywhere (developer texts said "uyarı"; "erken
  uyarı" stays), one name for USGS, smoother TR sentences, two EN fixes. Accessibility: screen titles are headings
  (`ScreenTitle`), city results have a button role; all remaining `contentDescription = null` icons are decorative
  next to text. Event log item spacing aligned with the earthquake list. Lint's `ConfigurationScreenWidthHeight`
  fixed by using the window size. Tests: 361 unit, 71/71 instrumented on API 31 and 34 (the first API 31 run crashed
  in the emulator's RenderThread — native SIGSEGV in libhwui — and passed on rerun). Verified on API 34: icon in the
  launcher, landscape list with the rail (2+ cards), Alerts in landscape, font scale 2.0 (list, Alerts, Settings,
  detail, tab label "Earthqua…"), dark mode (list, developer tools, event log); API 31: icon, landscape with the rail
  next to the 3-button navigation.
- [x] **3.4 Instrumented tests.** Compose UI tests for list states, onboarding happy path, alert settings;
  worker test with fakes.
  *Result:* the list states, alert settings and onboarding pages already had Compose UI tests (1.5, 2.4, 2.7); added
  what was missing: Hilt instrumented setup (`HiltTestRunner`, `@TestInstallIn` in-memory Room, per-test DataStore,
  `FakeUsgsApi`) (ADR-041); `OnboardingFlowTest` end to end through `MainActivity` (first launch → welcome → setup →
  notifications → Finish → list from the fake USGS, onboarding flag + baseline saved, periodic check enqueued; a
  completed onboarding opens on the list); `AlertCheckWorkerTest` with `HiltWorkerFactory`, fake USGS and notifier
  (skipped before settings, notified once over two runs, older than the baseline ignored, network → retry, parsing →
  failure, analytics recorded). Template `ExampleInstrumentedTest` removed. Full suites: 361 unit, 77/77 instrumented
  on API 31 and 34.
- [x] **3.5 Full QA matrix.** Every item of `docs/TESTING.md` §3 and the scenarios of §4 on API 31/32 and API 34/35; fix findings.
  *Result:* full run on `Pixel_6` (API 31) and `Pixel_7_API_34` from a fresh install, driven over adb with screenshots,
  Logcat `Analytics`, `dumpsys` and a copy of the Room database. All §3 items pass on both: onboarding (dialog on 34,
  none on 31), denied / blocked notifications (banner in Alerts + Settings, Open settings → system page; on 31 blocked
  from the system app page), whole-world warning, city search (İzmir in Türkiye/Turkey, Tokyo in Japan, nonsense →
  "No city named …", airplane mode → network error + `city_search_failed`), EN ↔ TR kept after a restart and listed on
  the system language page (34), list (refresh, filters, nearest sort with distances, offline banner over 325 cached
  earthquakes, dark, 1.5×, rotation), detail actions (maps, USGS, share + events), deep links (cold / warm / unknown
  id), simulate → notification → detail + no duplicate, run check now → event log, periodic job present, Doze
  (periodic job's `DEVICE_NOT_DOZING` / `CONNECTIVITY` unsatisfied while idle, satisfied after), reboot keeps the job,
  no coordinates or city names in 70 / 69 stored events. §4: warm, cold and restored notification taps with the
  expected events on both; worker in a killed process posts Turkish text on API 31. Finding fixed: at font scale 1.5
  the list's fixed-height top bar overflowed into the status bar → growing header row (ADR-040 update). Test fixed:
  `OnboardingFlowTest` read strings from the system locale and failed when the app language was Turkish → strings
  from the launched activity. Notes (no change): a pull during a running refresh is ignored; an external deep link
  logs `source=list`; a scheduled developer simulation can run during forced Doze (no constraints, WorkManager runs it
  in-process); the summary notification tap was not reproduced by hand (tests + 2.5). TESTING.md updated (AVD table,
  Doze reading, API 34 job format, last run). Full suites: 361 unit, 77/77 instrumented on API 31 and 34.
- [x] **3.6 Design alignment.** Compare the screens with the UI design and fix the drift the user picked: badge
  colours and severity words, list chips with icons, day sections, detail icons, area toggle, welcome illustration,
  colourful event log. Icons only from the existing set.
  *Result:* ADR-042. Severity palette minor teal / light amber / moderate orange `#BF5700` with white text (user
  request) / strong red / major purple, all ≥ 4.5:1; compact badge shows the severity word (`Mod` / `Orta`). Sticky
  day sections (Today, Yesterday, date + count) only for `Newest first`. List and radius chips share one colour set;
  area mode is a pill toggle with radio semantics; welcome illustration drawn with `Canvas`; event log coloured by
  `EventCategory` (alert / background / settings / usage). Icons from `material-icons-core` and existing drawables;
  only `ic_schedule` added (user approved). A list UI test failed because the day header repeats the count → test
  updated, +2 UI tests (day sections, flat list), +29 unit tests (category mapping, day grouping). Verified on API 34
  (light, dark, Turkish) and API 31 (onboarding). Full suites: 390 unit, 79/79 instrumented on API 31 and 34, lint
  clean.
- [x] **3.7 Follow-up polish.** "Biggest first" label; app icon on the welcome page; theme choice in Settings;
  USGS place text in the app language.
  *Result:* welcome page shows the launcher icon layers masked to a circle (ADR-042 update). Theme: System / Light /
  Dark in the shared `ChoiceToggle`, AppCompat night mode + SharedPreferences, applied at start-up, `theme_changed`
  event (ADR-043). Place text: parsed into distance / direction / place, rebuilt per language with a trailing country
  translated through the platform country list (+ alias table for USGS spellings), stored text unchanged (ADR-044);
  used in list, detail, share, map label and notifications. Found on the emulator: Turkish lines broke between
  "124 km" and the direction → non-breaking spaces; API 34 spells Turkey "Türkiye" in English → UI tests read the
  expected text from `formatPlace`. +23 unit tests (place parsing, country names, theme ViewModel, event), +4
  instrumented (place formatting EN/TR, theme toggle). Verified on API 34 (theme switch and restart, Turkish list,
  biggest first) and API 31 (theme kept after restart). Full suites: 413 unit, 83/83 instrumented on API 31 and 34,
  lint clean.

- [x] **3.8 Theme picker, capitalized directions, region names.** Theme picked like the language; Turkish
  directions start with a capital; USGS region-style texts translated (patterns + dictionary).
  *Result:* language and theme share `SelectedValueRow` + `SingleChoiceDialog`, and both default options read
  "System default"; the pill toggle went back to the
  alerts feature (ADR-042/043 updated). English now shows the USGS text unchanged. Region phrases ("south of X",
  "northern X", "X region", "off the (east) coast of X", "X Islands / Island") and a 44-name dictionary chosen from
  one year of USGS data (all magnitudes, 138,695 events): in Turkish 3,418 of 3,434 region-style events read
  differently, checked on a device against all 148 distinct texts (ADR-044 rewritten). +RegionPhrase unit tests,
  place formatting tests extended, theme dialog UI test. Full suites: 419 unit, 83/83 instrumented on API 31 and 34,
  lint clean.
- [x] **3.9 Alerts tab: alert switch vs. notification permission** (added on request). With the permission off the
  top card said the alerts were on while the status card below said notifications were off.
  *Result:* while alerts are on and the permission is off, the summary card replaces "You'll be notified…" with
  "Alerts can't reach you" + "Allow notifications" (opens the app's notification settings); with alerts off nothing
  is flagged. Turkish switch renamed "Deprem alarmları" (it said "Deprem bildirimleri", the same word as the
  permission), and the alerts-off texts follow (ADR-046). Follow-up on request: the status card (last background
  check, check interval) removed with its strings; `ObserveAlertSettingsUseCase` now returns only the settings
  (`AlertSettingsStatus` and `lastCheckedAt` in the UI state removed; the worker still stores the check time for
  `updatedafter`). 440 unit, 88/88 instrumented on API 31 and 34, lint clean; checked on API 34 in TR and EN, and the
  notice goes away after allowing notifications.

- [x] **3.10 Review fixes: notification category, onboarding restore, notified vs. detail data, saved city without
  geocoder** (added on request).
  *Result:* (1) `NotificationAccessChecker` returns app blocked / "Earthquake alerts" category blocked / allowed; a
  blocked category now suppresses (`reason=alert_channel_blocked`), is not remembered and is delivered once turned
  on; Alerts, Settings and onboarding name the category and open its own settings page (ADR-047). (2) The onboarding
  area choice is kept in `SavedStateHandle`; a notifications page restored without an area returns to setup (ADR-035
  updated). (3) Fetches replace cached copies of the events they return; a detail opened from a notification asks
  USGS first and, offline, shows the saved copy with a "may be out of date" banner (ADR-048). (4) Without a geocoder a
  saved city can still be switched to the whole world (ADR-049). 466 unit, 96/96 instrumented on API 31 and 34, lint
  clean; on API 34: category off → suppressed → on → posted once, process kill on step 3 → city kept, notification
  link online / airplane mode; on API 31: category off in onboarding → "Open settings" → back shows allowed.

- [x] **3.11 Onboarding without scrolling + alert preview** (added on request).
  *Result:* every onboarding step fits on the screen at the default font size: welcome keeps icon, summary and
  disclaimer (feature cards removed); setup drops its subtitle and the slider's hint sentence, and city search moved
  into a full-screen "Choose a city" dialog, shared with the Alerts tab (ADR-050). New preview below the area in
  onboarding and on the Alerts tab: "With these settings, N earthquakes in the last 3 days would have matched your
  alerts", counted on the cached list with `AlertMatcher.matchesThresholdAndArea` (split out of the delivery rule),
  live while the slider is dragged, loads a missing cache, skips simulated events (ADR-051). 483 unit, 109/109
  instrumented on API 31 and 34 (incl. `OnboardingLayoutTest`), lint clean; on API 34 and 31: no step scrolls in EN
  and TR, counts equal the USGS count API (world M4.5+ 56, Tokyo 250 km M4.5+ 1, 1000 km M3.0+ 4), mid-drag update.

- [x] **3.12 Preview only on the Alerts tab, no "saved" snackbar** (added on request).
  *Result:* the preview slot and its live threshold were removed from the onboarding setup step (the Alerts tab keeps
  them, ADR-051 updated); the "Alert settings saved" snackbar, the ViewModel's one-off event flow and the string are
  gone (ADR-029 updated). 482 unit, 109/109 instrumented on API 31 and 34 (drag test moved to
  `AlertSettingsScreenTest`), lint clean; on API 34: no preview on step 2, a threshold change on the Alerts tab saves
  (summary M6.0+) without a message.

- [x] **3.13 Alert feedback + on-device alert metrics** (added on request).
  *Result:* a detail opened from a notification asks "Was this alert useful? Yes / No" (optional, once, kept across
  rotation; slot filled by `navigation`), recorded as `alert_feedback_given` (ADR-052). The Event log gets an "Alert
  metrics on this device" card computed from all recorded events: setup completion, alerts opened, useful among
  answers (non-answers excluded), alerts off / threshold raised within 24 h after a notification (ADR-053); existing
  events, table, filter, share and clear unchanged. 499 unit, 115/115 instrumented on API 31 and 34, lint clean; on
  API 34: simulate → tap → Yes → thanks after rotation; list-opened detail without card; second alert unanswered →
  useful 1/1, answered 1 of 2; threshold 4.5 → 6.0 → opt-out 1 / 2.

## Phase 4 — Documentation and delivery
- [x] **4.1 README (Turkish) complete.** All sections filled, structured along the delivery requirements (how to run,
  key decisions, out of scope, time spent, next steps, AI usage: tools, delegated work, verification, link to the
  usage report). No screenshots (user decision, 2026-09-27).
  *Result:* tools = Claude Code (VS Code extension) + Google Stitch (design, read through the Stitch MCP server); time
  spent = work-log total only (user: nothing to add outside the log). `docs/AI_USAGE.md` tools table updated too.
- [x] **4.2 AI usage report.** Fill `docs/AI_USAGE.md` with real `npx ccusage@latest` output for this project's
  sessions (`--since 20260925`, per-model breakdown).
  *Result:* ccusage 20.0.24 has no `--breakdown` or project filter on `daily`, so the report uses
  `session --since 20260925 --json` and keeps the sessions whose logs live in this repository's Claude Code project
  folder (7 of 8; the excluded one ran in the home folder before the project started). One session is a resumed copy
  of another (150 shared messages); ccusage counts each message once — confirmed against the raw logs. Result:
  100% Claude Opus 5.5, 337.7 M tokens (98.4% cache reads, 1.49 M output), $127.21 at API list prices (subscription
  used). `docs/AI_USAGE.md` completed (tools, delegated work, what stayed with the user, verification, per-session /
  per-phase table); README AI section got the model summary, and a bug reference that pointed to an ADR which does
  not record it was corrected in both files.
- [ ] **4.3 Translate README to English** (keep it short).
- [ ] **4.4 Final checks.** Clean clone builds; all tests pass; lint clean; wording review of every file and commit
  message; `docs/PLAN.md` keep/remove decision with the user; update the README time total with the phase 4 rows
  of the work log; regenerate the usage report in `docs/AI_USAGE.md` (the documentation session is still running,
  so its numbers grow); if `docs/PLAN.md` is removed, fix the links to it in `docs/AI_USAGE.md`.

## Phase 5 — Stretch (only when Phases 0–4 are done)
- [x] **5.1 GPS-based area** ("Use my location" → center; reverse geocode for the name).
  *Result:* "Use my location" button above the country picker (onboarding and Alerts tab share it), approximate
  location permission only, requested on first tap (ADR-045). `AndroidDeviceLocationSource` (platform
  `LocationManager`, no new dependency) asks fused / network / gps at once, first fix wins, 15 s limit, then the last
  known location; `CitySearchRepositoryImpl.findCityAt` names the point with the existing Geocoder wrapper (API 33
  split) and keeps the device point as the circle center. The found city is picked like a tapped search result;
  errors: permission denied → app settings, location off → location settings, not found / network → retry. New
  event `current_location_used` (`result`, no coordinates). Found on the emulator: with approximate permission the
  fused provider stays in low-power mode and never answers there, so all providers are asked in parallel; a
  located city equal to the current one left the search open, so the result now goes through UI state instead of
  a one-off channel. 440 unit (+21), 89/89 instrumented (+6) on API 31 and 34; manual on API 31 (Konak, İzmir) and
  API 34 (EN + TR, deny, location off, rotation while locating; the API 34 AVD ignored `geo fix`, test providers
  used, see TESTING.md).

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
| 2026-09-25 | 18:38–18:50 | ~12m | 0.6 Design system from the UI design: colour schemes, severity scale, type, shapes, spacing, shared state components (ADR-018) |
| 2026-09-25 | 20:28–20:36 | ~8m | 1.1 Core basics: AppResult/AppError, Clock, IO dispatcher, network + DataStore modules, preferences repository, GeoPoint/City/AlertArea, haversine; 50 unit tests; verified on API 31 and 34 (ADR-019) |
| 2026-09-25 | 20:40–20:48 | ~8m | 1.2 Analytics core: event dictionary (25 events), LocalAnalyticsTracker (Room + Logcat), QuakeAlertDatabase v1, app scope, `app_opened`; unit + instrumented tests; verified on API 31 and 34 (ADR-020) |
| 2026-09-25 | 20:52–20:58 | ~6m | Project rules: no comments in code/build/resource files, no Turkish outside README and TR strings; removed existing comments (80 files) and Turkish test/preview data |
| 2026-09-25 | 20:58–21:07 | ~9m | 1.3 Earthquakes data: USGS DTOs + API, mappers, query parameters, cache table + DAO (DB v2, auto-migration), repository; unit + instrumented tests; verified on API 31 and 34 (ADR-021, ADR-022) |
| 2026-09-25 | 21:12–21:15 | ~3m | 1.4 Earthquakes domain: config, filters, observe/refresh/freshness/get use cases with distance; unit tests; verified on API 34 |
| 2026-09-25 | 21:18–21:42 | ~24m | 1.5 List screen: ViewModel, UI state, top bar, chips, cards, states, stale banner, analytics; fixed two layout bugs found on the emulator; unit + Compose UI tests; verified on API 31 and 34 |
| 2026-09-25 | 21:42–22:30 | ~48m | 1.5.1 List sorting (newest / largest / nearest) + performance measurement; list computation off the main thread, in-memory paging removed (ADR-023); unit + Compose UI tests; verified on API 31 and 34 |
| 2026-09-26 | 11:12–11:41 | ~29m | 1.6 Detail screen: details use case, assisted ViewModel, facts/distance/actions UI, detail route, not-found/error states, analytics; unit + Compose UI tests; verified on API 31 and 34 incl. real 404, offline and missing maps app (ADR-024) |
| 2026-09-26 | 11:41–12:09 | ~28m | 1.7 Navigation: onboarding/main graphs, start destination, onboarding placeholder, deep link to detail handled in place (ADR-025); unit tests; verified cold/warm deep links on API 31 and 34 |
| 2026-09-26 | 15:15–15:30 | ~15m | 2.1 Alert domain: AlertConfig, pure AlertMatcher with criteria object (ADR-026); 23 unit tests covering every SPEC §5.1 rule and its boundaries |
| 2026-09-26 | 15:27–15:42 | ~15m | 2.2 City search: country list, Geocoder wrapper with API 33 split, repository rules tested with a fake geocoder (ADR-027); fixed country-level results found on the emulator; 26 unit tests; verified on API 31 and 34 incl. airplane mode |
| 2026-09-26 | 15:44–16:08 | ~24m | 2.3 Area components: threshold slider, area selector (country picker, city search, selected city, radius, whole-world warning), shared CitySearchViewModel with device-region default (ADR-028); 39 unit + 9 Compose UI tests; verified on API 31 and 34 incl. TR, dark, offline, landscape |
| 2026-09-26 | 16:19–16:34 | ~15m | 2.4 Alert settings screen: summary switch, threshold, area, status (permission + last check), auto-save with new baseline and snackbar, analytics from the settings difference (ADR-029); declared POST_NOTIFICATIONS after the emulator showed it missing; 18 unit + 6 Compose UI tests; verified end to end on API 31 and 34 |
| 2026-09-26 | 16:46–17:20 | ~34m | 2.5 Notifications: channel, notify policy use case + Android notifier (single / up to 3 / summary), localized texts, tap extras, app-open tracking (ADR-030); fixed deep links after process death (ADR-031); language splits off (ADR-032); 17 unit + 5 instrumented tests; verified warm / cold / restored taps and summary on API 31 and 34 |
| 2026-09-26 | 17:45–18:10 | ~25m | 2.6 Worker: notified ids in Room (DB v3), background check use case, worker, scheduler + schedule sync (ADR-033); stored app language for worker processes below API 33 after the emulator showed English texts (ADR-034); 20 unit + 6 instrumented tests; verified migration, job, reboot and a killed-process run on API 31 and 34 |
| 2026-09-26 | 18:45–19:05 | ~20m | 2.7 Onboarding: welcome / alert setup / notifications pages, permission request and denial path, finish saves settings + schedules (ADR-035); 13 unit + 6 Compose UI tests; verified allow and deny paths on API 34 and the no-dialog path on API 31 |
| 2026-09-26 | 19:53–20:08 | ~15m | 2.8 Developer tools: simulate alert through the shared delivery step, repeat from the cache, run check now (ADR-036); 17 unit tests; verified simulate → tap → detail → no duplicate and run check now on API 31 and 34 |
| 2026-09-26 | 20:16–20:38 | ~22m | 2.8 follow-up: simulation with magnitude / distance / delay, scheduled delivery with the app closed, `outcome` on the developer event (ADR-037); +13 unit tests; verified matching vs non-matching on API 31 and 34 |
| 2026-09-26 | 20:52–21:09 | ~17m | 3.1 Settings screen: language picker, notification status, about (USGS, disclaimer, version), shared permission row and outgoing intents moved to core (ADR-038); 5 unit + 5 Compose UI tests; verified on API 31 and 34 incl. system per-app language, rotation, dark mode |
| 2026-09-26 | 21:10–21:22 | ~12m | 3.1 follow-up: developer tools moved to their own screen behind a Settings entry, alert testing card renamed (ADR-039); +4 Compose UI tests; verified on API 34 |
| 2026-09-26 | 21:22–21:34 | ~12m | 3.2 Event log: eventlog feature over the analytics table, filter / share / clear with confirmation, live list (ADR-039); 12 unit + 9 instrumented tests; verified on API 31 and 34 incl. rotation, share sheet, TR |
| 2026-09-26 | 21:35–22:01 | ~26m | 3.3 Polish: adaptive launcher icon, scrolling list controls + navigation rail for landscape (ADR-040), copy review EN/TR, heading semantics, one-line labels at large font scale; verified on API 31 and 34 incl. font scale 2.0 and dark mode |
| 2026-09-26 | 22:02–22:19 | ~17m | 3.4 Instrumented tests: Hilt test runner + isolated storage and fake USGS (ADR-041), onboarding end-to-end through MainActivity, AlertCheckWorker with fakes; 77/77 on API 31 and 34 |
| 2026-09-27 | 13:38–14:29 | ~51m | 3.5 Full QA matrix on API 31 and 34 (all §3 items + §4 scenarios); fixed list header overflow at large font (ADR-040 update) and a locale-dependent end-to-end test; TESTING.md updated |
| 2026-09-27 | 16:45–17:08 | ~23m | 4.1 README (Turkish): all sections rewritten from the delivery requirements and the current docs; AI tools (Claude Code, Google Stitch) confirmed by the user |
| 2026-09-27 | 17:08–17:17 | ~9m | 4.2 AI usage report: ccusage per session, scoped to this repository's sessions, duplicate check against raw logs, AI_USAGE.md completed, README AI summary |
| 2026-09-27 | 17:45–18:24 | ~39m | 3.6 Design alignment with the UI design: severity palette + words, chip icons, day sections, detail icons, area toggle, welcome illustration, event log categories (ADR-042); 390 unit, 79/79 instrumented on API 31 and 34 |
| 2026-09-27 | 18:24–19:15 | ~51m | 3.7 Follow-up polish: "Biggest first", app icon on the welcome page, theme choice (ADR-043), USGS place text localized on display (ADR-044); 413 unit, 83/83 instrumented on API 31 and 34 |
| 2026-09-27 | 19:15–20:07 | ~52m | 3.8 Theme picker like the language picker, capitalized Turkish directions, USGS region names translated (patterns + 44-name dictionary from one year of data, ADR-044); 419 unit, 83/83 instrumented on API 31 and 34 |
| 2026-09-28 | 14:45–15:30 | ~45m | Phase 5 trimmed to one item; 5.1 "Use my location": approximate permission, platform LocationManager (all providers in parallel), reverse geocoding, error paths, `current_location_used` (ADR-045); 440 unit, 89/89 instrumented on API 31 and 34 |
| 2026-09-28 | 16:10–16:30 | ~20m | 3.9 Alerts tab: blocked notifications shown once, in the summary card with "Allow notifications"; status card shows only the background check; Turkish switch renamed "Deprem alarmları" (ADR-046); 440 unit, 90/90 instrumented on API 31 and 34 |
| 2026-09-28 | 16:35–16:45 | ~10m | 3.9 follow-up: Alerts tab status card removed (last check + interval), use case returns only the settings (ADR-046 updated); 440 unit, 88/88 instrumented on API 31 and 34 |
| 2026-09-28 | 22:59–23:32 | ~33m | 3.10 Review fixes: blocked notification category suppressed and shown (ADR-047), onboarding area kept across process death (ADR-035 update), notification detail revalidated + cached copies refreshed (ADR-048), saved city clearable without geocoder (ADR-049); 466 unit, 96/96 instrumented on API 31 and 34 |
| 2026-09-29 | 13:00–13:37 | ~37m | 3.11 Onboarding fits without scrolling (feature cards and hints removed, city search in a dialog, ADR-050); alert preview "N earthquakes in the last 3 days would have matched" with the alert rule on the cached list, live while dragging (ADR-051); 483 unit, 109/109 instrumented on API 31 and 34 |
| 2026-09-29 | 14:10–14:20 | ~10m | 3.12 Preview removed from onboarding (Alerts tab only, ADR-051 updated); "saved" snackbar on the Alerts tab removed (ADR-029 updated); 482 unit, 109/109 instrumented on API 31 and 34 |
| 2026-09-29 | 14:32–14:54 | ~22m | 3.13 Optional alert feedback on the notification detail (ADR-052); on-device alert metrics card in the event log: setup completion, opens, useful among answers, opt-out within 24 h (ADR-053); 499 unit, 115/115 instrumented on API 31 and 34 |
