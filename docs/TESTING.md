# QuakeAlert — Testing Strategy

## 1. Automated tests

### Unit tests (`app/src/test`, JUnit Jupiter 6 + MockK + coroutines-test + Turbine, Arrange-Act-Assert)
| Area | What to cover |
|---|---|
| USGS mapper | null `mag` / `place`, depth from coordinates, epoch conversion, non-earthquake types |
| Distance / AlertArea | haversine known distances, boundary (exactly on radius), whole-world area |
| AlertMatcher | threshold boundary (equal passes), null magnitude, outside radius, already notified, before baseline, older than max age |
| CheckForNewAlertsUseCase | fetch → match → notify → store ids → lastCheckedAt; batch > 3 → summary; permission missing → suppressed; network error → retry result |
| Notification access | app off → `permission_denied`, app on + alert channel off → `alert_channel_blocked`; both suppressed, not remembered, delivered once allowed again; ViewModels expose the channel state |
| Repository | refresh replaces cache; failure keeps cache and returns error; `eventid` 404 → NotFound; fetches replace cached copies but never add events |
| Detail lookup | list: cache first; notification: USGS first, revised version wins, network error → cached copy flagged, 404 → cached copy unflagged |
| Feedback + metrics | feedback recorded once with the event id, kept across a restored screen; metrics built from real `AnalyticsEvent` objects: setup, opens, useful among answers only (non-answers excluded), opt-out within the window (end included, before / after / onboarding / lowered / turned on excluded), percent rounding and empty totals; metrics use all events while filtering |
| Alert preview | threshold boundary, radius, period start included, unknown magnitude, simulated events skipped, monotonic in threshold/radius; the shared rule equals delivery for eligible events; ViewModel: hidden / missing → load / load fails / stale → shown + refreshed / recount on change; config coverage of the preview period and threshold range |
| Onboarding restore | page, threshold and city + radius survive a new ViewModel on the same `SavedStateHandle`; notifications page without a restorable area → setup page |
| City search | country-code filtering, de-duplication, error mapping (with a fake geocoder) |
| Use my location | permission / location off / timeout → last known / nothing (fake location source); reverse lookup keeps the device point, skips country-level addresses; ViewModel states, located city delivery, analytics |
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
  settings interactions, area selector (city search dialog), onboarding pages, alert preview card, settings,
  developer tools, event log.
