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
- Runner: `HiltTestRunner` (starts `HiltTestApplication`). Every `@HiltAndroidTest` gets, through `@TestInstallIn`
  modules in `testing/`: an in-memory Room database, a fresh DataStore file and `FakeUsgsApi` instead of Retrofit
  (no network, no leftover state between tests). Tests that need them replace the notifier and the permission check
  with `@UninstallModules` + `@BindValue`, and initialise a test WorkManager with `HiltWorkerFactory` (the test
  application is not a `Configuration.Provider`).
- Compose UI tests (screen composables with fixed UI states): list states, filter chips, sort, detail, alert
  settings interactions, area selector, onboarding pages, settings, developer tools, event log.
- End to end (`navigation/OnboardingFlowTest`, real `MainActivity` and ViewModels): first launch → welcome → setup →
  notifications → Finish → earthquake list from the fake USGS, settings + baseline saved, periodic check enqueued;
  a completed onboarding opens straight on the list.
- Worker (`AlertCheckWorkerTest`, `TestListenableWorkerBuilder` + `HiltWorkerFactory`, fake USGS and notifier):
  skipped before settings are saved, a new matching earthquake is notified once across two runs, an earthquake
  older than the baseline is not, network error → retry, unreadable response → failure, analytics recorded.
- Room DAOs and the WorkManager scheduler against the real libraries.

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
- [ ] Deep link: `adb shell am start -a android.intent.action.VIEW -d "quakealert://earthquake/<id>"` with the app
      closed and open (another tab): detail opens, back goes to the list, one `app_opened`, no `earthquake_list_viewed`
      on cold start; an unknown id shows "not found".
- [ ] Developer → Simulate alert → notification appears → tap → detail; second simulate of same event → no duplicate.
- [ ] Developer → Run check now → `background_check_completed` in Event log.
- [ ] Periodic work scheduled: `adb shell dumpsys jobscheduler | grep quakealert`.
- [ ] Doze: `adb shell dumpsys deviceidle force-idle` → work deferred; `adb shell dumpsys deviceidle unforce` → runs.
- [ ] Reboot emulator → periodic work still scheduled.
- [ ] Event log shows expected events for the flows above; no coordinates or city names in params.

## 4. Verifying notifications and background checks

### Notification taps
A notification can open the app in three different ways, and each one takes a different code path (ADR-031):

| Scenario | How to get there | Expected |
|---|---|---|
| Warm | App running (e.g. on the Alerts tab), tap the notification | Detail opens; `alert_notification_opened` only |
| Cold | Leave the app with Back, end the process, tap the notification | Detail opens, Back → list; `app_opened` (source notification) + `alert_notification_opened` |
| Restored | Leave the app with Home, end the process, tap the notification | The tapped event opens (not the previous screen); same events as cold |
| Summary | Post more than 3 alerts at once | One "N new M… earthquakes" notification; tap opens the app; `event_id=summary` |

**Posting a notification.** Debug builds: Settings → Developer tools (opens its own screen). Enter a magnitude and (with a city area) the
distance from the city; "Simulate now" delivers it at once, "Schedule" after the delay in minutes — also with the app
closed (leave with Home or swipe it away; do not force-stop). Values that match the alert settings give a
notification, others give none (`developer_simulated_alert outcome=not_matched` in Logcat `Analytics`). "Simulate the
same alert again" must post nothing. Scheduled runs usually arrive within a minute of the chosen time. Before the developer tools existed (task 2.5), a temporary, never-committed hook in
`MainActivity` passed cached earthquakes to `NotifyAlertsUseCase` (started with `adb shell am start … --ei <extra>
<count>`); that is still the way to post several at once to see the summary notification.

**Useful commands**
- Open the notification shade: `adb shell cmd statusbar expand-notifications`.
- End the app process (debug build): `adb shell run-as com.ahmetyildiz.quakealert kill $(adb shell pidof com.ahmetyildiz.quakealert)`
  (`am kill` does not stop a recently used process; `am force-stop` also removes the notifications). Alternative:
  Developer options → "Don't keep activities", or Android Studio → Logcat → Terminate app.
- Allow notifications on API 33+ without the dialog: `adb shell pm grant com.ahmetyildiz.quakealert android.permission.POST_NOTIFICATIONS`.
- Check the events: Logcat tag `Analytics` or Settings → Developer tools → Event log.
- Clear the shade between runs: Android groups 4+ notifications of an app into a system group whose tap opens the
  app normally, which hides the scenario being tested.
- Language check: `adb shell cmd locale set-app-locales com.ahmetyildiz.quakealert --locales tr` (API 33+), post
  again, read titles with `adb shell dumpsys notification --noredact | grep android.title`.

### Background check
- Job present (and after a reboot): `adb shell dumpsys jobscheduler | grep -A 12 "com.ahmetyildiz.quakealert/androidx.work"`
  (network constraint visible under `Network type`).
- Enabling alerts (or saving settings) runs a first check at once → `background_check_completed` in Logcat `Analytics`.
- A forced run of the periodic job (`adb shell cmd jobscheduler run -f [-n androidx.work.systemjobscheduler]
  com.ahmetyildiz.quakealert <jobId>`, the namespace on API 34+) is postponed by WorkManager before its time; use
  Developer tools → Run check now for manual checks.
- Worker in a fresh process: end the app process (see above), then run the job; the notification texts must follow
  the in-app language on API 31/32 too (ADR-034).
- Database upgrades: install the previous build, create data, install the new build over it, check
  `pragma user_version` and the rows (`adb shell run-as com.ahmetyildiz.quakealert cat databases/quakealert.db`).

## 5. Definition of done for any task
`./gradlew assembleDebug testDebugUnitTest lintDebug` passes, new logic has unit tests, affected flows checked on at
least one emulator (both API sides for Geocoder / permission / language changes).
