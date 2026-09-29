# Architecture Decision Records

Short records of decisions that shape the app. Format: Context → Decision → Alternatives → Consequences.
Add a new record (next number) whenever a significant decision is made; never rewrite history — supersede instead.

---

## ADR-001 — Native Android with Kotlin and Jetpack Compose
- **Context:** One platform is enough to deliver the full experience; the team's strongest platform is Android.
- **Decision:** Native Android, Kotlin, Compose + Material 3, single activity.
- **Alternatives:** Native iOS; cross-platform (Flutter/KMP) — no benefit for a single-platform product.
- **Consequences:** Modern declarative UI, first-class WorkManager/notification APIs.

## ADR-002 — USGS as the data source
- **Context:** The product is global. We need a keyless, documented, reliable API with global coverage.
- **Decision:** USGS FDSN event service (GeoJSON).
- **Alternatives:** AFAD / Kandilli (Türkiye only; Kandilli has no official API), EMSC (global, less standard query
  model).
- **Consequences:** Worldwide coverage with one integration, native circle filters and `updatedafter`. Completeness
  for small events varies by region (measured: Türkiye, one week — USGS 2 events vs. AFAD 713). Documented as a
  limitation; regional sources are a next step, and the data source sits behind a repository interface.

## ADR-003 — WorkManager periodic work for background checks (no backend)
- **Context:** Without a server there is no push. The device itself must poll USGS.
- **Decision:** Unique periodic `CoroutineWorker` every 15 minutes (WorkManager minimum) with a network constraint.
- **Alternatives:**
  - FCM push via a backend — instant even in Doze, but requires a server (main next step).
  - Exact alarms (`setExactAndAllowWhileIdle`) — at most once per 9 min in Doze and requires the user to grant
    "Alarms & reminders" (not granted by default on Android 14+).
  - Foreground service polling — ~1 min, but a permanent notification, battery cost and Android 15 limits.
- **Consequences:** Battery-friendly and reliable, survives reboot. Alerts may arrive up to ~15 min late (longer in
  Doze). Stated in onboarding and README.

## ADR-004 — Feature-first Clean Architecture with MVVM, single module
- **Context:** Code must be easy to navigate, explain and change.
- **Decision:** Packages grouped by feature (`earthquakes`, `alerts`, `settings`, `eventlog`) with
  data/domain/presentation layers; shared code in `core`; MVVM + UDF in presentation. One Gradle module.
- **Alternatives:** Layer-first packages (harder to find feature code); multi-module Gradle (build isolation not
  needed at this size, adds setup overhead).
- **Consequences:** Clear ownership and boundaries; modules can be extracted later along feature lines.

## ADR-005 — `alerts` depends only on `earthquakes` domain
- **Context:** The alert check needs earthquake data owned by `earthquakes`. Features should depend on each other
  as little as possible.
- **Decision:** `alerts` uses the `EarthquakeRepository` interface and domain models from `earthquakes.domain`;
  never its data layer. This is the only cross-feature dependency.
- **Alternatives:** Duplicate the USGS client in `alerts` (two sources of truth); depend on `earthquakes.data`
  (couples alerts to Retrofit/Room details); move everything to `core` (core becomes a feature).
- **Consequences:** Data source changes never touch alerts; alert logic is tested with a fake repository; the
  "Simulate alert" tool reuses the real matching path.

## ADR-006 — Shared user preferences in `core/preferences`
- **Context:** The list (filters, distance) and alerts (worker, settings) both need threshold and area.
- **Decision:** A `UserPreferencesRepository` in core backed by DataStore; alerts writes, others read.
- **Alternatives:** List depends on `alerts.domain` (would create a two-way feature dependency).
- **Consequences:** No feature cycle; one place for persisted user choices.

## ADR-007 — Area model: circle (city + radius) with USGS native parameters
- **Context:** Users want alerts "near a place". USGS has no country/city filter; `place` strings are free text
  ("Japan region", "CA", "south of the Fiji Islands") and country bounding boxes include neighbours.
- **Decision:** Area = center point + radius (50/100/250/500/1000 km). Background queries use USGS
  `latitude`/`longitude`/`maxradiuskm`; the list filters the cached global data locally with the same haversine
  rule. Country selection only narrows the city search. Default "Whole world" with a visible warning.
- **Alternatives:** Parse `place` for a country (unreliable); country bounding boxes (false positives); bundled city
  database (app size, maintenance).
