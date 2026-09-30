# AI Usage

## Tools
| Tool | Used for |
|---|---|
| Claude Code | Research, planning documents, code, tests, documentation |
| Google Stitch (through its MCP server in Claude Code) | Screen designs and the design system, read and implemented from Claude Code |

## What was delegated to AI
- **Research:** USGS query parameters tried with real requests (`updatedafter`, circle filter, `eventid` 404, count
  endpoint), Android background-work limits (WorkManager, Doze, exact alarms), `Geocoder` and location behaviour
  below and above API 33, torch and `AudioTrack` APIs.
- **Planning documents:** spec, architecture, decision records, test plan and measurement plan, written from the
  decisions I made.
- **Design:** the design brief for Stitch; reading the Stitch design through MCP and applying its colours, type
  scale, shapes and screen layouts.
- **Implementation:** every task of the plan, with its unit and instrumented tests.
- **Testing:** driving the app over adb on the API 31 and API 34 emulators and a physical phone, reading Logcat,
  `dumpsys` and the Room database to confirm behaviour.
- **Texts:** English and Turkish strings, documentation drafts.

## What stayed with me
- Product decisions: target user, scope and priorities, data source, the area model (city + radius, no bundled data,
  no extra geocoding service), WorkManager as the background mechanism, multilingual support.
- What to add after the core was done and what not to build: "Use my location", the alert preview, the "Was this
  alert useful?" question with on-device metrics, "I felt it", the Statistics tab and the Emergency tab.
- Reviewing and accepting every task and asking for changes when the result did not fit, for example list sorting,
  the developer tools on their own screen, a parameterised alert simulation, onboarding without scrolling, etc.
- Review findings on notification channels, process death and offline details, which were then fixed with tests.
- Final wording of the documentation.

## How outputs were verified
- API behaviour was checked with real requests before anything was designed around it; the alert preview and the
  statistics were compared with the USGS count API.
- A task counted as done only after `./gradlew assembleDebug testDebugUnitTest lintDebug` passed; tasks with
  instrumented tests ran them on both emulators as well.
- Every task was checked by hand on the API 31 and API 34 emulators. Code paths that differ below and above
  Android 13 (Geocoder, notification permission, app language) were tested on both. The location, flashlight and
  whistle features were checked on a Samsung Galaxy S20 FE.
- Bugs found on the emulators were fixed at their root cause.

## Usage report

**Model usage ratio:** 100% **Claude Opus 5.5** (`claude-opus-5-5`). Stitch runs on Google's side; its usage is not
visible in these logs.

Generated on 2026-09-29 21:26 (+03:00) with [`ccusage`](https://github.com/ryoppippi/ccusage) 20.0.26 from the local
Claude Code logs:

```bash
npx ccusage@latest session --since 20260925 --json
```

| Session | Work | Input | Output | Cache write | Cache read | Total tokens | Share |
|---|---|---:|---:|---:|---:|---:|---:|
| `2f61661f` | Planning (09-25), README and first usage report (09-27) | 168 | 130,024 | 388,342 | 13,888,720 | 14,407,254 | 2.3% |
| `97c6522c` | Phase 0: toolchain, dependencies, skeleton, i18n, design system | 322 | 192,289 | 353,029 | 33,822,132 | 34,367,772 | 5.5% |
| `29293f75` + `355e00b6` | Phase 1: core, earthquake data, list, detail, navigation | 572 | 447,182 | 1,352,545 | 97,903,826 | 99,704,125 | 15.9% |
| `00820041` | Phase 2: alerts, city search, notifications, worker, onboarding, developer tools | 562 | 489,019 | 777,557 | 121,255,192 | 122,522,330 | 19.5% |
| `9e670e51` | Phase 3: settings, event log, polish, instrumented tests, QA matrix | 424 | 234,525 | 947,343 | 66,180,886 | 67,363,178 | 10.7% |
| `965c80cf` | Design alignment, theme choice, localized USGS place text | 392 | 211,256 | 447,630 | 55,590,559 | 56,249,837 | 9.0% |
| `acffe677` | "Use my location", alert switch vs. permission | 308 | 138,625 | 324,699 | 33,097,194 | 33,560,826 | 5.3% |
| `0cb0a7b5` | Review fixes, onboarding without scrolling, preview, feedback + metrics | 482 | 260,940 | 878,843 | 86,116,123 | 87,256,388 | 13.9% |
| `7b70c7f1` | "I felt it", Statistics and Emergency tabs, strobe light, whistle | 530 | 343,142 | 1,139,571 | 91,269,004 | 92,752,247 | 14.8% |
| `a8759f21` | Final documentation, test runs, clean-clone check, this report | 182 | 65,950 | 267,732 | 19,232,405 | 19,566,269 | 3.1% |
| **Total** | | **3,942** | **2,512,952** | **6,877,291** | **618,356,041** | **627,750,226** | **100%** |

- 98.5% of all tokens are **cache reads**: Claude Code re-sends the conversation context on every step and most of it
  is served from the prompt cache. New output written by the model is 2.51 M tokens.
