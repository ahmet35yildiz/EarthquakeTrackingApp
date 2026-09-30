# QuakeAlert — Measurement Plan and Event Dictionary

Events are recorded locally (Room `analytics_events` + Logcat tag `Analytics`) through the `AnalyticsTracker`
interface and can be inspected in Settings → Developer tools → Event log (debug builds). A remote backend (e.g. Firebase) can be
plugged in later as another `AnalyticsTracker` implementation.

## 1. What "the product works" means

| Question | Metric | Events |
|---|---|---|
| Do new users finish setting up alerts? | **Activation rate** = `onboarding_completed` / `onboarding_started` | onboarding_* |
| Can we actually reach them? | **Notification opt-in rate** = granted / requested | notification_permission_result |
| Do users personalise alerts? | % with an area set; threshold distribution; located vs. searched areas | onboarding_completed, alert_*, current_location_used, city_search_performed |
| Are alerts valuable? (**north star**) | **Alert open rate** = `alert_notification_opened` / `alert_notification_posted`; median time to open | alert_notification_* |
| Are alerts relevant to the people who answer? | **Useful rate among answers** = useful / answered `alert_feedback_given`; answer rate = answered / opened. Not answering is never counted as "not useful" | alert_feedback_given, alert_notification_opened |
| Are alerts too noisy? | Share of notifications followed within 24 h by alerts turned off or the threshold raised (settings only) | alert_notification_posted, alert_threshold_changed, alerts_toggled |
| Is the list useful on its own? | Detail views per list view; share/map actions; which sort orders people use | earthquake_list_viewed, earthquake_detail_viewed, detail_action_clicked, list_sort_changed |
| Do people who feel an earthquake report it? | Felt reports per detail view, by magnitude | felt_reported, earthquake_detail_viewed |
| Is the statistics view used, and for what? | Statistics opens per active user; share of 30-day and "near city" views | statistics_viewed |
| Do people prepare with the safety guide? | Guide opens per active user; share of opens that reach "during" / "after" | safety_guide_viewed |
| Are the emergency tools used? | Starts per tool; share of starts stopped by the user | emergency_tool_toggled |
| Is the pipeline reliable? | Background check success rate; refresh failure rate | background_check_*, earthquake_list_refreshed |

## 2. Event dictionary

33 events. Parameter values are strings. No personal data: never log coordinates or city names.

| Event | Params | When |
|---|---|---|
| `app_opened` | `source` = launcher \| notification | Main activity opened from launcher or notification tap, including a restore after the process was killed (not on recreation after rotation or a language switch) |
| `onboarding_started` | – | First onboarding screen shown |
| `onboarding_step_viewed` | `step` = welcome \| alert_setup \| notifications | Each step shown |
| `onboarding_completed` | `threshold`, `has_area`, `radius_km`, `notifications_granted` | Finish tapped |
| `notification_permission_requested` | `context` = onboarding | Before system dialog (Alerts and Settings open the system notification page instead of the dialog) |
| `notification_permission_result` | `granted` | Dialog result |
| `alerts_toggled` | `enabled` | Alerts switch changed |
| `alert_threshold_changed` | `from`, `to`, `context` = onboarding \| settings | Threshold saved |
| `alert_area_set` | `country_code`, `radius_km`, `context` | City + radius saved |
| `alert_area_cleared` | `context` | Switched to whole world |
| `city_search_performed` | `country_code`, `result_count` | Search completed |
| `city_search_failed` | `reason` = network \| unavailable \| unknown | Search failed |
| `current_location_used` | `result` = success \| permission_denied \| location_off \| not_found \| network \| unknown | "Use my location" finished (success = the city was set), or the permission was denied |
| `earthquake_list_viewed` | `region_filter` = world \| near_city, `magnitude_filter` = all \| above_threshold, `sort` = newest_first \| largest_first \| nearest_first | List screen shown, also when coming back from the detail (not after rotation / language switch) |
| `earthquake_list_refreshed` | `trigger` = initial \| pull \| stale (pull = any user-started refresh: pull, refresh button, retry), `result` = success \| failure, `count` (cached earthquakes after the refresh) | Refresh finished |
| `list_filter_changed` | `filter` = region \| magnitude, `value` (same values as above) | Chip tapped |
| `list_sort_changed` | `sort` = newest_first \| largest_first \| nearest_first | Sort order picked |
| `earthquake_detail_viewed` | `source` = list \| notification (an external `quakealert://` link counts as list; the app only publishes it in notifications), `magnitude` (omitted when unknown) | Detail loaded; once per opened detail (not for not-found / error, not again after rotation) |
| `detail_action_clicked` | `action` = map \| usgs \| share | Detail action |
| `statistics_viewed` | `period` = last_7_days \| last_30_days, `region_filter` = world \| near_city | Statistics tab shown (not after rotation) and each period / area chip change |
| `emergency_tool_toggled` | `tool` = whistle \| strobe, `enabled` = true \| false | Start / Stop tapped on an emergency tool (automatic stops are not recorded) |
| `safety_guide_viewed` | `section` = before \| during \| after | Safety guide opened (its first section) and each tab switch; not again after rotation |
| `felt_reported` | `event_id`, `magnitude` (omitted when unknown) | "I felt it" tapped on the detail (before the USGS form opens; whether the form was sent is not visible to the app) |
| `alert_notification_posted` | `event_id` (`summary` for the summary), `magnitude` (largest for the summary), `batch_size` | Once per notification handed to Android while app notifications and the "Earthquake alerts" category were on (up to 3 individual ones, or 1 summary). Handed over, not seen: Do Not Disturb or a dismissed notification are invisible to the app; `alert_notification_opened` is the only "seen" signal |
| `alert_notification_suppressed` | `reason` = permission_denied (app notifications off) \| alert_channel_blocked (app allowed, "Earthquake alerts" category off) | Match found but cannot notify; the match is not remembered and is retried by the next check |
| `alert_notification_opened` | `event_id` (`summary` for the summary), `delay_seconds` | Notification tapped (cold or warm start) |
| `alert_feedback_given` | `event_id`, `useful` = true \| false | "Was this alert useful?" answered on a detail opened from a notification; once per opened alert, nothing when ignored |
| `background_check_completed` | `fetched`, `matched`, `notified`, `duration_ms` | Worker success |
| `background_check_failed` | `reason` = network \| server \| parsing \| unknown | Worker failure/retry |
| `language_changed` | `from`, `to` = language tag \| system | Language picked |
| `theme_changed` | `from`, `to` = system \| light \| dark | Theme picked |
| `developer_simulated_alert` | `outcome` = posted \| already_notified \| not_matched \| notifications_off \| alerts_off \| nothing_to_repeat, `scheduled` | Simulated alert delivered or rejected (debug; immediately or when the scheduled one runs) |
| `developer_check_triggered` | – | Run check now (debug) |

The Event log screen (debug builds) shows these on the device as "Alert metrics on this device": setup completion,
alert open rate, useful rate among answers and the 24 h opt-out share. Simulated alerts from the developer
tools count like real ones there, so the view can be demonstrated; a remote pipeline would filter `simulated-` ids.
