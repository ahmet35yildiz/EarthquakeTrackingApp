package com.ahmetyildiz.quakealert.core.analytics

import com.ahmetyildiz.quakealert.core.appearance.ThemeMode
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource

class AnalyticsEventTest {

    @ParameterizedTest
    @MethodSource("dictionary")
    fun `event is recorded with the name and params of the event dictionary`(
        event: AnalyticsEvent,
        expectedName: String,
        expectedParams: Map<String, String>,
    ) {
        assertEquals(expectedName, event.name)
        assertEquals(expectedParams, event.params)
    }

    companion object {

        @JvmStatic
        fun dictionary(): List<Arguments> = listOf(
            entry(
                AnalyticsEvent.AppOpened(AppOpenSource.NOTIFICATION),
                "app_opened",
                "source" to "notification",
            ),
            entry(AnalyticsEvent.OnboardingStarted, "onboarding_started"),
            entry(
                AnalyticsEvent.OnboardingStepViewed(OnboardingStep.ALERT_SETUP),
                "onboarding_step_viewed",
                "step" to "alert_setup",
            ),
            entry(
                AnalyticsEvent.OnboardingCompleted(threshold = 4.5, radiusKm = 250, isNotificationsGranted = true),
                "onboarding_completed",
                "threshold" to "4.5", "has_area" to "true", "radius_km" to "250", "notifications_granted" to "true",
            ),
            entry(
                AnalyticsEvent.OnboardingCompleted(threshold = 5.0, radiusKm = null, isNotificationsGranted = false),
                "onboarding_completed",
                "threshold" to "5.0", "has_area" to "false", "notifications_granted" to "false",
            ),
            entry(
                AnalyticsEvent.NotificationPermissionRequested(SetupContext.ONBOARDING),
                "notification_permission_requested",
                "context" to "onboarding",
            ),
            entry(
                AnalyticsEvent.NotificationPermissionResult(isGranted = false),
                "notification_permission_result",
                "granted" to "false",
            ),
            entry(AnalyticsEvent.AlertsToggled(isEnabled = true), "alerts_toggled", "enabled" to "true"),
            entry(
                AnalyticsEvent.AlertThresholdChanged(from = 4.5, to = 6.0, context = SetupContext.SETTINGS),
                "alert_threshold_changed",
                "from" to "4.5", "to" to "6.0", "context" to "settings",
            ),
            entry(
                AnalyticsEvent.AlertAreaSet(countryCode = "JP", radiusKm = 100, context = SetupContext.ONBOARDING),
                "alert_area_set",
                "country_code" to "JP", "radius_km" to "100", "context" to "onboarding",
            ),
            entry(
                AnalyticsEvent.AlertAreaCleared(SetupContext.SETTINGS),
                "alert_area_cleared",
                "context" to "settings",
            ),
            entry(
                AnalyticsEvent.CitySearchPerformed(countryCode = "TR", resultCount = 2),
                "city_search_performed",
                "country_code" to "TR", "result_count" to "2",
            ),
            entry(
                AnalyticsEvent.CitySearchFailed(CitySearchFailureReason.UNAVAILABLE),
                "city_search_failed",
                "reason" to "unavailable",
            ),
            entry(
                AnalyticsEvent.CurrentLocationUsed(CurrentLocationResult.LOCATION_OFF),
                "current_location_used",
                "result" to "location_off",
            ),
            entry(
                AnalyticsEvent.EarthquakeListViewed(
                    RegionFilterValue.NEAR_CITY,
                    MagnitudeFilterValue.ABOVE_THRESHOLD,
                    SortOrderValue.NEAREST_FIRST,
                ),
                "earthquake_list_viewed",
                "region_filter" to "near_city", "magnitude_filter" to "above_threshold", "sort" to "nearest_first",
            ),
            entry(
                AnalyticsEvent.EarthquakeListRefreshed(RefreshTrigger.PULL, isSuccessful = false, count = 372),
                "earthquake_list_refreshed",
                "trigger" to "pull", "result" to "failure", "count" to "372",
            ),
            entry(
                AnalyticsEvent.RegionFilterChanged(RegionFilterValue.WORLD),
                "list_filter_changed",
                "filter" to "region", "value" to "world",
            ),
            entry(
                AnalyticsEvent.MagnitudeFilterChanged(MagnitudeFilterValue.ALL),
                "list_filter_changed",
                "filter" to "magnitude", "value" to "all",
            ),
            entry(
                AnalyticsEvent.ListSortChanged(SortOrderValue.LARGEST_FIRST),
                "list_sort_changed",
                "sort" to "largest_first",
            ),
            entry(
                AnalyticsEvent.EarthquakeDetailViewed(DetailSource.LIST, magnitude = 5.3),
                "earthquake_detail_viewed",
                "source" to "list", "magnitude" to "5.3",
            ),
            entry(
                AnalyticsEvent.EarthquakeDetailViewed(DetailSource.NOTIFICATION, magnitude = null),
                "earthquake_detail_viewed",
                "source" to "notification",
            ),
            entry(
                AnalyticsEvent.DetailActionClicked(DetailAction.USGS),
                "detail_action_clicked",
                "action" to "usgs",
            ),
            entry(
                AnalyticsEvent.AlertNotificationPosted(eventId = "us7000abcd", magnitude = 6.1, batchSize = 1),
                "alert_notification_posted",
                "event_id" to "us7000abcd", "magnitude" to "6.1", "batch_size" to "1",
            ),
            entry(
                AnalyticsEvent.AlertNotificationSuppressed(SuppressionReason.PERMISSION_DENIED),
                "alert_notification_suppressed",
                "reason" to "permission_denied",
            ),
            entry(
                AnalyticsEvent.AlertNotificationSuppressed(SuppressionReason.ALERT_CHANNEL_BLOCKED),
                "alert_notification_suppressed",
                "reason" to "alert_channel_blocked",
            ),
            entry(
                AnalyticsEvent.AlertNotificationOpened(eventId = "us7000abcd", delaySeconds = 42),
                "alert_notification_opened",
                "event_id" to "us7000abcd", "delay_seconds" to "42",
            ),
            entry(
                AnalyticsEvent.AlertFeedbackGiven(eventId = "us7000abcd", isUseful = false),
                "alert_feedback_given",
                "event_id" to "us7000abcd", "useful" to "false",
            ),
            entry(
                AnalyticsEvent.BackgroundCheckCompleted(fetched = 12, matched = 2, notified = 1, durationMs = 850),
                "background_check_completed",
                "fetched" to "12", "matched" to "2", "notified" to "1", "duration_ms" to "850",
            ),
            entry(
                AnalyticsEvent.BackgroundCheckFailed(BackgroundCheckFailureReason.NETWORK),
                "background_check_failed",
                "reason" to "network",
            ),
            entry(
                AnalyticsEvent.LanguageChanged(from = null, to = AppLanguage("tr")),
                "language_changed",
                "from" to "system", "to" to "tr",
            ),
            entry(
                AnalyticsEvent.ThemeChanged(from = ThemeMode.SYSTEM, to = ThemeMode.DARK),
                "theme_changed",
                "from" to "system", "to" to "dark",
            ),
            entry(
                AnalyticsEvent.DeveloperSimulatedAlert(SimulationOutcomeValue.NOT_MATCHED, isScheduled = true),
                "developer_simulated_alert",
                "outcome" to "not_matched", "scheduled" to "true",
            ),
            entry(AnalyticsEvent.DeveloperCheckTriggered, "developer_check_triggered"),
        )

        private fun entry(event: AnalyticsEvent, name: String, vararg params: Pair<String, String>): Arguments =
            argumentSet("$name ${params.toMap()}", event, name, params.toMap())
    }
}
