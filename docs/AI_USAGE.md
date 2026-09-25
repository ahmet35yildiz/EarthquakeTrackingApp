# AI Usage

> Draft — filled in during development; the usage report is generated at the end from real local logs.

## Tools
| Tool | Used for |
|---|---|
| Claude Code (VS Code extension) | Research, planning documents, code generation, tests, reviews |
| _to be completed_ | |

## What was delegated to AI
- API research and verification (USGS query parameters, Android background-work and Geocoder behaviour).
- Drafting planning documents (spec, architecture, ADRs, test plan) from decisions made by me.
- _to be completed per phase (scaffolding, boilerplate, tests, translations…)_

## What stayed with me
- Product decisions: target user, scope, area model, alert rules, priorities.
- Reviewing every generated change, running the app on emulators, final wording.

## How outputs were verified
- API behaviour checked with real requests (`curl`) before designing around it.
- Every task gated by `./gradlew assembleDebug testDebugUnitTest lintDebug`.
- Manual QA on API 31/32 and API 34/35 emulators (`docs/TESTING.md`).
- _to be completed_

## Usage report
Generated with [`ccusage`](https://github.com/ryoppippi/ccusage) from local Claude Code logs, limited to this
project's sessions:

```bash
npx ccusage@latest daily --since 20260925 --breakdown
```

| Model | Input tokens | Output tokens | Cache tokens | Share |
|---|---|---|---|---|
| _to be filled from the report_ | | | | |
