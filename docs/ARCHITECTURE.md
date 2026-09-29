# QuakeAlert — Architecture

## 1. Overview
- Single Gradle module `:app`, single activity (`MainActivity`), 100% Jetpack Compose + Material 3.
- **Feature-first Clean Architecture:** code is grouped by feature; each feature has `data` / `domain` /
  `presentation`. Shared code lives in `core`.
- **MVVM + Unidirectional Data Flow** in presentation: `@HiltViewModel` exposes one immutable `UiState` via
  `StateFlow`; composables call ViewModel functions for events; one-off effects via `SharedFlow`.
- Dependency rule: presentation → domain ← data. `domain` is pure Kotlin (no Android imports).

## 2. Tech stack
| Concern | Choice |
|---|---|
| Language / build | Kotlin 2.4 (AGP built-in Kotlin), AGP 9, Gradle 9, version catalog, KSP, Java 17, compileSdk 36 |
| UI | Jetpack Compose (BOM pinned for compileSdk 36), Material 3, Navigation Compose (type-safe routes with kotlinx.serialization) |
| DI | Hilt (+ `hilt-lifecycle-viewmodel-compose` for `hiltViewModel()`, `hilt-work`) |
| Networking | Retrofit + OkHttp + kotlinx.serialization converter |
| Persistence | Room (cache, notified ids, analytics events), DataStore Preferences (user preferences) |
| Background | WorkManager (periodic work, `CoroutineWorker`, Hilt worker factory) |
| Localization | Android resources + AppCompat per-app language API (`AppCompatDelegate.setApplicationLocales`) |
| Tests | JUnit Jupiter 6 (android-junit plugin), MockK, kotlinx-coroutines-test, Turbine; Compose UI tests (JUnit 4 rule) |

Exact versions are pinned in `gradle/libs.versions.toml`: the latest stable release that supports compileSdk 36
(ADR-015).

## 3. Package structure
```
com.ahmetyildiz.quakealert/
├── QuakeAlertApplication.kt         # @HiltAndroidApp, WorkManager Configuration.Provider
├── MainActivity.kt                  # AppCompatActivity (needed for per-app language < API 33), hosts NavHost
├── navigation/                      # Routes, NavHost, onboarding graph, main graph, bottom bar / rail, deep links
├── core/
│   ├── analytics/                   # AnalyticsTracker (interface), AnalyticsEvent, LocalAnalyticsTracker
│   ├── database/                    # QuakeAlertDatabase, entities, DAOs (earthquake cache, notified ids, events)
│   ├── datastore/                   # DataStoreUserPreferencesRepository (DataStore keys + mapping)
│   ├── preferences/                 # UserPreferences, AlertSettings + UserPreferencesRepository (shared by features)
│   ├── model/                       # GeoPoint, City, AlertArea, MagnitudeSeverity … pure Kotlin shared models
│   ├── location/                    # Distance (haversine) utilities, location permission check
│   ├── network/                     # Retrofit/OkHttp/Json setup
│   ├── notification/                # NotificationChannels, NotificationAccessChecker (app + channel), AlertNotificationTap extras
│   ├── navigation/                  # DeepLinkConfig (detail deep link builder), ExternalIntents (browser, notification settings)
│   ├── locale/                      # AppLanguage, AppLanguageManager (per-app language), DeviceRegionProvider, CountryNames
│   ├── appearance/                  # ThemeMode, ThemeModeManager (system / light / dark through AppCompat night mode)
│   ├── error/                       # AppResult, AppError
│   ├── time/                        # Clock abstraction (testable "now"), formatters
│   ├── di/                          # App-wide Hilt modules (network, database, datastore, dispatchers, app scope, clock)
│   └── ui/                          # Reusable composables (states, badges, SectionCard, NotificationPermissionStatus) + theme/
├── features/
│   ├── earthquakes/                 # List + detail + USGS data
│   │   ├── data/model | source | repository
│   │   ├── domain/model | repository | usecase
│   │   ├── presentation/viewmodel | screen | component
│   │   └── di/
│   ├── alerts/                      # Alert settings, city search (Geocoder), onboarding, worker, notifications
│   │   ├── data/model | source (CityGeocoder, DeviceLocationSource + Android implementations) | repository
│   │   ├── domain/model | repository | usecase   (+ AlertConfig, AlertMatcher at the domain root)
│   │   ├── worker/                  # AlertCheckWorker, AlertWorkScheduler
│   │   ├── notification/            # EarthquakeAlertNotifier
│   │   ├── presentation/viewmodel | screen | component   (settings, onboarding, developer tools)
│   │   └── di/
│   ├── settings/                    # Language, about, permission status, developer entry points
│   │   └── presentation/viewmodel | screen | component   (+ SettingsConfig; no data/domain: reads core only)
│   └── eventlog/                    # Developer event log: data (AnalyticsEventDao) | domain (LoggedEvent, filter, share text) | presentation
```

