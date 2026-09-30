# QuakeAlert — Architecture

## 1. Overview
- Single Gradle module `:app`, single activity (`MainActivity`), 100% Jetpack Compose + Material 3.
- **Feature-first Clean Architecture:** code is grouped by feature; each feature has `data` / `domain` /
  `presentation`. Shared code lives in `core`.
- **MVVM + Unidirectional Data Flow** in presentation: `@HiltViewModel` exposes one immutable `UiState` via
  `StateFlow`; composables call ViewModel functions for events. One-off results (a developer-tools message, a located
  city) are fields of the UI state that the screen consumes, so they survive rotation; there are no event channels.
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
│   ├── time/                        # Clock abstraction (testable "now", time zone), RelativeTime
│   ├── di/                          # App-wide Hilt modules (network, database, datastore, dispatchers, app scope, clock)
│   └── ui/
│       ├── component/               # States, badges, SectionCard, NavigationCard, NotificationPermissionStatus, ScreenTitle
│       ├── format/                  # Dates, numbers, coordinates, relative times, localized USGS place text, fitting label size
│       └── theme/                   # Colours, severity colours, type, shapes, spacing
├── features/
│   ├── earthquakes/                 # List + detail + statistics tab + USGS data
│   │   ├── data/model | source | repository
│   │   ├── domain/model | repository | usecase   (+ EarthquakesConfig)
│   │   ├── presentation/viewmodel | screen | component
│   │   └── di/
│   ├── alerts/                      # Alert settings, city search (Geocoder), "Use my location", preview, feedback, onboarding, worker, notifications
│   │   ├── data/model | source (CityGeocoder, DeviceLocationSource + Android implementations) | repository
│   │   ├── domain/model | repository | usecase   (+ AlertConfig, AlertMatcher at the domain root)
│   │   ├── worker/                  # AlertCheckWorker, SimulatedAlertWorker, AlertWorkScheduler
│   │   ├── notification/            # EarthquakeAlertNotifier, AlertNotificationBuilder
│   │   ├── presentation/viewmodel | screen | component   (settings, onboarding, preview, feedback, developer alert testing)
│   │   └── di/
│   ├── settings/                    # Language, theme, notification status, about, developer tools screen
│   │   └── presentation/viewmodel | screen | component   (+ SettingsConfig; no data/domain: reads core only)
│   ├── eventlog/                    # Developer event log + on-device alert metrics: data (AnalyticsEventDao) | domain | presentation
│   └── emergency/                   # Emergency tools tab (whistle, strobe light) + safety guide; core only
│       ├── domain/                  # EmergencyConfig, TorchController + WhistlePlayer (interfaces), model/WhistlePattern, usecase/RunStrobeUseCase
│       ├── data/source/             # CameraTorchController (CameraManager.setTorchMode), AudioTrackWhistlePlayer + WhistleWaveform
│       ├── presentation/viewmodel | screen | component
│       └── di/
```

### Cross-feature rules
- The **only** cross-feature dependency: `alerts` uses `earthquakes.domain` (`EarthquakeRepository` interface and
  domain model) to fetch earthquakes. Never `earthquakes.data`.
- Shared user preferences (alerts enabled, threshold, area, onboarding flag, baseline, last check) live in
  `core/preferences` because both the list (filters, distance) and alerts need them.
- Features never import each other's screens; the `navigation` package wires all routes.
- `emergency` depends on `core` only. The Statistics tab lives in `earthquakes` (a view over earthquake data), so it
  adds no cross-feature dependency. The tappable card with a chevron is shared as `core/ui/component/NavigationCard`
  (Settings, Developer tools, Emergency).
- Onboarding lives in `alerts` because it is the alert setup flow (reuses the same threshold/area components).

## 4. Localization
- Default `values/strings.xml` = English, `values-tr/strings.xml` = Turkish. Plurals for counts/relative times.
- ⇒ **Adding a language = adding `values-<tag>/strings.xml`.**
- Language names shown in their own language (`Locale.forLanguageTag(tag).getDisplayName(thatLocale)`).
- USGS `place` text is English-only; it is stored as is and localized on display when the app language is not
  English (`formatPlace`: distance and direction, country names, region phrases and a 47-name dictionary; town
  names kept). Everything else we render (distance, dates, numbers, labels) is localized.

## 5. Design system
- Custom light and dark `ColorScheme`, typography scale, shapes, spacing tokens (`Spacing`) in `core/ui/theme`;
  values come from the UI design.
- Icons: `material-icons-core` (`Icons.Rounded.*`); icons it lacks, and the bottom-bar icons (outlined/filled pairs),
  are Material Symbols Rounded vector drawables.

## 6. Error handling
- `AppResult<T>` = `Success(data)` | `Failure(error: AppError)` for expected failures.
- `AppError`: `Network`, `Server(code)`, `NotFound`, `Parsing`, `GeocoderUnavailable`, `LocationPermissionDenied`,
  `LocationDisabled`, `LocationUnavailable`, `Unknown`.

## 7. Conventions
- Strings via `stringResource`; colours/typography via `MaterialTheme`; no hard-coded UI values.
- No comments in source, build or resource files; names carry the meaning, rationale lives in these docs.
