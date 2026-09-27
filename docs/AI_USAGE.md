# AI Usage

## Tools
| Tool | Used for |
|---|---|
| Claude Code (VS Code extension) | Research, planning documents, code, tests, emulator testing over adb, documentation |
| Google Stitch (through its MCP server in Claude Code) | Screen designs and the design system, read and implemented from Claude Code |

## What was delegated to AI
- **Research:** USGS query parameters tried with real requests (`updatedafter`, circle filter, `eventid` 404),
  Android background-work limits (WorkManager, Doze, exact alarms), `Geocoder` behaviour below and above API 33.
- **Planning documents:** spec, architecture, decision records, test plan and analytics plan, written from the
  decisions I made.
- **Design:** the design brief for Stitch; reading the Stitch design through MCP and applying its colours, type
  scale, shapes and screen layouts.
- **Implementation:** every task of the plan (`docs/PLAN.md`), with its unit and instrumented tests.
- **Testing on emulators:** driving the app over adb on API 31 and API 34, reading Logcat, `dumpsys` and the Room
  database to confirm behaviour.
- **Texts:** English and Turkish strings, documentation drafts.

## What stayed with me
- Product decisions: target user, scope and priorities, data source, the area model (city + radius, no bundled data,
  no extra geocoding service), WorkManager as the background mechanism, multilingual support.
- Reviewing and accepting every task; asking for changes when the result did not fit (for example list sorting,
  moving the developer tools to their own screen, a parameterised alert simulation, no comments in source files).
- Final wording of the documentation.

## How outputs were verified
- API behaviour was checked with real requests before anything was designed around it.
- A task counted as done only after `./gradlew assembleDebug testDebugUnitTest lintDebug` passed.
- Every task was checked by hand on the API 31 and API 34 emulators. Code paths that differ below and above
  Android 13 (Geocoder, notification permission, app language) were tested on both.
- At the end, the full QA matrix ran on both emulators ([TESTING.md](TESTING.md)): 361 unit tests, 77 instrumented
  tests.
- Bugs found on the emulators were fixed at their root cause. Fixes that changed the design are recorded in
  [DECISIONS.md](DECISIONS.md) — for example notifications in the wrong language when the check runs without an open
  screen (ADR-034) and a notification tap opening the wrong screen after the process was killed (ADR-031); the others
  are noted in the task results of [PLAN.md](PLAN.md), such as the missing `POST_NOTIFICATIONS` declaration (task 2.4).

## Usage report

**Model usage ratio:** 100% **Claude Opus 5.5** (`claude-opus-5-5`). No other model was used in this project's
Claude Code sessions. Stitch runs on Google's side; its usage is not visible in these logs.

Generated on 2026-09-27 17:14 (+03:00) with [`ccusage`](https://github.com/ryoppippi/ccusage) 20.0.24 from the local
Claude Code logs:

```bash
npx ccusage@latest session --since 20260925 --json
```

Only this project's sessions are included: the session ids were matched against the log folder of this repository
(`~/.claude/projects/<this repository>/`). One session outside the repository (started before this project) is
excluded. One session is the resumed continuation of the previous one and repeats its history; ccusage counts each
message only once (checked against the raw logs), so nothing is double-counted.

| Session | Work | Input | Output | Cache write | Cache read | Total tokens | Share |
|---|---|---:|---:|---:|---:|---:|---:|
| `2f61661f` | Planning (09-25) and documentation, phase 4 (09-27) | 156 | 121,668 | 377,929 | 12,307,354 | 12,807,107 | 3.8% |
| `97c6522c` | Phase 0: toolchain, dependencies, skeleton, i18n, design system | 322 | 192,289 | 353,029 | 33,822,132 | 34,367,772 | 10.2% |
| `29293f75` + `355e00b6` | Phase 1: core, earthquake data, list, detail, navigation | 572 | 447,182 | 1,352,545 | 97,903,826 | 99,704,125 | 29.5% |
| `00820041` | Phase 2: alerts, city search, notifications, worker, onboarding, developer tools | 562 | 489,019 | 777,557 | 121,255,192 | 122,522,330 | 36.3% |
| `9e670e51` | Phase 3: settings, event log, polish, instrumented tests, QA matrix | 424 | 234,525 | 947,343 | 66,180,886 | 67,363,178 | 19.9% |
| `3201ca66` | Short start of phase 4 (continued in `2f61661f`) | 28 | 4,799 | 59,099 | 893,897 | 957,823 | 0.3% |
| **Total** | | **2,064** | **1,489,482** | **3,867,502** | **332,363,287** | **337,722,335** | **100%** |

- 98.4% of all tokens are **cache reads**: Claude Code re-sends the conversation context on every step and most of it
  is served from the prompt cache. New output written by the model is 1.49 M tokens.
- Development ran on a Claude subscription. For reference, ccusage prices the same usage at **$127.21** at API list
  prices.