### Cross-feature rules
- The **only** cross-feature dependency: `alerts` uses `earthquakes.domain` (`EarthquakeRepository` interface and
  domain model) to fetch earthquakes. Never `earthquakes.data`. See ADR-005.
- Shared user preferences (alerts enabled, threshold, area, onboarding flag, baseline, last check) live in
  `core/preferences` because both the list (filters, distance) and alerts need them.
- Features never import each other's screens; the `navigation` package wires all routes.
- Onboarding lives in `alerts` because it is the alert setup flow (reuses the same threshold/area components).
- `settings` depends on `core` only (ADR-038): `SettingsViewModel` reads `AppLanguageManager`, `ThemeModeManager` and
  `NotificationAccessChecker` directly. Pieces used by more than one feature moved to `core` — the permission
  status row (`core/ui/component/NotificationPermissionStatus`, now shown only in Settings; the Alerts tab flags a
  blocked permission in its summary card, ADR-046) and the outgoing intents
  (`core/navigation/ExternalIntents`). The Settings tab only shows a "Developer tools" entry (debug builds); `DeveloperToolsScreen` (settings) is a pushed
  route whose alert testing card comes in as a slot from `navigation` and which links to the event log (ADR-039).

## 4. Key flows

### 4.1 Earthquake list (offline-first)
```
EarthquakeListScreen ─▶ EarthquakeListViewModel
        ▲                   │ observe: ObserveRecentEarthquakesUseCase (Room Flow + UserPreferences + filters)
        │ UiState           │ refresh: RefreshEarthquakesUseCase
        │                   ▼
        └────────── EarthquakeRepositoryImpl ── UsgsApi (Retrofit)
                                             └─ EarthquakeDao (Room)
```
- `RefreshEarthquakesUseCase` builds the query (7 days / M2.5+ worldwide) and stores `lastRefreshedAt`; the repository
  replaces the cache in one transaction. A failed refresh leaves the cache untouched.
- The repository holds no product rules: every query (time window, magnitude, area, `updatedafter`) comes from a use
  case as an `EarthquakeQuery`, mapped to USGS parameters in the data layer.
- Detail by id: `getCachedEarthquake(id)` and `fetchEarthquake(id)` (USGS `eventid`; 404 or a non-earthquake event →
  `NotFound`); `GetEarthquakeUseCase` decides the order (ADR-048). Fetches never add events to the cache, but they
  replace the cached copy of every event they return (`EarthquakeDao.updateExisting`), so the cache keeps the list's
  scope with the newest version the app has seen.
- Features that are not earthquakes or lack valid coordinates/depth are dropped while mapping.
- Filters and distances are computed in the domain layer (haversine) on the cached list:
  `ObserveRecentEarthquakesUseCase(options)` combines the cache with the user preferences and returns
  `RecentEarthquakes` (filtered and sorted items with distance, applied options, cached count, area, threshold, last
  refresh). `EarthquakeListOptions` = region filter + magnitude filter + sort order. "Near city" and "nearest first"
  without an area fall back to world / newest first and are reported as such in `appliedOptions`.
