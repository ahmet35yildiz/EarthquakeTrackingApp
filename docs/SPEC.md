# QuakeAlert — Product Specification

Status: agreed, 2026-09-25. Changes to this document must be recorded in `docs/DECISIONS.md`.

## 1. Problem and target user

**Target user:** a person anywhere in the world who wants to know quickly when a *meaningful* earthquake happens near
the place they care about (their city, or their family's city) — without being disturbed by the many small
earthquakes that happen every day.

**Core need:** "Tell me when an earthquake I would care about happens, and let me quickly check what happened
(where, how strong, how far from my city, how deep)."

**Solution approach:** a calm, low-noise alerting app.
- The user chooses *what matters* once: a minimum magnitude and optionally an area (city + radius).
- The app checks USGS in the background and notifies only for new events that match.
- The app also offers a clear list of recent earthquakes and a detail view.

**What this app is NOT:** an earthquake early-warning system. Notifications arrive minutes after an event (USGS
publication delay + background check interval). The app says this explicitly in onboarding and in the README.

## 2. Scope

### In scope (MVP)
1. First-run onboarding: value proposition + disclaimer → alert setup (threshold, area) → notification permission.
2. Earthquake list: recent events, filters, pull-to-refresh, offline cache, loading/empty/error states.
3. Earthquake detail: all key facts, distance to the user's city, open in maps, open on USGS, share.
4. Alert settings: enable/disable, magnitude threshold, area (country → city search → radius, or "Use my
   location") or whole world.
5. Background check with WorkManager (every 15 min) + local notifications for new matching earthquakes.
6. Local analytics event log + developer screen to inspect it.
7. Developer tools: "Simulate alert" and "Run check now" to demonstrate the notification flow on demand.
8. Multilingual UI: English (default) + Turkish, in-app language switch.
9. Light and dark theme, consistent custom Material 3 design.

### Out of scope (documented in README with reasons)
| Item | Reason |
|---|---|
| Push notifications (FCM) + backend | Needs a server that polls USGS and fans out pushes; only way to get instant alerts in Doze. Listed as the main next step. |
| Map SDK | Needs API keys and setup; "Open in maps" intent covers the need. |
| Accounts / sync | No multi-device need for the core use case. |
| Multiple areas | Adds settings complexity; one area covers the main persona. |
| Home-screen widget | Nice-to-have, not part of the core loop. |
| Regional data sources (AFAD, EMSC, Kandilli) | App is global; USGS is keyless and global. Regional completeness is a known limitation. |
| Imperial units | Kilometres only for MVP. |

## 3. Data source: USGS
- Endpoint: `https://earthquake.usgs.gov/fdsnws/event/1/query` (FDSN event service, GeoJSON, no API key).
- Always send `format=geojson&eventtype=earthquake` (excludes quarry blasts / explosions).
- Area filter uses the native circle parameters: `latitude`, `longitude`, `maxradiuskm`.
- Background checks use `updatedafter` so events that are *published late* (USGS often publishes minutes after
  origin time) are not missed.
- Detail fallback: `eventid=<id>` (returns HTTP 404 when unknown → "not found" state).
- Field notes: `mag` and `place` can be `null`; depth is `geometry.coordinates[2]` (km); `time`/`updated` are epoch
  ms; `place` is always English (e.g. "15 km SSW of Hilvan, Turkey"); magnitudes can be revised after publication.
- Measured on 2026-09-25: last 7 days, M2.5+, worldwide = 372 events, ~265 KB GeoJSON.
- Known limitation: USGS completeness varies by region (e.g. for Türkiye in one week: USGS 2 events vs. AFAD 713).
  Stated in README as a limitation + next step (regional sources).

## 4. Screens and behaviour

### 4.1 Onboarding (first run only; completion stored)
1. **Welcome:** app icon, what the app does in one sentence, disclaimer "not an early-warning system; alerts can be
   delayed by several minutes".
2. **Alert setup:** magnitude threshold slider + area selection (same components as Alert settings). If the user
   keeps "Whole world", a warning explains that alerts will come for earthquakes anywhere and can be frequent at low
   thresholds.
3. **Notifications:** explains why the permission is needed → requests `POST_NOTIFICATIONS` on API 33+. Below 33 the
   step only confirms (no runtime permission). Denial is allowed; the app still works and shows a banner later.
4. Finish → main screen, periodic work scheduled if alerts are enabled.
5. Every step fits on a phone screen without scrolling at the default font size (scrolling remains only as a fallback
   for large text, landscape and very small screens). City search therefore opens in its own full-screen dialog.

### 4.2 Earthquake list (tab "Earthquakes", start destination after onboarding)
- Data: last **7 days**, **M2.5+**, worldwide, fetched once per refresh and cached in Room. All filtering is local.
- Filters (chips): Region `World | Near <city>` (second chip only when an area is set) and Magnitude
  `All (2.5+) | ≥ my threshold`.
- Item: magnitude badge (colour and word by severity), place (shown in the app language: distance, direction,
  country, region phrases and well-known oceans / ridges / island groups are translated, town names are kept —
  ADR-044), relative time ("12 min ago"), depth, distance to the
  user's city when an area is set.
- With `Newest first` the list is split into day sections (Today, Yesterday, then the date) with a count per day;
  other sort orders show one flat list.
- Sort order (menu next to the count, always visible): `Newest first` (default) | `Biggest first` (unknown
  magnitudes last) | `Nearest first` (only when an area is set). Ties are broken by time, newest first. Changing the
  sort or a filter scrolls back to the top; rotation keeps the position. `LazyColumn` with stable keys (event id).
- The whole cached period is one list (no paging); see ADR-023.
- Refresh: on first open and when the cache is older than 5 min; pull-to-refresh anytime.
- States: loading (first load), content, empty (with explanation for active filters), error with retry, offline
  banner "Showing data from <time>" when refresh fails but a cache exists.

### 4.3 Earthquake detail
- Magnitude + magnitude type, place, local time + UTC, relative time, depth, coordinates, distance to the user's city,
  review status (automatic/reviewed), tsunami flag, felt reports count (if any).
- Actions: Open in maps (`geo:` intent), View on USGS (browser), Share (plain text summary).
- Loads from cache; if missing (e.g. opened from an old notification) fetches by `eventid`; 404 → not-found state.
- Reached from the list or from a notification tap (deep link).

### 4.4 Alert settings (tab "Alerts")
- Alerts on/off switch (on: schedules periodic work; off: cancels it).
- Magnitude threshold: slider **2.5 – 8.0**, step **0.5**, default **4.5**.
- Area: `Whole world` (default, with warning) or `City + radius`.
  - Country picker: all ISO countries from `Locale.getISOCountries()`, names localized to the app language, sorted
    with a locale-aware `Collator`, local search box. Default: device locale country.
  - City search (full-screen "Choose a city" dialog, opened by picking "Near a city" without a city, the "Choose a
    city" button or "Change"): text field + "Search" action (no live suggestions) → Android `Geocoder` → results
    filtered to the selected country → user picks one and the dialog closes. Loading/empty/error states. Hidden if `Geocoder.isPresent()` is false; a city that is
    already saved is still shown and can be switched to `Whole world` (it just cannot be changed).
  - "Use my location" (above the country picker, same place as the search): asks for approximate location
    (`ACCESS_COARSE_LOCATION` only) on first use → the device location becomes the circle center and reverse
    geocoding gives the name (city + admin area + country, in the app language); the city is set directly, no
    extra pick. Errors: permission denied → "Open settings" (app details); location off → "Open settings"
    (location settings); no location within 15 s and no recent one, or no place name → retry; offline → network
    message + retry. Searching by name stays available in every case.
  - Radius options: **50 / 100 / 250 / 500 / 1000 km**, default **250 km**.
- Preview (Alerts tab, below the area; not in onboarding): "With these settings, N earthquakes in the last 3
  days would have matched your alerts." Counted on the cached list (7 days, M2.5+) with the same threshold and area
  rule as the alert check; updates while the slider is dragged and on every area/radius change. Hidden while "Near a
  city" has no city; "Checking recent earthquakes…" while a missing cache loads; a short note if it cannot load.
  Simulated test earthquakes are not counted.
- Notification permission: while alerts are on and the permission is off, the summary card shows "Alerts can't
  reach you" with "Allow notifications" (opens the app's notification settings) instead of the summary sentence;
  nothing is flagged while alerts are off. The Settings tab keeps the full permission status row.
- Every saved change (switch, threshold, area) is stored immediately, without a confirmation message, and resets the
  alert baseline (see 5.1) so older events never trigger notifications.

### 4.5 Settings (tab "Settings")
- Language: `System default` + every language the app ships (generated automatically), applied instantly.
- Theme: `System default` | `Light` | `Dark`, picked like the language (row + dialog), applied instantly and kept
  after a restart (ADR-043).
- About: data source attribution (USGS), "not an early-warning system" note, app version.
- Developer tools entry below About (debug builds only) → a separate Developer tools screen with Event log,
  Simulate alert, Run check now, so the Settings tab stays short.

### 4.6 Event log (developer)
- Chronological list of recorded analytics events (name, params, timestamp), filter by name, clear, share as text.
  Each event is coloured by category (alert, background, settings, usage).

### 4.7 Navigation
- Bottom bar with 3 tabs: Earthquakes, Alerts, Settings (a navigation rail on wide windows: landscape, tablets). Detail, Developer tools and Event log are pushed on top
  (no bottom bar, back arrow).
- Start: onboarding graph if not completed, otherwise main graph.
- Notification tap → app opens directly on the earthquake detail (back goes to the list).

## 5. Alert rules (single source of truth)

### 5.1 Matching
An earthquake triggers an alert when **all** hold:
1. Alerts are enabled.
2. `magnitude >= threshold` (events with null magnitude never alert).
3. Area is whole world, or distance(area center, epicenter) `<= radiusKm`.
4. Its id has never been notified before (stored in Room).
5. Its origin time is `>= alertBaseline` (time the alert settings were last saved/changed). No baseline yet (settings
   never saved) → nothing alerts.
6. Its origin time is within the last **6 hours** (`AlertConfig.MAX_EVENT_AGE`) — stale events after long offline periods
   stay in the list but do not notify.

### 5.2 Background check (periodic, every 15 min, requires network)
1. Read settings; if alerts disabled → finish.
2. Query USGS with `starttime = now - MAX_EVENT_AGE`, `updatedafter = lastCheckedAt - 10 min overlap`
   (first run: baseline), `minmagnitude = threshold`, and circle params when an area is set.
3. Apply rules 5.1 (the API already filters 2–3; they are re-checked locally, cheaply).
4. Post notifications: one per event up to 3; if more, one group summary ("5 new earthquakes above M4.5").
5. Store notified ids (pruned after 30 days) and `lastCheckedAt`; log analytics events.
6. Network/server error → `Result.retry()` with backoff; log failure event.
7. If the notification permission is missing → do not post, log a suppressed event.

### 5.3 Notification content
- Title: "M5.3 earthquake · 12 km from Izmir" (distance part only when an area is set).
- Text: place + local time. Channel "Earthquake alerts", high importance. Tap → detail.

## 6. Non-functional requirements
- minSdk 26, target/compile SDK 36. Portrait and landscape both work; supports font scaling and TalkBack labels.
- Works offline with cached data. No personal data leaves the device except the USGS query (area center coordinates)
  and the geocoding query performed by the platform geocoder.
- Analytics never store exact coordinates or city names (country code + radius only).
- All texts localized (EN, TR); dates, times and numbers formatted with the app locale.
