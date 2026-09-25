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
│   ├── datastore/                   # UserPreferencesDataSource (DataStore)
│   ├── preferences/                 # UserPreferences model + UserPreferencesRepository (shared by features)
│   ├── model/                       # GeoPoint, AlertArea, Magnitude … pure Kotlin shared models
│   ├── location/                    # Distance (haversine) utilities
│   ├── network/                     # Retrofit/OkHttp/Json setup
│   ├── notification/                # Channels, NotificationPermissionChecker
│   ├── locale/                      # AppLanguage, AppLanguageManager (per-app language), supported languages
│   ├── error/                       # AppResult, AppError
│   ├── time/                        # Clock abstraction (testable "now"), formatters
│   ├── di/                          # App-wide Hilt modules (network, database, datastore, dispatchers, clock)
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
        └────────── EarthquakeRepositoryImpl ── UsgsRemoteDataSource (Retrofit)
                                             └─ EarthquakeLocalDataSource (Room DAO)
```
- Refresh fetches 7 days / M2.5+ worldwide, replaces the cache in one transaction, stores `lastRefreshedAt`.
- Filters and distances are computed in the domain layer (haversine) on the cached list.

### 4.2 Background alert check
```
WorkManager (periodic 15 min, NetworkType.CONNECTED)
  └─ AlertCheckWorker (thin) ─▶ CheckForNewAlertsUseCase
        ├─ UserPreferencesRepository (settings, baseline, lastCheckedAt)
        ├─ EarthquakeRepository.fetchUpdatedSince(query)   ← earthquakes.domain
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
  - Results filtered by `Address.countryCode == countryCode`, mapped to `City(name, adminArea, countryCode, point)`,
    de-duplicated.
- Must be verified on API < 33 **and** API ≥ 33 emulators (see TESTING.md).

### 4.4 Notification tap → detail
- `PendingIntent` to `MainActivity` with deep link `quakealert://earthquake/{id}?source=notification`.
- Detail route declares `navDeepLink`; analytics logs `alert_notification_opened` with delay since posting.

## 5. Persistence
| Store | Content |
|---|---|
| Room `earthquakes` | Cached list (id, magnitude, magType, place, time, updated, lat, lon, depth, url, status, tsunami, felt) |
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
- Shared state composables: `LoadingState`, `EmptyState`, `ErrorState`, `OfflineBanner`, `MagnitudeBadge`.
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
- Clock and dispatchers injected so time-based logic is testable.