- The mapping, filtering, distance and sorting run on the injected `@DefaultDispatcher` (`flowOn`), never on the main
  thread. The full period is kept as one in-memory list without paging (ADR-023).
- `CheckCacheFreshnessUseCase` → `MISSING` (never refreshed) / `STALE` (older than 5 min) / `FRESH`; the list uses it to
  decide on an initial or stale refresh. Product values live in `EarthquakesConfig`.
- The screen calls `onScreenStarted()` / `onScreenStopped(isConfigurationChange)` from a `LifecycleStartEffect`: each
  start checks freshness; a restart caused by rotation or a language switch is not logged as a new view.
- `EarthquakeListUiState.content` = `LOADING` | `ERROR` | `EMPTY` | `EMPTY_FILTERED` | `ITEMS`, derived in the ViewModel
  from the cache size, the filtered list and the last refresh result; the stale-data banner and the filter chips sit
  above the list (not inside it), together with the count + sort menu, so they are always visible. The list state is
  keyed on the options (`rememberSaveable(options)`): a new sort or filter starts at the top, rotation keeps the
  position.

### 4.2 Background alert check
```
WorkManager (periodic 15 min, NetworkType.CONNECTED)
  └─ AlertCheckWorker (thin) ─▶ CheckForNewAlertsUseCase
        ├─ UserPreferencesRepository (settings, baseline, lastCheckedAt)
        ├─ EarthquakeRepository.fetchEarthquakes(query with updatedAfter)   ← earthquakes.domain
        ├─ AlertMatcher (rules SPEC §5.1, pure Kotlin, unit-tested; input: AlertMatchCriteria, ADR-026)
        ├─ NotifiedEarthquakeRepository (dedupe, prune 30 days)
        ├─ AlertNotifier (interface) ─▶ EarthquakeAlertNotifier (Android)
        └─ AnalyticsTracker
```
- `CheckForNewAlertsUseCase` (ADR-033): skipped while alerts are off or before settings were ever saved; USGS query
  = `starttime now − 6 h`, `minmagnitude` threshold, circle of the area, `updatedafter (lastCheckedAt ?: baseline) −
  10 min`; matches notified through `NotifyAlertsUseCase`; only posted ids are stored; ids older than 30 days pruned;
  `lastCheckedAt` = start of the check; result `Skipped` / `Completed(fetched, matched, notified)` / `Failed(error)` →
  worker `success` / `retry` (network, server) / `failure`.
- `SyncAlertScheduleUseCase` (app start in `QuakeAlertApplication`, and after every saved change in
  `UpdateAlertSettingsUseCase`) → `AlertCheckScheduler`: `AlertWorkScheduler` enqueues unique periodic work
  `periodic_alert_check` (15 min, network, `ExistingPeriodicWorkPolicy.UPDATE`) when alerts are enabled and settings
  were saved; cancels it otherwise. Enabling alerts runs a first check right away; WorkManager postpones a forced run
  of the periodic job before its time.
- `DeliverAlertsUseCase` is the shared "match against stored ids → `NotifyAlertsUseCase` → remember posted ids" step
  of the background check and of the developer tools.
- Developer tools (debug builds only, ADR-036): "Simulate alert" (`SimulateAlertUseCase`) builds an earthquake that
  matches the saved settings, adds it to the cache (so the notification opens a detail) and delivers it through
  `DeliverAlertsUseCase`; "Simulate the same alert again" re-delivers the newest cached simulated event (nothing is
  posted — dedupe); "Run check now" enqueues a unique one-time `AlertCheckWorker` request (network constraint, not
  expedited). The simulation takes a magnitude, a distance from the city and a delay (`SimulationRequest`);
  "Schedule" enqueues `SimulatedAlertWorker` through `SimulatedAlertScheduler` with an initial delay (ADR-037). The card is the `alertTools` slot of `DeveloperToolsScreen`, a route registered in `QuakeAlertNavHost` only when
  `BuildConfig.DEBUG` (ADR-039).
