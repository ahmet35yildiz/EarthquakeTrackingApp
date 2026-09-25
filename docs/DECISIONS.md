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
  - Self-rescheduling OneTimeWork chain (~5 min while the device is active) — kept as a stretch item on top of the
    periodic work.
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
  is added as a Material Symbols vector drawable in `res/drawable`.
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
  (≥ 5.7:1). Changing the brand colour means regenerating `Color.kt` from the same palettes.
