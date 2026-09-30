# QuakeAlert

An Android app that shows recent earthquakes worldwide and sends a local notification when a new earthquake matches
the user's own threshold and area (a city + radius, or the whole world). Data: USGS.

**Not an early-warning system.**

## Run it
**Needs:** Android Studio Quail 4 (2026.1.4) or newer (required by AGP 9.4), JDK 17+, Android SDK 36, an emulator or
device with Android 8.0 (API 26)+. The emulator needs a **Google APIs** image: city search uses the platform
geocoder.

1. Clone the repository and open it in Android Studio; wait for the Gradle sync.
2. Run the `app` configuration.
3. Finish the three onboarding steps (pick a threshold and optionally a city, allow notifications).


552 unit tests, 135 instrumented tests on API 31 and on API 34, all passing; lint
clean. Details: [docs/TESTING.md](docs/TESTING.md).

### See an alert without waiting for an earthquake (debug build only)
Settings → **Developer tools**. This entry and its screens exist only in debug builds; release builds do not contain
them.
- **Alert testing → Simulate now:** creates a test earthquake with the magnitude and distance you enter and sends it
  through the same matching and notification step as real alerts. It notifies only if it matches your settings.
  Tap the notification → detail with "Was this alert useful?". "Simulate the same alert again" shows that the same
  earthquake is not notified twice.
- **Schedule:** the same, after a delay in minutes, also with the app closed.
- **Run check now:** runs the real USGS background check once.
- **Event log:** shows every recorded analytics event and the alert metrics computed on the device.

## Key product decisions
- **Target user:** someone who wants to know quickly when an earthquake that matters happens near a place they care
  about (their city, their family's city), without being disturbed by the many small ones that happen every day.
- **Approach — few, relevant alerts:** the user picks a minimum magnitude and optionally a city + radius. The app
  checks USGS in the background and notifies only for new matching earthquakes; it never notifies the same
  earthquake twice. An on-screen preview shows how many earthquakes of the last 3 days the chosen settings would have
  matched.
- **USGS as the only source:** global, keyless, with a native circle filter and `updatedafter` for late-published
  events. Regional sources cover one country only.
- **Area = circle, no bundled data:** earthquakes do not stop at borders; distance matters. Countries come from the
  platform, cities from Android's `Geocoder`.
- **WorkManager every 15 minutes:** the most reliable, battery-friendly option without a server. Exact alarms need an
  extra permission; a foreground service means a permanent notification. Instant alerts need a backend (next step).
- **Around the core:** offline-first list (7 days, M2.5+) with filters and sorting; detail with distance, maps,
  "I felt it" (USGS form) and share; a Statistics tab (7 / 30 days); an Emergency tab (whistle, strobe light, safety
  guide); English + Turkish, adding a language is one `strings.xml`; light / dark theme.
- **Code:** feature-first Clean Architecture, MVVM, one module; tunable values live in one `*Config` object per
  feature (e.g. `AlertConfig`).

Product definition and scope: [docs/SPEC.md](docs/SPEC.md). Design: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

**Measuring it:** events are stored on the device only (Room, no external service, no coordinates or city names).
Main metric: alert open rate (opened / posted notifications); also setup completion, "useful" answers and alerts
turned off within 24 h of a notification. See [docs/ANALYTICS.md](docs/ANALYTICS.md).

## Out of scope
| Item | Why |
|---|---|
| Push notifications (FCM) + backend | Needs a server; the only way to instant alerts. First next step. |
| Map SDK | API keys and setup; "Open in maps" covers the need. |
| Accounts / sync | Not needed for the core use on one phone. |
| Several areas | More settings complexity; one area covers the main need. |
| Widget | Not part of the core loop. |

## Time spent
About **20 hours** of logged work (20 h 12 min) between 2026-09-25 and 2026-09-29.

| Work | Time |
|---|---|
| Analysis, research, product and architecture decisions, planning | 1 h 15 min |
| Foundation: toolchain, dependencies, skeleton, languages, design system | 1 h 47 min |
| Core, earthquake list and detail | 2 h 43 min |
| Alerts: city search, settings, notifications, background check, onboarding, developer tools | 3 h 02 min |
| Settings, event log, polish, instrumented tests, QA on API 31 + 34 | 2 h 15 min |
| Design alignment and refinements (preview, feedback, metrics, review fixes) | 4 h 34 min |
| Additions: "Use my location", "I felt it", Statistics, Emergency tools | 3 h 47 min |
| Documentation and usage report | 49 min |

## Next steps
1. **Backend + FCM push:** a server polls USGS continuously and pushes matching alerts instantly, also in Doze.
2. **Remote analytics:** send the same events to a service such as Firebase Analytics by adding one more
   `AnalyticsTracker` implementation.
3. **Regional data sources** (e.g. AFAD for Türkiye) for better coverage of small local earthquakes.

## AI usage
- **Tools:** Claude Code for research, planning documents, code and tests; Google Stitch for the screen designs (read
  into Claude Code through the Stitch MCP server).
- **Delegated:** trying the USGS API with real requests, the design brief, unit and instrumented tests, emulator
  checks, English and Turkish texts.
- **Kept by me:** target user, scope and priorities; data source, area model and background method; which features
  to add and which not to build; reviewing and accepting each task and asking for changes.
- **Verified by:** real API requests before designing around them; no task counted as done before `assembleDebug`,
  `testDebugUnitTest` and `lintDebug` passed; manual checks on the API 31 and API 34 emulators (code paths that differ
  before and after Android 13 on both sides), checks on a physical phone for location, the flashlight and the
  whistle; counts compared with the USGS count API.
- **Usage report:** 100% Claude Opus 5.5 across all Claude Code sessions of this project. Per-session tokens and how
  they were measured: [docs/AI_USAGE.md](docs/AI_USAGE.md).