- The worker uses Hilt via `HiltWorkerFactory`; the default WorkManager initializer is removed from the manifest.
- Event log (debug builds only, ADR-039): `EventLogRepositoryImpl` maps `AnalyticsEventDao.observeAll()` (newest
  first) to `LoggedEvent` and clears with `deleteAll()`. `EventLogViewModel` combines the events with the query from
  `SavedStateHandle` (`filterByName`: trimmed, case-insensitive "contains"); the text field keeps its own saveable
  text so typing never waits for the flow. Share = the visible events as text (`EventLogText`: one line per event,
  ISO-8601 local time with offset, name, `key=value` params) through the system share sheet; Clear asks first.
- Alert metrics (ADR-053): `CalculateAlertMetricsUseCase` is a pure function over all `LoggedEvent`s (not the
  filtered ones) and returns `AlertMetrics` (`MetricRatio` count / total, percent only when total > 0). Event names
  and params it reads are listed once in `MeasuredEvents`; its tests build the log from real `AnalyticsEvent`
  objects, so a renamed event or param breaks them. The 24 h window is `AlertMetricsConfig.OPT_OUT_WINDOW`.
  `AlertMetricsCard` is the first list item while no filter is set. Nothing is stored besides the existing
  `analytics_events` rows.

### 4.3 City search (Geocoder wrapper)
```
AreaSelectorEntry ─▶ CitySearchViewModel ─▶ SearchCitiesUseCase ─▶ CitySearchRepository (domain interface)
                                                  └─ CitySearchRepositoryImpl (data, rules)
                                                       └─ CityGeocoder ─▶ AndroidCityGeocoder (android.location)
```
- `SearchCitiesUseCase(CitySearchQuery(name, country, locale))` trims the name; a blank name returns no cities
  without a request. `isAvailable()` tells the UI whether to show city search at all.
- `CitySearchRepositoryImpl.searchCities(query)` (ADR-027):
  - `Geocoder.isPresent() == false` → `AppError.GeocoderUnavailable`.
  - Asks the geocoder for `"<name>, <localized country name>"` in the app locale (better hits for names that exist
    in several countries, localized admin areas).
  - `IOException` → `AppError.Network`, anything else → `Unknown` (`safeApiCall`); empty → no results.
  - Drops country-level results (no locality or admin area) unless their name equals the typed name (city states),
    maps to `City(name, adminArea, countryCode, location)` (`core/model`), keeps the selected country, de-duplicates
    by name + admin area.
- `AndroidCityGeocoder.findAddresses(locationName, locale)` is the only class using `android.location`:
  - **API ≥ 33:** `getFromLocationName(name, max, GeocodeListener)` wrapped in `suspendCancellableCoroutine`
    (`onError` → `IOException`).
  - **API < 33:** deprecated blocking `getFromLocationName(name, max)` on the IO dispatcher (`@Suppress` scoped to
    that one function).
  - Addresses without coordinates are skipped; the rest become plain `GeocodedAddress` values.
- `GetCountriesUseCase(displayLocale)`: `Locale.getISOCountries()` with names in the display locale, sorted with that
  locale's `Collator`.
- Presentation (ADR-028): the alert settings screen and the onboarding setup step own the user's choice as an
  `AreaSelection` (mode, city, radius) and render the stateless `MagnitudeThresholdSelector` and
  `AreaSelectorEntry`. The entry wires `CitySearchViewModel`, shared by both screens: country list for the app
  language (rebuilt on language change, keeps the selected code), default country = device region
  (`DeviceRegionProvider`, system locale — not the per-app language), search states Idle / Loading / Found /
  NoResults / Failed, `city_search_performed` / `city_search_failed`. Picking a city, the radius or the mode is
  reported back as a new `AreaSelection`; `toAlertAreaOrNull()` is null while "Near a city" has no city yet.
  Without a geocoder the mode choice stays visible while a city is part of the selection, so a saved city can be
  switched to the whole world; only search and "Change" are hidden (ADR-049).
