# QuakeAlert — Measurement Plan and Event Dictionary

Events are recorded locally (Room `analytics_events` + Logcat tag `Analytics`) through the `AnalyticsTracker`
interface and can be inspected in Settings → Developer → Event log. A remote backend (e.g. Firebase) can be plugged
in later as another `AnalyticsTracker` implementation.

## 1. What "the product works" means

| Question | Metric | Events |
|---|---|---|
| Do new users finish setting up alerts? | **Activation rate** = `onboarding_completed` / `onboarding_started` | onboarding_* |
| Can we actually reach them? | **Notification opt-in rate** = granted / requested | notification_permission_result |
| Do users personalise alerts? | % with an area set; threshold distribution | onboarding_completed, alert_* |
| Are alerts valuable? (**north star**) | **Alert open rate** = `alert_notification_opened` / `alert_notification_posted`; median time to open | alert_notification_* |
| Are alerts too noisy? | Threshold raised or alerts disabled within 24 h after a notification | alert_threshold_changed, alerts_toggled |
| Is the list useful on its own? | Detail views per list view; share/map actions; which sort orders people use | earthquake_list_viewed, earthquake_detail_viewed, detail_action_clicked, list_sort_changed |
| Is the pipeline reliable? | Background check success rate; refresh failure rate | background_check_*, earthquake_list_refreshed |

## 2. Event dictionary

Parameter values are strings. No personal data: never log coordinates or city names.

| Event | Params | When |
|---|---|---|
| `app_opened` | `source` = launcher \| notification | Main activity created from launcher or notification tap (not on recreation after rotation or a language switch) |
| `onboarding_started` | – | First onboarding screen shown |
| `onboarding_step_viewed` | `step` = welcome \| alert_setup \| notifications | Each step shown |
| `onboarding_completed` | `threshold`, `has_area`, `radius_km`, `notifications_granted` | Finish tapped |
| `notification_permission_requested` | `context` = onboarding \| settings | Before system dialog |
| `notification_permission_result` | `granted` | Dialog result |
| `alerts_toggled` | `enabled` | Alerts switch changed |
| `alert_threshold_changed` | `from`, `to`, `context` | Threshold saved |
| `alert_area_set` | `country_code`, `radius_km`, `context` | City + radius saved |
| `alert_area_cleared` | `context` | Switched to whole world |
| `city_search_performed` | `country_code`, `result_count` | Search completed |
| `city_search_failed` | `reason` = network \| unavailable \| unknown | Search failed |
| `earthquake_list_viewed` | `region_filter` = world \| near_city, `magnitude_filter` = all \| above_threshold, `sort` = newest_first \| largest_first \| nearest_first | List screen shown (not again after rotation / language switch) |
| `earthquake_list_refreshed` | `trigger` = initial \| pull \| stale (pull = any user-started refresh: pull, refresh button, retry), `result` = success \| failure, `count` (cached earthquakes after the refresh) | Refresh finished |
| `list_filter_changed` | `filter` = region \| magnitude, `value` (same values as above) | Chip tapped |
| `list_sort_changed` | `sort` = newest_first \| largest_first \| nearest_first | Sort order picked |
| `earthquake_detail_viewed` | `source` = list \| notification, `magnitude` | Detail shown |
| `detail_action_clicked` | `action` = map \| usgs \| share | Detail action |
| `alert_notification_posted` | `event_id`, `magnitude`, `batch_size` | Notification posted |
| `alert_notification_suppressed` | `reason` = permission_denied | Match found but cannot notify |
| `alert_notification_opened` | `event_id`, `delay_seconds` | Notification tapped |
| `background_check_completed` | `fetched`, `matched`, `notified`, `duration_ms` | Worker success |
| `background_check_failed` | `reason` = network \| server \| parsing \| unknown | Worker failure/retry |
| `language_changed` | `from`, `to` = language tag \| system | Language picked |
| `developer_simulated_alert` | – | Simulate alert (debug) |
| `developer_check_triggered` | – | Run check now (debug) |

## 3. Implementation notes
- Event names and params are defined once in `core/analytics/AnalyticsEvent.kt` (sealed class) — no free-form
  strings at call sites. Enum-like values are enums in `AnalyticsParameters.kt`, logged as the constant name in
  lower case. Numbers are logged with `toString()` (e.g. `4.5`, `250`); booleans as `true` / `false`.
- Tracking calls happen in ViewModels / use cases / worker, never inside composables' recomposition paths
  (use `LaunchedEffect` keyed on the screen for "viewed" events).
- `LocalAnalyticsTracker` writes on an IO dispatcher and never throws to callers.
