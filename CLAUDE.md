# QuakeAlert — Working Guide

Native Android app (Kotlin, Jetpack Compose). Users browse recent earthquakes worldwide, set a magnitude threshold and
an optional area (city + radius), and get a local notification when a new matching earthquake is published by USGS.

## Read first (every session)
1. `CLAUDE.local.md` if present (local-only context).
2. `docs/PLAN.md` — phases, task checklist, progress and work log. **Source of truth for what to do next.**
3. The doc relevant to the task:
   - `docs/SPEC.md` — product definition, scope, screens, behaviour rules.
   - `docs/ARCHITECTURE.md` — packages, layers, data flow, background work, i18n, conventions.
   - `docs/DECISIONS.md` — architecture decision records (why things are the way they are).
   - `docs/ANALYTICS.md` — event dictionary and success metrics.
   - `docs/TESTING.md` — test strategy, emulator matrix, manual QA checklist.
   - `docs/AI_USAGE.md` — AI tooling and usage report.

## Commands
```bash
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (emulator running)
./gradlew lintDebug              # Android lint
```
All three of `assembleDebug testDebugUnitTest lintDebug` must pass before a task is marked done.

## Rules
- Package: `com.ahmetyildiz.quakealert`. Feature-first Clean Architecture, MVVM in presentation (see ARCHITECTURE).
- Code, identifiers and docs in English. No Turkish anywhere in the project except `README.md` (Turkish during
  development, translated to English at the end) and the Turkish translations in `res/values-tr/strings.xml`.
- No comments in code or build/resource files (no `//`, `/* */`, KDoc, `<!-- -->`, `#`). Names must explain the code;
  the "why" goes into `docs/DECISIONS.md` / `docs/ARCHITECTURE.md`.
- Every user-facing string lives in `res/values/strings.xml` (English, default) and `res/values-tr/strings.xml`.
  No hard-coded UI text.
- Tunable values (thresholds, radius options, intervals, limits) live in one place per feature as named constants.
- Keep functions small and explicit; prefer readability over cleverness — the code must be easy to explain and change.
- Do not commit, push or create branches unless explicitly asked.
- When a task is finished: tick it in `docs/PLAN.md`, add a work-log row, record new decisions in `docs/DECISIONS.md`.