- The search itself (location, country, name, results) is `CitySearchPanel` inside the full-screen
  `CitySearchDialog`; `AreaSelector` shows only the selected city or a "Choose a city" button, so the onboarding
  setup step fits without scrolling (ADR-050).
- Must be verified on API < 33 **and** API ≥ 33 emulators (see TESTING.md).

### 4.3.2 Alert preview (ADR-051)
```
MagnitudeThresholdSelector ──onThresholdDragged──▶ previewThreshold (screen state)
AreaSelection.toAlertAreaOrNull() ──────────────┐        │
                                                ▼        ▼
                   AlertPreviewEntry ─▶ AlertPreviewViewModel ─▶ PreviewRecentAlertMatchesUseCase
                                             │                        ├─ EarthquakeRepository.observeCachedEarthquakes()
                                             │                        └─ AlertMatcher.matchesThresholdAndArea (same rule as alerts)
                                             └─ CheckCacheFreshnessUseCase / RefreshEarthquakesUseCase (missing → load, stale → refresh)
```
- The count uses the list cache (7 days, M2.5+, worldwide), which covers every selectable threshold and area; the
  window is `AlertConfig.PREVIEW_PERIOD` (3 days) back from now. `AlertConfigTest` guards both coverage rules.
- Only the threshold + area part of `AlertMatcher` applies; baseline, already-notified ids and the 6 h freshness
  limit are delivery rules for new events and do not apply to history. Simulated test events are skipped.
- Shown on the Alerts tab only (not in onboarding). The slider reports every step while dragging
  (`onThresholdDragged`), so the preview changes before the threshold is saved; area and radius changes arrive through the selection. `AlertPreviewUiState`: Hidden (no area yet) /
  Loading / Unavailable / Ready(`AlertPreview`).

### 4.3.1 "Use my location" (ADR-045)
```
CurrentLocationSection ─▶ AreaSelectorEntry (permission launcher) ─▶ CitySearchViewModel.onUseMyLocation
  └─ FindCityAtCurrentLocationUseCase ─▶ DeviceLocationRepository ─▶ DeviceLocationSource ─▶ AndroidDeviceLocationSource
                                     └─▶ CitySearchRepository.findCityAt ─▶ CityGeocoder.findAddressesAt
```
- `AreaSelectorEntry` checks `hasLocationPermission()` (`core/location`) and requests `ACCESS_COARSE_LOCATION`
  through `rememberLauncherForActivityResult`; granted → `onUseMyLocation()`, denied → `onLocationPermissionDenied()`.
- `DeviceLocationRepositoryImpl`: no permission → `LocationPermissionDenied`, location off → `LocationDisabled`,
  otherwise the current location within `AlertConfig.CURRENT_LOCATION_TIMEOUT` (15 s), else the newest last known
  location, else `LocationUnavailable`.
- `AndroidDeviceLocationSource` is the only class using `LocationManager`: `LocationManagerCompat.getCurrentLocation`
  on every enabled provider of fused (API 31+), network and gps at the same time (`channelFlow`); the first fix wins
  and the other requests are cancelled, as they are when the coroutine is cancelled.
- `CitySearchRepositoryImpl.findCityAt(point, locale)`: reverse geocoding (`getFromLocation`, same API 33 split and
  error mapping as the search), first address that is not country level → `City` whose `location` is the device
  point (the circle is centred on the user, not on the town centre); no such address → `LocationUnavailable`.
- `CitySearchViewModel` keeps `LocationLookup` (Idle / Locating / Found / Failed) in `CitySearchUiState`. `Found`
  is picked by `AreaSelector` exactly like a tapped search result (`onCitySelected`: the search closes, the lookup
  goes back to Idle, the `AreaSelection` gets the city), so a result that arrives during a rotation is not lost.
  Search and location share one job, so the newer request cancels the older. Every result is tracked as
  `current_location_used`.

