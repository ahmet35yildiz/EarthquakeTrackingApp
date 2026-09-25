# QuakeAlert — Testing Strategy

## 1. Automated tests

### Unit tests (`app/src/test`, JUnit Jupiter 6 + MockK + coroutines-test + Turbine, Arrange-Act-Assert)
| Area | What to cover |
|---|---|
| USGS mapper | null `mag` / `place`, depth from coordinates, epoch conversion, non-earthquake types |
| Distance / AlertArea | haversine known distances, boundary (exactly on radius), whole-world area |
| AlertMatcher | threshold boundary (equal passes), null magnitude, outside radius, already notified, before baseline, older than max age |
| CheckForNewAlertsUseCase | fetch → match → notify → store ids → lastCheckedAt; batch > 3 → summary; permission missing → suppressed; network error → retry result |
| Repository | refresh replaces cache; failure keeps cache and returns error; `eventid` 404 → NotFound |
| City search | country-code filtering, de-duplication, error mapping (with a fake geocoder) |
| ViewModels | list states (loading/content/empty/error/offline), filter changes, alert settings save + baseline reset, onboarding steps |
| Locale | supported language list from `BuildConfig`, display names |

Fakes are preferred over mocks for repositories and the clock (`FakeClock`), so tests read like specifications.

### Instrumented tests (`app/src/androidTest`)
- Compose UI tests: list states render, filter chips, onboarding happy path, alert settings interactions.
- Worker: `TestListenableWorkerBuilder` with fakes (Hilt test module) — returns success/retry correctly.

## 2. Emulator matrix (manual + instrumented)

The Geocoder, notification permission and per-app language code paths differ below and above API 33, so both sides
must be verified.

| AVD | API | Why | Status |
|---|---|---|---|
| `Pixel_API_31` (to create) | 31 or 32, Google APIs, arm64 | **Pre-33 paths:** blocking Geocoder, no runtime notification permission, AppCompat-stored locale | installed manually by the user when needed |
| `Pixel_7_API_34` (exists) | 34 | **33+ paths:** async Geocoder, `POST_NOTIFICATIONS`, system per-app language | ready |
| `Pixel_9_API_35` (exists) | 35 | Latest behaviour, edge-to-edge | ready |
| API 26 (optional) | 26 | minSdk smoke test | optional |

Emulators must use a **Google APIs** image (Geocoder backend needs Google Play services).

## 3. Manual QA checklist (run on API 31/32 and API 34/35)
- [ ] Fresh install → onboarding → permission dialog (34/35) / no dialog (31/32) → list.
- [ ] Deny permission → app works, banner in Alerts + Settings, "Open settings" works.
- [ ] City search: "Izmir" in Türkiye, "Tokyo" in Japan, nonsense text (empty state), airplane mode (error state).
- [ ] No area → warning visible in onboarding and Alerts.
- [ ] Language: switch EN ↔ TR in-app; survives app restart; system settings language page shows the app (33+).
- [ ] List: pull-to-refresh, filters, airplane mode → offline banner with cached data, dark mode, font scale 1.5×,
      rotation.
- [ ] Detail: open in maps, open USGS, share; open from notification (cold start and warm start).
- [ ] Developer → Simulate alert → notification appears → tap → detail; second simulate of same event → no duplicate.
- [ ] Developer → Run check now → `background_check_completed` in Event log.
- [ ] Periodic work scheduled: `adb shell dumpsys jobscheduler | grep quakealert`.
- [ ] Doze: `adb shell dumpsys deviceidle force-idle` → work deferred; `adb shell dumpsys deviceidle unforce` → runs.
- [ ] Reboot emulator → periodic work still scheduled.
- [ ] Event log shows expected events for the flows above; no coordinates or city names in params.

## 4. Definition of done for any task
`./gradlew assembleDebug testDebugUnitTest lintDebug` passes, new logic has unit tests, affected flows checked on at
least one emulator (both API sides for Geocoder / permission / language changes).
