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
├── navigation/                      # Routes, NavHost, onboarding graph, main graph, bottom bar, deep links
├── core/
│   ├── analytics/                   # AnalyticsTracker (interface), AnalyticsEvent, LocalAnalyticsTracker
│   ├── database/                    # QuakeAlertDatabase, entities, DAOs (earthquake cache, notified ids, events)
│   ├── datastore/                   # DataStoreUserPreferencesRepository (DataStore keys + mapping)
│   ├── preferences/                 # UserPreferences, AlertSettings + UserPreferencesRepository (shared by features)
│   ├── model/                       # GeoPoint, City, AlertArea, MagnitudeSeverity … pure Kotlin shared models
│   ├── location/                    # Distance (haversine) utilities
│   ├── network/                     # Retrofit/OkHttp/Json setup
│   ├── notification/                # Channels, NotificationPermissionChecker
│   ├── locale/                      # AppLanguage, AppLanguageManager (per-app language), supported languages
│   ├── error/                       # AppResult, AppError
│   ├── time/                        # Clock abstraction (testable "now"), formatters
│   ├── di/                          # App-wide Hilt modules (network, database, datastore, dispatchers, app scope, clock)
│   └── ui/                          # Reusable composables (states, chips, badges) + theme/
├── features/
│   ├── earthquakes/                 # List + detail + USGS data
│   │   ├── data/model | source | repository
│   │   ├── domain/model | repository | usecase
│   │   ├── presentation/viewmodel | screen | component
│   │   └── di/
│   ├── alerts/                      # Alert settings, city search (Geocoder), onboarding, worker, notifications
│   │   ├── data/source (AndroidCityGeocoder) | repository
│   │   ├── domain/model | repository | usecase
│   │   ├── worker/                  # AlertCheckWorker, AlertWorkScheduler
│   │   ├── notification/            # EarthquakeAlertNotifier
│   │   ├── presentation/viewmodel | screen | component   (settings, onboarding, developer tools)
│   │   └── di/
│   ├── settings/                    # Language, about, permission status, developer entry points
│   └── eventlog/                    # Developer event log (reads core/analytics)
```

### Cross-feature rules
- The **only** cross-feature dependency: `alerts` uses `earthquakes.domain` (`EarthquakeRepository` interface and
  domain model) to fetch earthquakes. Never `earthquakes.data`. See ADR-005.
- Shared user preferences (alerts enabled, threshold, area, onboarding flag, baseline, last check) live in
  `core/preferences` because both the list (filters, distance) and alerts need them.
- Features never import each other's screens; the `navigation` package wires all routes.
- Onboarding lives in `alerts` because it is the alert setup flow (reuses the same threshold/area components).

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
- Detail by id: cache first, then USGS `eventid` (404 or a non-earthquake event → `NotFound`); the network result is
  not written to the cache, which always mirrors the last list refresh.
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
        ├─ AlertMatcher (rules SPEC §5.1, pure Kotlin, unit-tested)
        ├─ NotifiedEarthquakeRepository (dedupe, prune 30 days)
        ├─ AlertNotifier (interface) ─▶ EarthquakeAlertNotifier (Android)
        └─ AnalyticsTracker
```
- `AlertWorkScheduler` enqueues unique periodic work (`ExistingPeriodicWorkPolicy.UPDATE`) on app start and on
  settings change when alerts are enabled; cancels it when disabled.
- Developer "Simulate alert" builds a fake `Earthquake` matching current settings and runs it through the same
  matcher + notifier path. "Run check now" enqueues an expedited `OneTimeWorkRequest` of the same worker.
- The worker uses Hilt via `HiltWorkerFactory`; the default WorkManager initializer is removed from the manifest.