- Alert settings screen (ADR-029): `AlertSettingsViewModel` combines `ObserveAlertSettingsUseCase` (saved alert
  settings), the on-screen `AreaSelection` draft and the notification permission (re-read on resume). Every change goes
  through `UpdateAlertSettingsUseCase` (mutex, reads the stored settings, saves with a new baseline only when
  something changed, returns previous + updated); the ViewModel logs `toAnalyticsEvents(SETTINGS)` of that update. No
  "saved" message: the controls and the summary card already show the saved state.

### 4.4 Earthquake detail
- `EarthquakeDetailRoute(earthquakeId, isFromNotification)` (navigation) → `EarthquakeDetailEntry` →
  `EarthquakeDetailViewModel`, which receives the id and the analytics source through Hilt assisted injection
  (ADR-024) → `GetEarthquakeUseCase(id, shouldRevalidate = isFromNotification)` → `EarthquakeDetails` (earthquake +
  `DistanceFromCity` with `isWithinAlertArea` from `AlertArea.contains`, the same rule as alerts, +
  `isSavedCopyAfterFailedRefresh`). From the list: cache first, then USGS. From a notification: USGS first; on a
  network / server error the cached copy is shown under a "may be out of date" banner with Retry (ADR-048).
- Feedback (ADR-052): the detail screen has a `feedback` slot; `navigation` fills it with the alerts feature's
  `AlertFeedbackEntry(eventId)` only for `isFromNotification`, so `earthquakes` does not depend on `alerts`.
  `AlertFeedbackViewModel` keeps "answered" in `SavedStateHandle` (survives rotation and process death) and tracks
  `alert_feedback_given` once.
- States: `LOADING` | `LOADED` | `NOT_FOUND` (HTTP 404 or not an earthquake) | `ERROR` (retry).
- Maps (`geo:` intent), USGS page (browser) and share (chooser) are launched by the entry composable; the ViewModel
  only logs `detail_action_clicked`. A missing handler app shows a snackbar instead of crashing.
- The bottom bar is shown only on top-level destinations; pushed screens (detail) use the full height.
- The list's header (`EarthquakeListTopBar`) is a plain row with a 64 dp minimum height instead of a `TopAppBar`, so
  its two-line title block grows with the font scale instead of overflowing into the status bar (found in 3.5).
- The list's controls (stale-data banner, filter chips, count + sort) are the first item of the `LazyColumn`, so they
  scroll away with the list and landscape / large font scales keep room for the cards (ADR-040).

### 4.5 Navigation and deep links
- Root `NavHost` with two graphs: `OnboardingGraphRoute` (onboarding) and `MainGraphRoute` (three tabs + detail). The
  start graph comes from `StartDestinationViewModel`, which reads `isOnboardingCompleted` once per session (the
  first frame stays empty for that read); finishing onboarding navigates to the main graph and pops onboarding.
- Tabs pop up to `MainGraphRoute` with save/restore state; the tab navigation is shown only on the three tab
  destinations: a bottom bar below 600 dp window width, a navigation rail from 600 dp (landscape phones, tablets),
  decided in `QuakeAlertApp` from `LocalWindowInfo.containerSize` (ADR-040). Tab labels stay on one line.
- `EarthquakeDetailRoute` declares `navDeepLink` `quakealert://earthquake/{earthquakeId}?isFromNotification={bool}`
  (manifest: `VIEW` + scheme `quakealert`, `launchMode="singleTop"`). Cold start: the NavController handles the
  activity intent while setting the graph; any later intent (`onNewIntent`, also right after a restore from process
  death) is stored as a pending deep link in `MainActivity` and handled by `QuakeAlertApp` once the NavHost exists
  (ADR-031). Both run in place (ADR-025): the back stack becomes list → detail, the activity is not recreated.
- A deep link before onboarding is completed opens the detail with onboarding underneath (only reachable from adb
  or another app; notifications exist only after onboarding).