- **Consequences:** Matches how earthquakes are felt (borders don't matter, distance does). GPS-based area later is
  just another way to pick the center.

## ADR-008 — Countries from the platform, cities from Android `Geocoder`
- **Context:** City coordinates are needed without bundling data or adding external APIs.
- **Decision:** Countries: `Locale.getISOCountries()` with localized names. Cities: platform `Geocoder`
  ("type and search", no live suggestions), results filtered by country code. Async API on API 33+, blocking API
  on the IO dispatcher below 33, behind one wrapper.
- **Alternatives:** Open-Meteo geocoding (extra external dependency), bundled city list, USGS Geoserve places (no
  name search).
- **Consequences:** No new dependency or data. Requires the device geocoding backend (`Geocoder.isPresent()`);
  otherwise city search is hidden and the area stays "Whole world". Tested on API < 33 and ≥ 33.

## ADR-009 — Offline-first list with a Room cache
- **Context:** Users open the app right after feeling a quake, often on bad networks.
- **Decision:** Fetch last 7 days, M2.5+, worldwide (~370 events, ~265 KB); cache in Room; UI observes Room; filters
  are local.
- **Alternatives:** Query per filter (no offline story, more requests).
- **Consequences:** Instant filter changes and an offline mode with "data from <time>" banner.

## ADR-010 — Alert deduplication, baseline and max age
- **Context:** A user must never be notified twice for the same event, nor for old events after changing settings
  or coming back online; USGS publishes and revises events late.
- **Decision:** Query with `updatedafter` (+10 min overlap); store notified ids in Room (30-day prune); ignore
  events older than the alert baseline (last settings change) or older than 6 h.
- **Consequences:** Late-published events are caught; no duplicate or retroactive alerts.

## ADR-011 — Local analytics behind an `AnalyticsTracker` interface
- **Context:** We need to measure whether the product works; a remote service is not required yet.
- **Decision:** `AnalyticsTracker` interface; `LocalAnalyticsTracker` writes to Room (+ Logcat); developer Event log
  screen. No PII (no coordinates or city names).
- **Alternatives:** Firebase Analytics now (setup, privacy review, not needed to validate the loop).
- **Consequences:** A remote tracker (e.g. Firebase) can be added later as another implementation — listed in next
  steps.

## ADR-012 — Multilingual via resources + per-app language
- **Context:** The app is global; English and Turkish now, more later with minimum effort.
- **Decision:** String resources, AppCompat per-app language API, generated locale config and generated supported
  language list. Adding a language = adding one `strings.xml`.
- **Consequences:** `MainActivity` extends `AppCompatActivity`; behaviour verified on API < 33 and ≥ 33.

## ADR-013 — minSdk 26
- **Context:** Template default was 24.
- **Decision:** minSdk 26 (Android 8.0).
- **Alternatives:** 24 with core library desugaring and pre-O notification code paths.
- **Consequences:** Native `java.time`, notification channels always available, adaptive icons; covers the vast
  majority of active devices.

## ADR-014 — Developer tools for simulated alerts
- **Context:** Real alerts depend on real earthquakes and a 15-minute schedule — hard to demonstrate and test.
- **Decision:** Debug-only "Simulate alert" (fake matching event through the real matcher + notifier) and "Run check
  now" (one-time run of the real worker).
- **Consequences:** The full notification loop can be verified in seconds; not shipped in release builds.

## ADR-015 — Toolchain: AGP 9 with built-in Kotlin, compileSdk 36 with pinned libraries
- **Context:** The build starts on current stable tooling. The newest Compose BOM (2026.08.00+), `androidx.core`
  1.19 and `androidx.lifecycle` 2.11 require compileSdk 37 (Android 17 SDK).
- **Decision:** Gradle 9.8.0, AGP 9.4.1 with its built-in Kotlin support (no `org.jetbrains.kotlin.android` plugin),
  Kotlin 2.4.20 (set through the Compose compiler plugin version), KSP 2.3.12, Java 17. Stay on compileSdk/targetSdk
  36 and pin the latest library versions that support it: Compose BOM 2026.06.01, core-ktx 1.18.0, lifecycle 2.10.0.
- **Alternatives:** compileSdk 37 with target 36 (newest libraries, needs the API 37 SDK platform on every dev
  machine); compileSdk + target 37 (also brings Android 17 behaviour changes into the QA matrix).
- **Consequences:** Builds on the SDK platforms already used for testing, with no extra setup. Compose stays one minor
  release behind. Moving to compileSdk 37 later means changing one number plus the pinned versions in
  `gradle/libs.versions.toml`. AGP 9.4 requires Android Studio Quail 4 (2026.1.4) or newer.

## ADR-016 — Icons from `material-icons-core` plus Material Symbols drawables
- **Context:** The app needs a few dozen standard icons (tabs, refresh, share, map, warning…). The Compose
  `material-icons-*` libraries are no longer updated (frozen at 1.7.8); `material-icons-extended` holds thousands of
  icons and slows debug builds.
- **Decision:** Use `material-icons-core` (`Icons.Default.*`, via the Compose BOM) for common icons. Any icon it lacks
  is added as a Material Symbols vector drawable in `res/drawable` (Material Symbols: Apache License 2.0).
- **Alternatives:** `material-icons-extended` (every icon in code, heavy); only vector drawables (more files for icons
  the core set already has).
- **Consequences:** Small dependency, familiar `Icons.Default.X` API; missing icons are single XML files.

## ADR-017 — Unit tests on JUnit Jupiter 6
- **Context:** Unit tests need parameterized tests and readable names; the current JUnit major version is 6 (same
  Jupiter API as JUnit 5, requires Java 17 — already our baseline).
- **Decision:** JUnit Jupiter 6 through the `de.mannodermaus.android-junit` Gradle plugin for `src/test`, with MockK,
  Turbine and kotlinx-coroutines-test. Instrumented tests stay on the JUnit 4 runner that the Compose test rule needs.
- **Alternatives:** JUnit 4 everywhere (no parameterized tests without extra runners, older API).
- **Consequences:** Two test APIs in the project, split cleanly by source set (`test` = Jupiter, `androidTest` = JUnit 4).

## ADR-018 — Design system taken from the UI design, with two deliberate deviations
- **Context:** The screens and the design system ("Seismic Precision Material") were designed in a UI design tool.
  It provides full light colour roles, a 5-level severity scale, type scale, shapes and spacing, but only hints for
  the dark scheme, and it specifies Roboto Flex.
- **Decision:** Light colours are the design's values. The dark scheme is generated with Material Color Utilities
  from the same palettes (fidelity variant; regenerating the light scheme this way matched the design on 34 of 35
  roles, the 35th being `surfaceVariant = surfaceContainerHighest`, which we follow). Deviations: (1) dark "major"
  severity is `#A8353F`/`#FFEDEC` instead of `#FFB2BC`, which was indistinguishable from dark "strong" (`#FFB4AB`,
  contrast 1.0); (2) the system Roboto font instead of bundling Roboto Flex (same metrics, no download or APK cost).
  Screens follow the design's visual language but only for in-scope features (no map, account or search elements).
- **Alternatives:** Hand-picked dark colours (drift from the light palette); downloadable Roboto Flex via Google
  Fonts (needs Play services, async loading and a fallback).
- **Consequences:** Both themes derive from one palette definition; every badge colour pair meets WCAG AA
  (≥ 4.5:1). The severity palette was later revised to follow the design more closely (ADR-042). Changing the brand colour means regenerating `Color.kt` from the same palettes.

## ADR-019 — User preferences: one DataStore repository, baseline saved with the alert settings
- **Context:** ADR-006 puts shared preferences in `core`. The alert rules (SPEC §5.1) require that a change of alert
  settings never alerts for events from before the change, and the default threshold is needed by both the list
  filter and alerts before the user has saved anything.
- **Decision:** `UserPreferencesRepository` (interface, `core/preferences`) with a single implementation,
  `DataStoreUserPreferencesRepository` (`core/datastore`), which owns the keys and the mapping. Alert settings are
  saved only through `saveAlertSettings(settings, baselineAt)`, so a new baseline is written in the same edit. The
  area is a sealed `AlertArea` (`WholeWorld` | `AroundCity`); an incomplete stored area reads as whole world.
  Defaults for persisted values live next to the model (`AlertSettings.DEFAULT`, threshold 4.5, alerts enabled);
  the alerts feature's `AlertConfig` reuses that constant instead of redefining it.
- **Alternatives:** A separate `UserPreferencesDataSource` wrapped by a repository (a pass-through layer with no
  logic of its own); separate setters for threshold, area and baseline (easy to forget the baseline); nullable
  stored threshold with the default in `alerts` (the list would need the alerts feature).
- **Consequences:** One class to read to see what is stored and how. Features depend on the interface and are
  tested with a fake. Key names are part of the file format; renaming one drops the stored value.

## ADR-020 — One Room database, versioned with migrations from the first schema
- **Context:** Analytics events, the earthquake cache and notified ids all live in Room (`QuakeAlertDatabase`). Tables
  are added task by task, and devices that already run the app must keep working after an update.
- **Decision:** One database file (`quakealert.db`). Schemas are exported to `app/schemas` and kept in git. Every
  schema change increases the version and adds a migration (`@AutoMigration` where Room can derive it). No
  destructive fallback.
- **Alternatives:** `fallbackToDestructiveMigration` (silently drops the event log and notified ids — the latter
  would allow duplicate alerts); one database per feature (more setup, no cross-table transactions).
- **Consequences:** Upgrades are safe and reviewable (schema JSON diffs). Adding a table is a version bump plus
  one annotation.

## ADR-021 — Earthquake repository without extra data source classes and without product rules
- **Context:** The list, the detail and the background check all read USGS data. The planned layout had a remote and
  a local data source between the repository and Retrofit/Room.
- **Decision:** `EarthquakeRepositoryImpl` talks to `UsgsApi` and `EarthquakeDao` directly. It only moves data: the
  caller passes an `EarthquakeQuery` (time window, minimum magnitude, area, `updatedAfter`), and use cases own the
  product values (7 days, M2.5+, alert threshold). Detail lookups read the cache first and fall back to USGS without
  writing the result into the cache.
- **Alternatives:** Remote/local data source wrappers (one-line pass-through classes); repository methods with the
  query rules built in (the list and the alert check would need different methods for the same call).
- **Consequences:** One class shows the whole data flow. Changing what the list or the alerts fetch is a use case
  change. The cache holds the events of the last list refresh; since ADR-048, any later fetch replaces the cached copy
  of an event it returns (never adds one), so a revised magnitude reaches the list and the detail too.

## ADR-022 — No comments in source, build and resource files
- **Context:** Comments drift away from the code they describe, and the rationale for decisions already lives in
  these docs.
- **Decision:** Code, Gradle and resource files carry no comments; names (types, functions, constants, tests) carry
  the meaning. Rationale goes into `docs/DECISIONS.md` and `docs/ARCHITECTURE.md`.
- **Consequences:** Names are chosen to be self-explanatory (e.g. `toEarthquakeOrNull`, `refreshCache`), and a test
  name states the rule it checks. Third-party attributions (Material Symbols, Apache License 2.0) are recorded in
  the docs instead of the drawable files.

## ADR-023 — The earthquake list is one in-memory list, computed off the main thread
- **Context:** The list shows every cached earthquake of the last 7 days (M2.5+, ~320–380 items, ~265 KB of JSON) and
  can now be sorted by time, magnitude or distance. The question was whether showing all of them at once costs too
  much and needs paging.
- **Measurement** (API 31 and 34 emulators, debug build, 321 items): the `LazyColumn` composes ~7 cards at any time,
  also after scrolling to the end, so rendering does not grow with the list. Mapping the Room rows + filtering +
  distance + sorting took 1–13 ms per change and ran on the main thread (a frame is 16 ms). Emulator frame timings
  were not used: the emulators render with SwiftShader (CPU), which says nothing about list size.
- **Decision:** Keep the full period as one list, fetched in one request and filtered/sorted in memory (ADR-009).
  Run the use case's `combine` on an injected `@DefaultDispatcher` with `flowOn`. No paging.
- **Alternatives:** In-memory "load 10 more" (started, removed: the data is already loaded and sorted, and the lazy
  list already renders only visible rows — it only added state); Paging 3 over Room with SQL filtering/sorting
  (distance filter and sort need haversine in SQL, more code, no benefit at this size); USGS `limit`/`offset` paging
  (more requests, breaks offline filtering and sorting of the whole period).
- **Consequences:** Simple code, instant sort and filter changes, smooth main thread. Revisit if the cached period grows
  by an order of magnitude (e.g. 30 days or M1+, several thousand items): then Paging 3 with SQL-side filtering.

## ADR-024 — Screen arguments through assisted ViewModel injection; navigation arguments stay primitive
- **Context:** The detail screen needs the earthquake id and where it was opened from (for analytics). Reading them
  from `SavedStateHandle.toRoute()` ties the ViewModel to the navigation library and needs Android classes in unit
  tests. The first draft passed the analytics `DetailSource` enum as a route argument; lint warned that enum
  navigation arguments can be broken by R8 in minified builds unless kept.
- **Decision:** The entry composable reads the route and creates the ViewModel with Hilt assisted injection
  (`@HiltViewModel(assistedFactory = ...)`, `hiltViewModel(creationCallback)`), passing plain values. Route
  arguments are primitives (`earthquakeId: String`, `isFromNotification: Boolean`); the navigation layer maps them to
  domain or analytics types.
- **Alternatives:** `SavedStateHandle` key lookups (string keys duplicated from the route); `@Keep` on the enum
  (couples analytics types to navigation and relies on a build rule).
- **Consequences:** ViewModels are constructed in unit tests with ordinary arguments; routes and deep links use simple
  query values (`?isFromNotification=true`); nothing depends on keep rules.

## ADR-025 — Deep links are handled in place, never by restarting the task
- **Context:** Notification taps and `adb am start` deliver `quakealert://earthquake/{id}` with
  `FLAG_ACTIVITY_NEW_TASK`. Navigation 2.9's `NavController.handleDeepLink(intent)` treats `NEW_TASK` without
  `CLEAR_TASK` as "unknown task state": it restarts the task with `TaskStackBuilder` and finishes the current
  activity. The activity would be created twice (double `app_opened`, a visible flash, lost state).
- **Decision:** `MainActivity` is `singleTop`. Before the intent reaches the NavController (in `onCreate` and
  `onNewIntent`), our own deep-link intents get `FLAG_ACTIVITY_CLEAR_TASK` added (`withDeepLinkHandledInPlace`). The
  flag is only read by the NavController at that point; it then clears its back stack and builds list → detail in
  place. Cold starts are handled while the graph is set (before the first frame), warm starts through an
  `OnNewIntentListener`.
- **Alternatives:** Default behaviour (task restart, see context); taking the URI out of the intent and calling
  `handleDeepLink(NavDeepLinkRequest)` after the first composition (tried: the list was shown for a frame and logged a
  list view and started a refresh); parsing the URI and navigating manually (duplicates the route's deep-link
  definition).
- **Consequences:** One activity instance, correct back stack, analytics counted once, state survives rotation (the
  NavController remembers that the link was handled). A deep link replaces the current back stack (for example the
  Alerts tab), like the library's own new-task behaviour.

## ADR-026 — Alert matching is one pure function over an explicit criteria object
- **Context:** SPEC §5.1 defines six rules that decide whether an earthquake alerts. The background check (2.6) and
  the developer "Simulate alert" (2.8) must apply exactly the same rules, and every rule needs boundary tests.
- **Decision:** `AlertMatcher` (`alerts/domain`) is a stateless class with no dependencies. Everything a rule reads
  comes in one `AlertMatchCriteria` (settings, baseline, notified ids, check time); the caller loads it from the
  preferences, Room and the `Clock`. All limits are inclusive (`magnitude >= threshold`, `distance <= radius`,
  `time >= baseline`, `time >= checkedAt - 6 h`). A missing baseline (alert settings never saved) matches nothing.
  Tunable alert values live in `AlertConfig`; the default threshold reuses `AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD`
  (ADR-019).
- **Alternatives:** Rules inside `CheckForNewAlertsUseCase` (needs fakes for USGS, Room and notifications to test a
  single boundary; the simulate path would duplicate them); a matcher that reads the repositories itself (suspending,
  harder to test); returning a rejection reason per rule (no consumer yet).
- **Consequences:** The rules are read and changed in one short file and tested without coroutines or fakes. A user
  who never saved alert settings gets no alerts for events from before the setup, instead of a burst of old ones.

## ADR-027 — City search rules live in the repository, behind a thin geocoder interface
- **Context:** City search depends on the platform `Geocoder`, which cannot run in unit tests and has two APIs
  (async from API 33, blocking below). The search rules (country filter, de-duplication, error mapping, which
  results count as a city) must be unit tested. ADR-021 avoids data source classes that only pass calls through.
- **Decision:** `CityGeocoder` (data/source) is a small interface; `AndroidCityGeocoder` is the only class that uses
  `android.location`, handles the API 33 split and returns plain `GeocodedAddress` values. `CitySearchRepositoryImpl`
  holds every rule and is tested with a fake geocoder. The geocoder is asked for `"<name>, <country name>"` in the
  app language. Results without locality or admin area are the country itself (returned for unknown names) and are
  dropped unless their name equals the typed name, which keeps city states. Countries come from
  `GetCountriesUseCase` (pure JVM `Locale` + `Collator`, no data layer).
- **Alternatives:** Rules inside `AndroidCityGeocoder` (only testable on a device); searching the bare name and
  filtering by country (tested: "Paris" in the US found nothing, the geocoder returns the best global match first);
  dropping every country-level result (loses Singapore, Monaco, Hong Kong).
- **Consequences:** The Android-specific part is ~50 lines and verified on both API sides; everything else is
  covered by fast unit tests. A city state is found by its name in the app language only ("Singapur" in Turkish).

## ADR-028 — Area components are stateless; one shared ViewModel owns city search
- **Context:** The threshold and area controls appear in the alert settings screen (2.4) and in the onboarding setup
  step (2.7). City search has its own state (countries, default country, loading / results / errors, analytics),
  while the chosen threshold and area belong to the screen that saves them.
- **Decision:** All controls are stateless composables. The hosting screen keeps the choice as an `AreaSelection`
  (mode, city, radius) and saves it only when `toAlertAreaOrNull()` returns an area. `AreaSelectorEntry` connects the
  controls to `CitySearchViewModel`, which both screens use: it builds the country list for the app language, picks
  the device region as the default country, runs searches and logs the search events. Transient UI state (typed
  text, open picker, "changing city") stays in `rememberSaveable`. The threshold slider reports a value when the drag
  ends, so a drag is one change. The whole-world warning uses the stale-data banner colours because the design's
  tertiary container is dark in both themes.
- **Alternatives:** City search inside each screen's ViewModel (the same logic twice); one ViewModel for the whole
  alert setup shared by settings and onboarding (the two flows save at different moments); country selection in the
  host screen (every host would need the device region and country list).
- **Consequences:** Screens stay small: they hold one `AreaSelection` and save it. The components have previews and
  Compose tests without Hilt. A city-search change is made in one ViewModel.

## ADR-029 — Alert settings save on every change, with a new baseline each time
- **Context:** The Alerts tab edits the switch, threshold and area. SPEC §5.1 needs a baseline so a change never
  alerts for older events. Settings are also read by the earthquake list (threshold chip, "Near city", distances).
- **Decision:** No Save button: each change is saved at once through `UpdateAlertSettingsUseCase`, which serializes
  updates with a mutex, reads the stored settings, applies the change and saves it with `baselineAt = now` only when
  something changed. The switch also resets the baseline, so turning alerts back on never notifies about events from
  while they were off. The ViewModel derives analytics from the returned previous/updated pair. (The "saved"
  snackbar was removed on 2026-09-29: the controls already show the new value and the summary card updates, so a
  message after every slider or radius change was noise.) "Near a city" without a city is only an on-screen draft. Scheduling / cancelling the background
  work will be triggered from the same use case (2.6).
- **Alternatives:** Explicit Save button (easy to leave the screen with unsaved changes; the list would show old
  settings); one repository call per field (baseline easy to forget, see ADR-019); analytics logged per UI callback
  (would also log no-op taps).
- **Consequences:** What the user sees is always what is stored, and the list reflects it immediately. Rapid changes
  cannot overwrite each other. Each change restarts the "only newer events" window, which is the intended behaviour.

## ADR-030 — Notification policy in the domain, rendering on Android, analytics per notification
- **Context:** SPEC §5.2 says one notification per event up to 3, otherwise one summary. The background check (2.6)
  and "Simulate alert" (2.8) both notify. The north-star metric is opened / posted notifications.
- **Decision:** `NotifyAlertsUseCase` decides: nothing, suppressed (permission missing, logged), individual (≤ 3,
  oldest first) or summary. `AlertNotifier` (domain interface) only renders: `EarthquakeAlertNotifier` +
  `AlertNotificationBuilder` build localized texts and tap intents. `alert_notification_posted` is logged once per
  notification shown (the summary as `event_id=summary` with the largest magnitude and the batch size), and taps
  carry the event id and posting time in intent extras (`AlertNotificationTap`) so `alert_notification_opened` has a
  delay. The detail deep link builder moved to `core/navigation/DeepLinkConfig` so a feature can create it.
- **Alternatives:** Policy inside the Android notifier (only testable on a device); posted event per earthquake
  (a summary tap would count as 1 open of 5 posts); posting time in the deep link URI (adds a route argument that the
  screen does not need); Android's own group summary with children (more notifications than SPEC allows).
- **Consequences:** Policy and analytics are unit tested; the Android part is covered by instrumented tests. Android
  still bundles 4+ notifications from successive checks into a system group, which opens the app normally.

## ADR-031 — Deep links from new intents go through a pending state; app opens are tracked per process
- **Context:** ADR-025 handled warm deep links with an `OnNewIntentListener` registered from Compose. After the
  process is killed, a notification tap recreates the activity from saved state and delivers the tap through
  `onNewIntent` before Compose has registered the listener: the tap was lost and the previous screen came back. The
  same restore skipped `app_opened` (saved state looked like a rotation).
- **Decision:** `MainActivity.onNewIntent` stores app deep links in a `pendingDeepLink` state; `QuakeAlertApp` hands it
  to `navController.handleDeepLink` once the NavHost exists and clears it. `AppOpenTracker` is a singleton: a fresh
  activity is an open; a restore in the same process is not; a restore after process death becomes an open on the
  first `onNewIntent` (source notification when it carries a tap) or `onResume` (source launcher).
- **Alternatives:** Registering the listener in `onCreate` (NavController does not exist yet); `setIntent` + reading it
  in Compose (the NavController would not know it has not handled it); logging opens only when there is no saved
  state (misses process death, the common case for notification taps).
- **Consequences:** Cold, warm and restored taps all open the tapped event, verified on API 31 and 34. The restored
  screen can show for a moment before the deep link replaces it, and logs one `earthquake_list_viewed`. Supersedes the
  warm-start part of ADR-025.

## ADR-032 — App bundles keep all languages in the base module
- **Context:** The app switches languages at runtime (AppCompat per-app locales, ADR-012) and builds notification text
  with a context in that language. With the default bundle language splits, Play installs only the device's
  languages, so an in-app switch could miss resources (lint `AppBundleLocaleChanges`).
- **Decision:** `bundle { language { enableSplit = false } }`.
- **Alternatives:** Play Core on-demand language downloads (extra dependency and a download step for a few strings).
- **Consequences:** Every install carries all `values-*` strings (a few KB per language). Adding a language is still
  one `strings.xml`.

## ADR-033 — Background check: skip until set up, remember only shown alerts, retry only transient errors
- **Context:** SPEC §5.2 defines the periodic check. Some details were open: what to do before the user saved any
  alert settings, whether matches that could not be shown (permission missing) count as notified, which errors
  should be retried, and where the schedule is kept in sync with the settings.
- **Decision:** `CheckForNewAlertsUseCase` skips (no network call) while alerts are off or no baseline exists. Only ids
  of posted notifications are stored; suppressed matches stay eligible, so allowing notifications within 6 h still
  delivers them (each check logs one suppressed event meanwhile). `lastCheckedAt` is the check's start time, so events
  updated during the check are caught by the next overlap. Network and server errors return `retry` (WorkManager
  backoff), parsing and unknown errors `failure` (no endless retries). `SyncAlertScheduleUseCase` is the one place
  that schedules or cancels: on app start and after every saved settings change.
- **Alternatives:** Mark suppressed matches as notified (the user never learns about them after allowing
  notifications); schedule from the Alerts screen (onboarding and future entry points would repeat it); retry every
  error (a broken response would retry forever).
- **Consequences:** No checks run before onboarding saves the settings (2.7). Nothing alerts twice; a late permission
  grant still delivers recent alerts. Scheduling rules are unit tested without WorkManager.

## ADR-034 — The selected app language is also stored by the app for background processes below API 33
- **Context:** Below API 33, AppCompat restores the per-app language only when an activity is created. The alert
  worker usually runs in a process without an activity, so notifications came out in the system language
  (verified on API 31: "7 new M4.5+ earthquakes" with the app set to Turkish).
- **Decision:** `AppCompatLanguageManager.setSelectedLanguage` also writes the tag to SharedPreferences
  (`app_language`); `getSelectedLanguage` falls back to it below API 33 when AppCompat has no locales.
  `LocalizedContextProvider` builds its context from `getSelectedLanguage()`. On API 33+ the platform keeps the
  per-app locale in every process, so the stored copy is not read there.
- **Alternatives:** Reading AppCompat's internal storage file (restricted API, format may change); creating an
  AppCompat delegate in `Application` (not supported); DataStore (asynchronous; the notifier needs the value
  synchronously).
- **Consequences:** Notifications and the channel name follow the in-app language on all API levels (verified: "7 yeni
  M4,5+ deprem" in a worker-only process on API 31). The language picker (3.1) must go through `AppLanguageManager`,
  which it does by design.

## ADR-035 — Onboarding is one route with pages; finishing always saves the settings
- **Context:** SPEC §4.1 has three steps (welcome, alert setup, notification permission). Alert checks only run once
  settings were saved with a baseline (ADR-033), and saving through `UpdateAlertSettingsUseCase` skips unchanged
  settings — a user who keeps the defaults would never get a check.
- **Decision:** A single `OnboardingRoute` whose `OnboardingViewModel` holds the current page (and threshold) in
  `SavedStateHandle`; Back goes to the previous page. The setup page reuses the Alerts components. The notifications
  page asks for `POST_NOTIFICATIONS` on API 33+ with "Not now" as an equal way out; a denial is accepted with a short
  explanation and an "Open settings" link; below API 33 it only shows the status. Finish calls
  `CompleteOnboardingUseCase`, which always saves the chosen settings with `baselineAt = now`, marks onboarding
  completed and syncs the schedule. Analytics of the choice reuse `toAnalyticsEvents(ONBOARDING)`.
- **Alternatives:** One navigation destination per step (three routes, arguments to carry the draft, more back-stack
  handling for the same result); asking for the permission on first launch before explaining it (lower grant rate);
  saving only when the choice differs from the defaults (no baseline, no schedule).
- **Consequences:** Rotation and back keep the flow. Every finished onboarding starts background checks right away.
- **Update (process death):** The area choice (mode, radius and every city field) is also kept in `SavedStateHandle`
  as plain values (`AreaSelectionSavedState`), written on first start and on every change. After process death the
  page, threshold and area come back as they were. If the notifications page is restored without an area that can be
  saved (missing or unreadable values, e.g. state written by an older version), the flow returns to the setup page,
  so Finish never saves an area the user did not see; a chosen city never silently turns into the whole world.

## ADR-036 — Developer tools reuse the real delivery path and live behind a debug-only slot
- **Context:** Alerts must be demonstrable on demand (a real matching earthquake may take hours), including the tap
  into the detail and the "never twice" rule, and a check must be triggerable without waiting 15 minutes.
- **Decision:** A "Developer tools" card on the Settings tab, passed by `QuakeAlertNavHost` as a slot only when
  `BuildConfig.DEBUG`. Simulate alert builds an earthquake that matches the saved settings, stores it in the cache so
  the detail can open it, and delivers it through `DeliverAlertsUseCase` — the step the background check uses too.
  "Simulate the same alert again" re-delivers the newest simulated event found in the cache, so it still works after
  a notification tap has rebuilt the back stack. Run check now enqueues a unique, non-expedited one-time request of
  the same worker.
- **Alternatives:** A fake id without caching (the notification would open "not found"); keeping the last simulated
  event in the ViewModel (lost after the deep link); expedited work (needs `getForegroundInfo` and a notification in
  the worker below API 31); a separate debug source set (more wiring for a small screen).
- **Consequences:** The simulated event appears in the list until the next refresh replaces the cache. Release builds
  never show the card; the use cases stay in the main source set but are unreachable there.

## ADR-037 — Simulated alerts take their values and can be scheduled
- **Context:** Extends ADR-036. The alert rules should be demonstrable in both directions (a matching earthquake
  notifies, a non-matching one does not) and with the app closed, a few minutes ahead.
- **Decision:** The developer card takes a magnitude, a distance from the selected city (ignored for the whole world)
  and a delay. "Simulate now" delivers at once; "Schedule" enqueues a one-time `SimulatedAlertWorker` with the values
  as input data and the delay as initial delay. The worker builds the event when it runs (its time is the delivery
  time, so baseline and max-age rules hold) and uses the same delivery step as real checks. The outcome of every
  simulation is logged as `developer_simulated_alert` with `outcome` and `scheduled`.
- **Alternatives:** Exact alarms (an extra permission for a debug tool); building the event at scheduling time (its time
  would be in the past or future relative to the check); free latitude/longitude input (harder to type, distance from
  the city is what the rule checks).
- **Consequences:** Delivery follows WorkManager timing (seen 0–1 min late on API 31 and 34). Force-stopping the app
  cancels a scheduled simulation.

## ADR-038 — Settings reads core directly; shared pieces move to core
- **Context:** The Settings tab shows the language picker, the notification permission status, the about section
  and the developer tools slot. The permission row already existed privately in `alerts`, and the browser intent
  helper in `earthquakes`; `settings` must not depend on other features.
- **Decision:** `settings` has only a presentation layer. `SettingsViewModel` reads `AppLanguageManager` and
  `NotificationPermissionChecker` (both `core` interfaces; renamed `NotificationAccessChecker` in ADR-047) and logs `language_changed` only when the pick differs from
  the current language. Language and permission are re-read on resume. The permission row became
  `core/ui/component/NotificationPermissionStatus` (used by Alerts and Settings) and the outgoing intents
  (`browserIntent`, `notificationSettingsIntent`, `tryStartActivity`) moved to `core/navigation/ExternalIntents`.
  "Open settings" opens the app's system notification page in both places; the in-app permission dialog stays in
  onboarding. The USGS link and version (`BuildConfig`) live in the about section; the URL is `SettingsConfig`.
- **Alternatives:** Use cases wrapping each `core` call (no logic to hold); `settings` importing the `alerts` row (a
  second cross-feature dependency); requesting the permission dialog again from Settings (Android stops showing it
  after two denials, so the system page is the path that always works).
- **Consequences:** `notification_permission_requested` is only sent from onboarding (`context=onboarding`). Adding
  a setting that needs logic (e.g. a stored preference) is the point to add a `domain` layer to `settings`.

## ADR-039 — Developer tools get their own screen; the event log reads the analytics table
- **Context:** The debug-only developer card (simulation fields, schedule, repeat, run check) made the Settings tab
  long, and the event log (SPEC §4.6) needed an entry point too.
- **Decision:** Settings shows one "Developer tools" entry below About (only when `BuildConfig.DEBUG`). It opens
  `DeveloperToolsRoute`, a pushed screen without the bottom bar: an "Event log" entry and the alert testing card
  (the `alertTools` slot filled by `navigation`, so `settings` still does not depend on `alerts`). Both developer
  routes are registered only in debug builds. The event log is its own feature (`eventlog`) over the existing
  `analytics_events` table: newest first, live updates, filter by name ("contains", case-insensitive), share the
  visible events as plain text lines, clear after a confirmation (`AnalyticsEventDao.deleteAll()`).
- **Alternatives:** Keep the card inline and collapse it (still one long screen, more state); a separate debug source
  set (more build wiring); filter by exact name from a dropdown (typing a part such as "alert" covers groups of events
  at once); share as JSON/CSV (plain lines are readable in any chat or issue).
- **Consequences:** One more tap to reach the developer tools. Clearing the log only affects the local table; Logcat
  keeps its copy. Release builds have neither route nor entry.

## ADR-040 — Landscape and large text: scrolling list controls and a navigation rail
- **Context:** In landscape the list's fixed header (filters, count, sort) plus the top and bottom bars left room for
  about one card; large font scales had the same effect in portrait and broke the "Earthquakes" tab label mid-word.
- **Decision:** The list controls are the first item of the list and scroll away with it. From a 600 dp window width
  (Material's medium width: landscape phones, tablets) the three tabs move from the bottom bar to a navigation rail on
  the start side; the width comes from `LocalWindowInfo.containerSize`. Tab and segmented button labels stay on one
  line with an ellipsis.
- **Alternatives:** Collapsing top app bar tied to scroll (more nested-scroll wiring, the title still takes space when
  expanded); `NavigationSuiteScaffold` (an extra adaptive dependency for one switch); `Configuration.screenWidthDp`
  (lint `ConfigurationScreenWidthHeight`: rounded and inset-dependent).
- **Consequences:** Landscape shows two to three cards instead of one. After changing a filter the list starts at the
  top again (the list state is keyed on the options), so the controls are visible right after a change.
- **Update (3.5):** the list header no longer uses the fixed-height `TopAppBar`: at font scale 1.5 on API 31 its
  title and two-line subtitle overflowed upwards into the status bar. A row with a 64 dp minimum height keeps the
  normal look and grows with the text.

## ADR-041 — Instrumented tests run on Hilt with isolated storage and a fake USGS
- **Context:** The onboarding happy path and the worker need the real object graph (ViewModels, use cases, DataStore,
  Room, WorkManager) to be meaningful, but must not depend on the network or on state left by earlier runs.
- **Decision:** A custom `HiltTestRunner` starts `HiltTestApplication`. `@TestInstallIn` modules replace, for every
  Hilt test, the database (in-memory), the DataStore (new file per test component) and the USGS API (`FakeUsgsApi`,
  injectable to set responses or failures). Per test class, `@UninstallModules` + `@BindValue` swap the notifier and
  the notification permission check. Tests initialise a test WorkManager with the injected `HiltWorkerFactory`. The
  end-to-end test launches `MainActivity` with `ActivityScenario` after this setup, so preferences are prepared first.
- **Alternatives:** A mock web server (more setup, tests the HTTP layer already covered by unit tests); per-test
  `@UninstallModules` for storage too (repeated in every class); constructing ViewModels by hand in UI tests (skips
  Hilt wiring and navigation, which is what the end-to-end test is for).
- **Consequences:** Existing plain Compose and DAO tests are unaffected. `QuakeAlertApplication.onCreate` (channel,
  schedule sync) does not run in instrumented tests; the flows under test do not depend on it.

## ADR-042 — Screens aligned with the UI design: severity palette, day groups, event categories
- **Context:** A side-by-side review against the UI design showed drift: the "light" and "moderate" badges were two
  similar browns, list badges had no severity word, the list had no day sections, the filter chips had no icons, the
  area mode used plain segmented buttons, the welcome page had a single icon and the event log was monochrome.
- **Decision:**
  - Severity badges: minor teal, light amber (dark text), moderate burnt orange `#BF5700` (white text), strong red,
    major purple (instead of a second dark red, so M6 and M7+ never look alike). The same moderate colour is used in
    both themes because white text on orange was preferred; `#BF5700` is the brightest orange that keeps 4.5:1 with
    white. The compact list badge shows the severity word too (`Mod` / `Orta` as the short form of moderate).
  - List: grouped into sticky day sections ("Today", "Yesterday", then a localized weekday + date) with a count, but
    only for `Newest first`; for `Biggest` / `Nearest` sections would break the chosen order, so the list stays flat.
    Days are the device's local days. Grouping is a pure function over the already sorted list
    (`groupByDay`), done in the UI because "today" moves with the clock, not with the data.
  - Filter chips and radius chips share one colour set (`quakeAlertFilterChipColors`): filled primary when selected,
    no outline; every list chip has a fixed icon (globe, pin, waves, bell).
  - Area mode: a pill-shaped toggle (selected option raised, icon + label) with radio-button semantics
    (`ChoiceToggle` in the alerts presentation layer).
  - Welcome page: the app icon itself (its adaptive background + foreground layers, masked to a circle like the
    launcher shows it); a first drawn `Canvas` version was replaced at the user's request, so the welcome page and
    the launcher show the same mark.
  - Event log: each event gets a category from its name (`EventCategory`: alert, background, settings, usage) that
    picks the icon and accent colour; parameter values use the accent colour; the time sits in a pill under the name
    so long snake_case names are not broken mid-word.
  - Icons come only from `material-icons-core` and the drawables already in the project; the one addition is
    `ic_schedule` (clock), which the core set lacks.
- **Alternatives:** Keep the palette of ADR-018 (two neighbouring bands hard to tell apart); orange with dark text
  (passes contrast more easily, but white text was preferred); day sections for every sort order (splits
  "Biggest first" into days, so the biggest earthquake would not be on top); adding a Material Symbols drawable for
  every icon in the design (more resource files for small visual gains).
- **Consequences:** Badges are distinguishable at a glance in both themes, all pairs meet WCAG AA (≥ 4.5:1). The
  event category mapping must be extended when a new event family is added; unknown names fall back to "usage".

## ADR-043 — Theme choice (system / light / dark) through AppCompat night mode
- **Context:** Users asked to pick the theme in the app, applied at once and kept after a restart.
- **Decision:** `ThemeModeManager` (`core/appearance`) with `AppCompatThemeModeManager`: the choice is stored in
  SharedPreferences (`app_theme`) and applied with `AppCompatDelegate.setDefaultNightMode`, in
  `QuakeAlertApplication.onCreate` and on every change. AppCompat recreates the activity, so Compose
  (`isSystemInDarkTheme`), the window background and the system bar icons all follow; the Settings tab stays selected.
  Settings shows it exactly like the language: the current value in a row that opens a radio dialog
  (`SelectedValueRow` + `SingleChoiceDialog`, shared by both sections); a change logs `theme_changed`.
- **Alternatives:** Keep the mode in DataStore and pass it to `QuakeAlertTheme` (no recreation, but an asynchronous
  first read shows the wrong theme for a frame, and the window background and system bars would need separate
  handling); `UiModeManager.setApplicationNightMode` (API 31+ only, minSdk is 26).
- **Consequences:** Same pattern as the language (ADR-034): a small synchronous read at start-up, identical on every
  API level (verified on API 31 and 34, including after the app is killed). Default is "System default", labelled
  like the language's default.

## ADR-044 — USGS place text is localized on display
- **Context:** USGS returns `place` as English text with no language option. Over one year of events (all
  magnitudes, 138,695 events) 97.5% had the form "`<distance>` km `<direction>` of `<place>`"; the other 2.5% (3,434
  events, 148 distinct texts) are region names from a fixed list ("south of the Fiji Islands", "Iceland region",
  "Kermadec Islands, New Zealand", "northern Mid-Atlantic Ridge").
- **Decision:** The stored text stays unchanged; it is localized only when shown (list, detail, share and map label,
  notifications), and not at all when the app language is English (the source text is already English).
  - `PlaceDescription.parse` (`core/model`, pure) splits the common form into distance, one of 16 compass directions
    and the place name; `place_near_format` rebuilds it per language (Turkish "Preston, Nevada · 28 km
    Kuzey-Kuzeybatı", capitalized directions, non-breaking spaces so distance and direction stay on one line).
  - Each comma-separated part of a place or region name is translated in this order: a country name through the
    platform country list (`CountryNames`, `Locale.getISOCountries` plus a small alias table for USGS spellings);
    a well-known name from a dictionary of 44 oceans, seas, ridges and island groups (`PlaceNameDictionary`, chosen
    from the one-year data); a region phrase (`RegionPhrase`, `core/model`, pure): "`<side>` of X", "northern X",
    "X region", "off the (east) coast of X", "X Islands", "X Island", whose inner name goes through the same steps.
    Anything else (towns, states, one-off event names) is kept.
- **Alternatives:** Reverse-geocoding every earthquake with the platform `Geocoder` (hundreds of lookups per refresh,
  needs the network, results vary by device); an on-device translation model (tens of MB for mostly proper names);
  translating the full USGS region list of about 750 names (complete, but a large per-language translation effort);
  keeping the English text (the list mixed two languages).
- **Consequences:** In Turkish, 3,418 of the 3,434 region-style events of the year read differently (checked on a
  device against all 148 texts); the 16 unchanged ones are US state names and named events. A new language needs
  `place_near_format`, 16 direction strings, the region phrase templates, 9 side words and the 44 dictionary names;
  missing ones fall back to English and lint reports them.

## ADR-045 — "Use my location" with approximate location and the platform location API
- **Context:** Typing a city is slow for users who simply want alerts around where they are. The area model is a
  circle of 50–1000 km (ADR-007), so the center does not need to be precise.
- **Decision:** A "Use my location" button in the area selector (onboarding and Alerts tab share it) sets the city in
  one tap.
  - Permission: `ACCESS_COARSE_LOCATION` only, requested at the moment of use (never at start-up). Approximate
    location (~2 km) is far below the smallest radius, and it is the least intrusive request.
  - Location: platform `LocationManager` through `LocationManagerCompat.getCurrentLocation` (androidx.core, already a
    dependency). All enabled providers (fused on API 31+, network, gps) are asked at the same time and the first fix
    wins: with approximate permission the fused provider runs in low-power mode (Wi-Fi / cell only), which never
    answers on an emulator and can be slow indoors, while gps still answers (coarsened by the system). 15 s limit
    (`AlertConfig.CURRENT_LOCATION_TIMEOUT`), then the newest last known location.
  - Name: reverse geocoding with the same `Geocoder` wrapper as the search (API 33 split, same error mapping). The
    circle center is the device point; the geocoder only names it (city, admin area, country in the app language).
  - The located city is applied directly (no second pick) and saved like a searched city; nothing else changes
    (alerts, list filter "Near <city>", distances all read the same `City`).
  - Failures keep the search usable: permission denied → app settings, location off → location settings, no fix or
    no place name → retry, offline → network message.
  - The located city is UI state (`LocationLookup.Found`) that the area selector picks like a tapped search result,
    so one code path sets the city and a result that arrives during a rotation is not lost.
- **Alternatives:** Fused Location Provider from Google Play services (one more dependency and a Play services
  requirement for a single lookup); `ACCESS_FINE_LOCATION` (a stronger permission prompt for no benefit at this
  radius); using the geocoded town's coordinates as the center (the circle would move away from the user for no
  reason); showing the located city as a search result to confirm (an extra tap for the common case).
- **Consequences:** The location is read once per tap and never in the background; coordinates are only stored as
  the alert area (as with a searched city) and never logged (`current_location_used` has only a `result`). Devices
  without a geocoder do not see the button (the whole "Near a city" mode is hidden there, ADR-008).

## ADR-046 — Alerts tab: the alert switch and the notification permission are shown as one state
- **Context:** With the notification permission off, the Alerts tab showed "Earthquake alerts" switched on with
  "You'll be notified about…" at the top, and "Notification permission is off" in the status card at the bottom. In
  Turkish both used the same word ("Deprem bildirimleri" / "Bildirim izni kapalı"), so the screen seemed to say
  "on" and "off" about the same thing.
- **Decision:** The switch is the user's choice, the permission is the system's; the screen shows their combined
  effect in one place. Alerts on + permission off → the summary card replaces the (untrue) summary sentence with
  "Alerts can't reach you" and an "Allow notifications" button that opens the app's notification settings (same
  target as before, ADR-038). Alerts off → the "alerts are off" text only. The status card at the bottom of the tab
  (permission, last background check, check interval) is removed: the permission now lives in the summary card, and
  the check time and interval are technical details (the delay is explained in onboarding and the README). Turkish uses "alarm" for the app's alerts and "bildirim" only for the system
  notifications ("Deprem alarmları", "Alarmlar kapalı").
- **Alternatives:** Keep both rows and reword them (still two places to read); turn the switch off while the
  permission is off (it would overwrite the user's choice and the saved baseline).
- **Consequences:** One answer to "will I get alerts?" at the top of the tab. The Settings tab still shows the full
  permission row, since it is about the device, not the alert choice.

## ADR-047 — Notification access is the app permission and the alert channel together
- **Context:** Only `areNotificationsEnabled()` was checked. On Android 8+ the user can turn off the "Earthquake
  alerts" category (channel) while notifications stay allowed for the app. Posting then succeeds without error but
  nothing is shown, so the delivery counted as `POSTED`: the event went into the notified table (never retried),
  `alert_notification_posted` was logged and every screen said "Notifications allowed".
- **Decision:** `NotificationAccessChecker` (was `NotificationPermissionChecker`) returns `NotificationAccess`:
  `APP_BLOCKED` (app notifications off), `ALERT_CHANNEL_BLOCKED` (app allowed, `earthquake_alerts` importance
  `NONE`), or `ALLOWED`. Only `ALLOWED` lets `NotifyAlertsUseCase` post; anything else is `SUPPRESSED`, logged as
  `alert_notification_suppressed` with `reason` = `permission_denied` or `alert_channel_blocked`, and not remembered
  (ADR-033), so the next check after the category is turned on delivers it. The Alerts summary card, the Settings row
  and the onboarding notifications page name the blocked category, and their button opens the channel's own settings
  page (`ACTION_CHANNEL_NOTIFICATION_SETTINGS`) instead of the app page; the permission dialog is offered only for
  `APP_BLOCKED`. `POSTED` / `alert_notification_posted` mean "handed to Android while app and channel were allowed",
  not "seen": Do Not Disturb, a dismissed or bundled notification are not visible to the app; seeing is measured only
  by `alert_notification_opened`.
- **Alternatives:** Let the notifier report whether Android showed the notification (Android gives no such answer
  for a blocked channel); check only the channel (misses the app switch); open the app page for both cases (the
  category switch is one level deeper and easy to miss).
- **Consequences:** UI, delivery result, notified table and analytics agree on every access state. A blocked channel
  group or a race between the check and the post are not covered (the app uses no groups; the window is a few ms).

## ADR-048 — A notification opens the latest USGS version; fetches refresh cached copies
- **Context:** The background check fetched the revised event (e.g. 4.7 → 5.0) and notified, but the list cache kept
  the old copy, and the detail read the cache first. Tapping the notification showed 4.7.
- **Decision:** Two changes. (1) `EarthquakeRepositoryImpl.fetchEarthquakes` and `fetchEarthquake(id)` replace the
  cached copy of every event they return (`EarthquakeDao.updateExisting`, a Room `@Update`: rows that are not cached
  are ignored), so the cache keeps the list's scope but never an older version than the app has seen. (2)
  `GetEarthquakeUseCase(id, shouldRevalidate)`: from the list it stays cache first (the detail matches the row the
  user tapped, works offline); from a notification it asks USGS first. If that fails with a network or server error
  and a cached copy exists, the copy is shown with a notice ("Couldn't get the latest version…", Retry); if USGS does
  not know the id (404, e.g. a simulated alert) the cached copy is shown without a notice, as before; with no cached
  copy the error / not-found states stay.
- **Alternatives:** Insert every fetched alert into the cache (the list would show single events outside its query
  and gaps between them); always ask USGS first (the list-to-detail path would lose offline use and show values that
  differ from the row just tapped); a per-row "last updated" time (a schema migration for one notice).
- **Consequences:** Notification, list and detail show the same version as soon as the check has run; an offline
  tap still shows the event and says it may be out of date. The repository contract changed from `getEarthquake` to
  `getCachedEarthquake` + `fetchEarthquake`; the cache-or-network rule now lives in the use case.

## ADR-049 — Clearing a saved city does not need the geocoder
- **Context:** Without a geocoder (`Geocoder.isPresent() == false`) the whole area choice was hidden. A user with a
  saved city (restored backup, geocoding removed later) saw the city but could not switch to the whole world.
- **Decision:** Only searching needs the geocoder. The "Whole world / Near a city" choice is shown when city search
  is available or a city is part of the selection; without a geocoder the selected city card has no "Change" button
  and a short note says the city cannot be changed here but the whole world can still be chosen. Switching back to
  "Near a city" restores the kept city (the selection keeps it, ADR-028).
- **Alternatives:** Keep the choice hidden and reset the area to the whole world when no geocoder exists (silently
  changes what the user saved); show the choice always (a "Near a city" that can never be completed).
- **Consequences:** A saved area can always be removed; new cities still need a geocoder.

## ADR-050 — Onboarding steps fit on one screen; city search moves into a dialog
- **Context:** The welcome step (three feature cards + disclaimer) and the setup step (threshold, area choice, "Use my
  location", country, city field, results, radius) needed scrolling on a 6.1–6.4" phone, so the "Next" decision
  sat below content the user had not seen.
- **Decision:** Every onboarding step fits without scrolling at the default font size. The welcome step keeps the
  icon, the one-sentence summary and the early-warning disclaimer; the feature cards are removed (the next two steps
  show the same things). The setup step drops its subtitle and the slider's "You'll get alerts for M4.5…" sentence
  (the badge shows the value). City search moves into a full-screen
  `CitySearchDialog`, opened by choosing "Near a city" without a city, the "Choose a city" button or "Change"; the
  area card only shows the selected city and the radius. The Alerts tab uses the same component, so it gets the same
  dialog. `verticalScroll` stays as a fallback for large text, landscape and very small screens.
  `OnboardingLayoutTest` checks on the emulator that no step has anything to scroll.
- **Alternatives:** Shrink fonts and spacing (breaks the design scale, still overflows with results); a separate
  onboarding page for the city (a fourth step for the optional choice); a dialog only in onboarding (two behaviours
  of one component).
- **Consequences:** One extra tap to reach the search field from the Alerts tab; search results get the whole
  screen. The Turkish setup step with a long region name is the tightest case (checked on API 34).

## ADR-051 — Alert preview counts the cached list with the alert rule itself
- **Context:** Choosing a threshold and radius is abstract; users cannot tell whether "M3.0 within 1000 km" means a
  few alerts a month or several a day.
- **Decision:** The Alerts tab shows "With these settings, N earthquakes in the last 3 days would have matched your
  alerts." (It was first also on the onboarding setup step; removed there on request on 2026-09-29, so the first
  choice stays short.) `PreviewRecentAlertMatchesUseCase` counts the cached list (the 7-day M2.5+ worldwide
  query, so any threshold ≥ 2.5 and any area is covered) with `AlertMatcher.matchesThresholdAndArea`, the same
  function the background check uses; it was split out of `matches` so both cannot drift apart. The delivery-only
  rules (baseline, already notified, 6 h freshness) are left out because they are about new events. The preview
  follows the slider while it is dragged and every area/radius change, without saving. A missing cache is loaded, a
  stale one is shown and refreshed; this refresh is not
  logged as `earthquake_list_refreshed` (it is not a list refresh). Simulated test earthquakes are not counted.
  Checked against the USGS count API (whole world M4.5+: 56 = 56; Tokyo 250 km M4.5+: 1 = 1; 1000 km M3.0+: 4 = 4).
- **Alternatives:** A USGS `count` request per change (network on every slider step, offline gives nothing, a
  second rule implementation on the server side); counting 7 days (older than the "recent" feeling, and the list
  already shows 7 days); no preview, only the whole-world warning (says nothing about city settings).
- **Consequences:** The number is exact for the cached data and costs no network while choosing. `PREVIEW_PERIOD`
  is one constant; `AlertConfigTest` fails if it outgrows the cached period or the threshold range drops below the
  cached minimum magnitude. Events revised after the last refresh show their cached values (ADR-048).

