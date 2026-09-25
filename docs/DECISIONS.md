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