- Onboarding (ADR-035) is one route, `OnboardingEntry` → `OnboardingViewModel`, with pages `WELCOME` →
  `ALERT_SETUP` → `NOTIFICATIONS` held in the ViewModel (page, threshold and area choice in `SavedStateHandle`;
  a notifications page restored without an area returns to the setup page, ADR-035; `BackHandler` for the previous
  page). The setup page reuses `MagnitudeThresholdSelector` + `AreaSelectorEntry`; the notifications
  page requests `POST_NOTIFICATIONS` through `rememberLauncherForActivityResult` on API 33+. Finish →
  `CompleteOnboardingUseCase` (save settings + baseline, completion flag, schedule) → `navigateToMainGraph()`.

### 4.6 Notifications (ADR-030)
- Channel `earthquake_alerts` ("Earthquake alerts", high importance) is registered in `QuakeAlertApplication.onCreate`
  (every process start, including a worker-only process).
- `NotifyAlertsUseCase(earthquakes, settings)`: empty → `NOTHING_TO_NOTIFY`; app notifications off or the
  `earthquake_alerts` channel blocked (`NotificationAccess`, ADR-047) → `SUPPRESSED` + `alert_notification_suppressed`
  (not remembered, retried by the next check); ≤ `AlertConfig.MAX_INDIVIDUAL_NOTIFICATIONS` (3) → `AlertNotifier.showAlerts`
  (oldest first), more → `showSummary`; one `alert_notification_posted` per shown notification.
- `EarthquakeAlertNotifier` builds with `AlertNotificationBuilder` from a context in the app language
  (`LocalizedContextProvider`): title with distance when an area is set, text place · local time, notification id =
  event id hash (the same event replaces its notification), summary = inbox style of the strongest events.
- Tap: detail `PendingIntent` = `VIEW` of `DeepLinkConfig.createEarthquakeDetailUri(id, isFromNotification = true)` for
  this package; summary = the launcher intent. Both carry `AlertNotificationTap` extras (event id or `summary`, posting
  time). `AppOpenTracker` (singleton) logs `app_opened` (source notification) and `alert_notification_opened` with the
  delay; a restore in the same process (rotation, language) is not an open, a restore after process death is.

## 5. Persistence
| Store | Content |
|---|---|
| Room `earthquakes` | Cached list (id, magnitude, magnitude type, place, time, lat, lon, depth, detail url, reviewed, tsunami flag, felt reports) |
| Room `notified_earthquakes` | id, notifiedAt — dedupe, pruned after 30 days |
| Room `analytics_events` | id, name, params (JSON string map), timestamp |
| DataStore `user_preferences` | onboardingCompleted, alertsEnabled, threshold, area (lat, lon, city, admin, countryCode, radiusKm), alertBaselineAt, lastCheckedAt, lastRefreshedAt |
| AppCompat locale storage | Selected app language (system-managed on API 33+, `autoStoreLocales` below) |
| SharedPreferences `app_language` | Copy of the selected language tag, read below API 33 when no activity has loaded AppCompat's locales (worker process, ADR-034) |
| SharedPreferences `app_theme` | Selected theme mode (system / light / dark), applied at start-up (ADR-043) |

## 6. Localization
- Default `values/strings.xml` = English, `values-tr/strings.xml` = Turkish. Plurals for counts/relative times.
- `androidResources { generateLocaleConfig = true }` + `res/resources.properties` (`unqualifiedResLocale=en`)
  generates `locales_config.xml` automatically (system per-app language settings on API 33+).
- A small Gradle snippet scans `src/main/res/values-*` for `strings.xml` and writes the language tags into
  `BuildConfig.SUPPORTED_LANGUAGE_TAGS`, which the in-app language picker reads.
- ⇒ **Adding a language = adding `values-<tag>/strings.xml`. Nothing else.**
- Language names shown in their own language (`Locale.forLanguageTag(tag).getDisplayName(thatLocale)`).
- Settings tab: "System default" + `AppLanguageManager.getSupportedLanguages()` in a radio dialog; a pick is applied
  at once (`setSelectedLanguage`, AppCompat recreates the activity, the Settings tab stays selected) and logs
  `language_changed`. The screen re-reads the language on resume, so a change in the system per-app language
  page (API 33+) shows up when the user returns.