### 4.3 City search (Geocoder wrapper)
```
CitySearchViewModel ─▶ SearchCitiesUseCase ─▶ CitySearchRepository (domain interface)
                                                  └─ AndroidCityGeocoder (data)
```
- `AndroidCityGeocoder.search(query, countryCode, locale)`:
  - `Geocoder.isPresent() == false` → `AppError.GeocoderUnavailable`.
  - **API ≥ 33:** `getFromLocationName(name, max, GeocodeListener)` wrapped in `suspendCancellableCoroutine`
    (`onError` → failure).
  - **API < 33:** deprecated blocking `getFromLocationName(name, max)` on the IO dispatcher (`@Suppress` scoped to
    that one function).
  - `IOException` → network error; empty → no results.
  - Results filtered by `Address.countryCode == countryCode`, mapped to `City(name, adminArea, countryCode, location)`
    (`core/model`),
    de-duplicated.
- Must be verified on API < 33 **and** API ≥ 33 emulators (see TESTING.md).

### 4.4 Notification tap → detail
- `PendingIntent` to `MainActivity` with deep link `quakealert://earthquake/{id}?source=notification`.
- Detail route declares `navDeepLink`; analytics logs `alert_notification_opened` with delay since posting.

## 5. Persistence
| Store | Content |
|---|---|
| Room `earthquakes` | Cached list (id, magnitude, magnitude type, place, time, lat, lon, depth, detail url, reviewed, tsunami flag, felt reports) |
| Room `notified_earthquakes` | id, notifiedAt — dedupe, pruned after 30 days |
| Room `analytics_events` | id, name, params (JSON string map), timestamp |
| DataStore `user_preferences` | onboardingCompleted, alertsEnabled, threshold, area (lat, lon, city, admin, countryCode, radiusKm), alertBaselineAt, lastCheckedAt, lastRefreshedAt |
| AppCompat locale storage | Selected app language (system-managed on API 33+, `autoStoreLocales` below) |

## 6. Localization
- Default `values/strings.xml` = English, `values-tr/strings.xml` = Turkish. Plurals for counts/relative times.
- `androidResources { generateLocaleConfig = true }` + `res/resources.properties` (`unqualifiedResLocale=en`)
  generates `locales_config.xml` automatically (system per-app language settings on API 33+).
- A small Gradle snippet scans `src/main/res/values-*` for `strings.xml` and writes the language tags into
  `BuildConfig.SUPPORTED_LANGUAGE_TAGS`, which the in-app language picker reads.
- ⇒ **Adding a language = adding `values-<tag>/strings.xml`. Nothing else.**
- Language names shown in their own language (`Locale.forLanguageTag(tag).getDisplayName(thatLocale)`).
- `MainActivity` extends `AppCompatActivity`; manifest declares `AppLocalesMetadataHolderService` with
  `autoStoreLocales=true` for API < 33. Must be verified on API < 33 and ≥ 33.
- USGS `place` text is English-only and shown as is (documented limitation); everything we render ourselves
  (distance, dates, numbers, labels) is localized.

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
- Edge-to-edge, custom adaptive launcher icon, `contentDescription` on all meaningful icons.

## 8. Error handling
- `AppResult<T>` = `Success(data)` | `Failure(error: AppError)` for expected failures.
- `AppError`: `Network`, `Server(code)`, `NotFound`, `Parsing`, `GeocoderUnavailable`, `Unknown`.
- Mapping from exceptions happens in the data layer only; UI maps `AppError` → string resources.

## 9. Conventions (from the project coding rules)
- Explicit types on public functions/properties; no `Any`; data classes immutable (`val`, `List`).
- Functions short (< 20 statements), start with a verb; booleans `isX` / `hasX` / `canX`; no blank lines inside
  functions; early returns instead of nesting.
- Constants `UPPER_CASE` in a per-feature `*Config` object (e.g. `AlertConfig.THRESHOLD_RANGE`,
  `AlertConfig.RADIUS_OPTIONS_KM`, `AlertConfig.CHECK_INTERVAL_MINUTES`) — no magic numbers.
- Value classes for domain primitives where it helps (e.g. `Magnitude`, `EarthquakeId`).
- Composables stateless where possible (state hoisting); `LazyColumn` items always keyed; previews use
  `@PreviewLightDark`.
- Strings via `stringResource`; colours/typography via `MaterialTheme`; no hard-coded UI values.
- No comments in source, build or resource files; names carry the meaning, rationale lives in these docs.
- Clock and dispatchers injected so time-based logic is testable.
