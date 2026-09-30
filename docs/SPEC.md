# QuakeAlert — Product Specification

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

**What this app is NOT:** an earthquake early-warning system. Notifications arrive after an event (USGS
publication delay + background check interval). The app says this explicitly in onboarding and in the README.

## 2. Scope

### In scope (MVP)
1. First-run onboarding: value proposition + disclaimer → alert setup (threshold, area) → notification permission.
2. Earthquake list: recent events, filters, pull-to-refresh, offline cache, loading/empty/error states.
3. Earthquake detail: all key facts, distance to the user's city, open in maps, "I felt it" (USGS form), open on
   USGS, share.
4. Alert settings: enable/disable, magnitude threshold, area (country → city search → radius, or "Use my
   location") or whole world.
5. Background check with WorkManager (every 15 min) + local notifications for new matching earthquakes.
6. Local analytics event log + developer screen to inspect it.
7. Developer tools: "Simulate alert" and "Run check now" to demonstrate the notification flow on demand.
8. Multilingual UI: English (default) + Turkish, in-app language switch.
9. Light and dark theme, consistent custom Material 3 design.
10. Emergency tab: whistle, strobe light, safety guide (before / during / after an earthquake).
11. Statistics tab: last 7 or 30 days, whole world or the saved area; overview, magnitude classes, earthquakes per
    day, most active regions.

### Out of scope (documented in README with reasons)
| Item | Reason |
|---|---|
| Push notifications (FCM) + backend | Needs a server that polls USGS and fans out pushes. Listed as a next step. |
| Map SDK | Needs API keys and setup; "Open in maps" intent covers the need. |
| Accounts / sync | Not needed for the core use case. |
| Multiple areas | Adds settings complexity; one area covers the main persona. |
| Imperial units | Kilometres only for MVP. |