- `MainActivity` extends `AppCompatActivity`; manifest declares `AppLocalesMetadataHolderService` with
  `autoStoreLocales=true` for API < 33. Must be verified on API < 33 and ≥ 33.
- Text built outside the UI (notifications, channel name) uses `LocalizedContextProvider`, a context in the language
  from `AppLanguageManager.getSelectedLanguage()` (AppCompat's locales; below API 33 in a process without an activity,
  the stored tag — ADR-034). App bundles keep every language in the base APK (`bundle.language.enableSplit =
  false`, ADR-032) so switching the in-app language never misses resources.
- USGS `place` text is English-only; it is stored as is and localized on display when the app language is not
  English (`formatPlace`: distance and direction, country names, region phrases and a 44-name dictionary; town
  names kept — ADR-044). Everything else we render (distance, dates,
  numbers, labels) is localized.

## 7. Design system
- Custom light and dark `ColorScheme` (dynamic color **off** for a consistent brand look), typography scale, shapes,
  spacing tokens (`Spacing`) in `core/ui/theme`; values come from the UI design (ADR-018).
- Magnitude severity colours as an extended theme token set (`LocalSeverityColors`, read via
  `QuakeAlertTheme.severityColors`): < 4, 4–5, 5–6, 6–7, ≥ 7. The bands live in `core/model/MagnitudeSeverity`.
- Shared state composables: `LoadingState`, `EmptyState`, `ErrorState`, `StaleDataBanner` (offline or failed refresh
  over cached data), `MagnitudeBadge`. `EmptyState`/`ErrorState` scroll themselves: never place them inside a lazy list.
- Formatting (`core/ui/format`): relative times from `core/time/RelativeTime` with plural resources (not `DateUtils`,
  which ignores the in-app language), numbers and dates with `LocalLocale`, a once-per-minute `rememberCurrentTime()`.
- Icons: `material-icons-core` (`Icons.Rounded.*`); icons it lacks, and the bottom-bar icons (outlined/filled pairs),
  are Material Symbols Rounded vector drawables (ADR-016).
- Edge-to-edge, custom adaptive launcher icon (vector layers only: background, foreground, monochrome = foreground;
  no bitmap fallbacks because minSdk 26 always uses the adaptive icon), `contentDescription` on all meaningful icons
  (decorative icons next to text use `null`), screen titles through `ScreenTitle` (marked as heading for TalkBack).

## 8. Error handling
- `AppResult<T>` = `Success(data)` | `Failure(error: AppError)` for expected failures.
- `AppError`: `Network`, `Server(code)`, `NotFound`, `Parsing`, `GeocoderUnavailable`, `Unknown`.
- Mapping from exceptions happens in the data layer only; UI maps `AppError` → string resources.

## 9. Conventions (from the project coding rules)
- Naming per screen: `XRoute` = navigation key (`navigation/Routes.kt`), `XEntry` = composable that wires the
  ViewModel and platform actions, `XScreen` = stateless composable (previews and UI tests use it).
- Explicit types on public functions/properties; no `Any`; data classes immutable (`val`, `List`).
- Functions short (< 20 statements), start with a verb; booleans `isX` / `hasX` / `canX`; no blank lines inside
  functions; early returns instead of nesting.
- Constants `UPPER_CASE` in a per-feature `*Config` object (e.g. `AlertConfig.THRESHOLD_RANGE`,
  `AlertConfig.RADIUS_OPTIONS_KM`, `AlertConfig.CHECK_INTERVAL`) — no magic numbers.
- Value classes for domain primitives where it helps (e.g. `Magnitude`, `EarthquakeId`).
- Composables stateless where possible (state hoisting); `LazyColumn` items always keyed; previews use
  `@PreviewLightDark`.
- Strings via `stringResource`; colours/typography via `MaterialTheme`; no hard-coded UI values.
- No comments in source, build or resource files; names carry the meaning, rationale lives in these docs.
- Clock and dispatchers injected so time-based logic is testable.