- Layout (`OnboardingLayoutTest`, on the emulator's own screen size): no onboarding step has anything to scroll
  (welcome, setup with whole world, setup with a city, notifications denied). `AlertSettingsScreenTest` checks that
  the preview slot gets the slider value while it is being dragged.
- End to end (`navigation/OnboardingFlowTest`, real `MainActivity` and ViewModels): first launch → welcome → setup →
  notifications → Finish → earthquake list from the fake USGS, settings + baseline saved, periodic check enqueued;
  a completed onboarding opens straight on the list.
- Worker (`AlertCheckWorkerTest`, `TestListenableWorkerBuilder` + `HiltWorkerFactory`, fake USGS and notifier):
  skipped before settings are saved, a new matching earthquake is notified once across two runs, an earthquake
  older than the baseline is not, network error → retry, unreadable response → failure, analytics recorded.
- Room DAOs and the WorkManager scheduler against the real libraries.
- Before `connectedDebugAndroidTest` on an emulator where the debug app finished onboarding by hand, run
  `adb shell pm clear com.ahmetyildiz.quakealert`: a periodic check that fires during the run starts WorkManager's job
  service in the test process, where `HiltTestApplication` has no WorkManager, and the run stops.

## 2. Emulator matrix (manual + instrumented)

The Geocoder, notification permission and per-app language code paths differ below and above API 33, so both sides
must be verified.

| AVD | API | Why | Status |
|---|---|---|---|
| `Pixel_6` | 31, Google APIs, arm64 | **Pre-33 paths:** blocking Geocoder, no runtime notification permission, AppCompat-stored locale | ready |
| `Pixel_7_API_34` (exists) | 34 | **33+ paths:** async Geocoder, `POST_NOTIFICATIONS`, system per-app language | ready |
| `Pixel_9_API_35` (exists) | 35 | Latest behaviour, edge-to-edge | ready |
| API 26 (optional) | 26 | minSdk smoke test | optional |

Emulators must use a **Google APIs** image (Geocoder backend needs Google Play services).

## 3. Manual QA checklist (run on API 31/32 and API 34/35)
Last full run: 2026-09-27 on `Pixel_6` (API 31) and `Pixel_7_API_34` — every item and the §4 scenarios passed
(summary notification tap: covered by tests, not repeated by hand); findings and fixes in `docs/PLAN.md` task 3.5.

- [ ] Fresh install → onboarding → permission dialog (34/35) / no dialog (31/32) → list.
- [ ] Deny permission → app works, banner in Alerts + Settings, "Open settings" works.
- [ ] Notifications allowed but the "Earthquake alerts" category turned off (system settings) → Alerts, Settings and
  the onboarding step name the category, their button opens the category page; Simulate → nothing shown,
  `alert_notification_suppressed {reason=alert_channel_blocked}`; turn it on → "Simulate the same alert again" posts,
  a second repeat is `already_notified`.
- [ ] Onboarding at font scale 1.0 on API 31 and 34, EN and TR: no step scrolls (also whole world vs. a city with a
  long region name). "Near a city" opens the "Choose a city" dialog; picking a result closes it.
- [ ] Simulate → tap the notification → "Was this alert useful?" on the detail; Yes → thank-you, kept after
  rotation, `alert_feedback_given` in the log. Opened from the list → no card. Open a second alert without answering
  → Event log metrics: useful rate unchanged, "1 of 2 opened alerts answered". Raise the threshold → opt-out 1.
- [ ] Alerts tab: changing the threshold, area or radius shows no "saved" message; the summary card updates.
- [ ] Preview (Alerts tab only, not in onboarding): whole world M4.5+ equals `https://earthquake.usgs.gov/fdsnws/event/1/count?starttime=<now-3d>&minmagnitude=4.5`;
  a city + radius equals the same query with `latitude`, `longitude`, `maxradiuskm`; the number changes while the
  slider is still held.
- [ ] Onboarding: pick a city + radius, go to step 3, Home, `adb shell am kill com.ahmetyildiz.quakealert`, reopen →
  step 3 again, Back shows the city, Finish saves it.
- [ ] City search: "Izmir" in Türkiye, "Tokyo" in Japan, nonsense text (empty state), airplane mode (error state).
- [ ] Use my location (onboarding and Alerts): tap → permission dialog (approximate only) → give the emulator a
      location while it is searching → city named in the app language, set directly; deny → message + "Open
      settings" (app details); location off → message + "Open settings" (location settings); geocoder error → retry;
      rotation while locating keeps the result. Giving a location: `adb emu geo fix <lon> <lat>` or Extended
      controls → Location (worked on API 31; the API 34 AVD ignored it), otherwise test providers:
      `adb shell appops set com.android.shell android:mock_location allow`, then for `gps`, `fused`, `network`:
      `adb shell cmd location providers add-test-provider <p>`, `… set-test-provider-enabled <p> true`,
      `… set-test-provider-location <p> --location 38.42,27.14 --accuracy 10`; afterwards `remove-test-provider <p>`
      and `appops set … default`.
- [ ] No area → warning visible in onboarding and Alerts.
- [ ] Language: switch EN ↔ TR in-app; survives app restart; system settings language page shows the app (33+).
- [ ] List: pull-to-refresh, filters, airplane mode → offline banner with cached data, dark mode, font scale 1.5×,
      rotation.
- [ ] Five tabs (Earthquakes, Statistics, Alerts, Emergency, Settings) in EN and TR at font scale 1.0, 1.3 and 2.0:
      labels complete and the same size; landscape: rail centred at 1.0, scrolls to Settings at 2.0.
- [ ] Strobe light on a physical device: Start → the flashlight flashes twice a second
      (`adb shell dumpsys media.camera | grep quakealert` shows ~4 switches per second) and the window has
      `KEEP_SCREEN_ON`; tab switch / Home → torch off and "Start" again; rotation → keeps flashing; Stop → off,
      `emergency_tool_toggled` with `enabled` true and false. Emulators have an emulated torch (visible in the same
      dump); "no flashlight" is covered by `EmergencyScreenTest`.
- [ ] Statistics: 7 days (no request) and 30 days (loading, then data; airplane mode → error, Retry), World and
      Near {city}; counts equal the USGS count API with the same window, `eventtype=earthquake`, `minmagnitude=2.5`
      (and `latitude` / `longitude` / `maxradiuskm` for the area); a chip change scrolls to the top; the largest
      earthquake opens its detail; EN / TR, dark mode, landscape, font scale 2.0.
- [ ] Whistle on a physical device: Start → three blasts and a pause, repeating, audible with the phone in silent
      mode; alarm volume at maximum while it plays (`adb shell cmd media_session volume --stream 4 --get`) and the
      old volume back after Stop; tab switch / Home stop it, rotation does not; whistle and strobe together.
      `AudioTrackWhistlePlayerTest` checks start / volume / stop with a real `AudioTrack` on the emulators.
- [ ] Emergency → Safety guide: Before / During / After switch, back returns to the Emergency tab; rotation keeps the
      section; `safety_guide_viewed` once per shown section.
- [ ] Detail: open in maps, I felt it (USGS form of the same event opens, `felt_reported` logged), open USGS, share; open from notification (cold start and warm start).
- [ ] Detail from a notification link (`?isFromNotification=true`) for a cached event: online → no banner; airplane
  mode → the saved copy with the "may be out of date" banner and Retry.
- [ ] Deep link: `adb shell am start -a android.intent.action.VIEW -d "quakealert://earthquake/<id>"` with the app
      closed and open (another tab): detail opens, back goes to the list, one `app_opened`, no `earthquake_list_viewed`
      on cold start; an unknown id shows "not found".
- [ ] Developer → Simulate alert → notification appears → tap → detail; second simulate of same event → no duplicate.
- [ ] Developer → Run check now → `background_check_completed` in Event log.
- [ ] Periodic work scheduled: `adb shell dumpsys jobscheduler | grep quakealert`.
- [ ] Doze: screen off (`input keyevent KEYCODE_SLEEP`), `adb shell dumpsys deviceidle force-idle` → the periodic
      job (`Required constraints: TIMING_DELAY CONNECTIVITY`) shows `DEVICE_NOT_DOZING` / `CONNECTIVITY` unsatisfied;
      `adb shell dumpsys deviceidle unforce` → both satisfied again, only the timing delay remains. A scheduled
      developer simulation is not a Doze probe: it has no constraints and WorkManager may run it in-process.
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
  (network constraint visible under `Network type`). API 34+ prints the job as
  `JOB androidx.work.systemjobscheduler:u0aNNN/N: … @androidx.work.systemjobscheduler@com.ahmetyildiz.quakealert/…`
  (namespace, no `#`); right after a reboot give the system a few seconds before reading it.
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
