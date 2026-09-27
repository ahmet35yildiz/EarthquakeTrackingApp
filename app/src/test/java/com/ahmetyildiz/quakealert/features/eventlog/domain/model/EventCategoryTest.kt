package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EventCategoryTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        "alert_notification_posted, ALERT",
        "alert_notification_suppressed, ALERT",
        "alert_notification_opened, ALERT",
        "developer_simulated_alert, ALERT",
        "background_check_completed, BACKGROUND",
        "background_check_failed, BACKGROUND",
        "earthquake_list_refreshed, BACKGROUND",
        "developer_check_triggered, BACKGROUND",
        "onboarding_started, SETTINGS",
        "onboarding_step_viewed, SETTINGS",
        "onboarding_completed, SETTINGS",
        "notification_permission_requested, SETTINGS",
        "notification_permission_result, SETTINGS",
        "alerts_toggled, SETTINGS",
        "alert_threshold_changed, SETTINGS",
        "alert_area_set, SETTINGS",
        "alert_area_cleared, SETTINGS",
        "city_search_performed, SETTINGS",
        "city_search_failed, SETTINGS",
        "language_changed, SETTINGS",
        "app_opened, USAGE",
        "earthquake_list_viewed, USAGE",
        "list_filter_changed, USAGE",
        "list_sort_changed, USAGE",
        "earthquake_detail_viewed, USAGE",
        "detail_action_clicked, USAGE",
        "unknown_future_event, USAGE",
    )
    fun `event name maps to its category`(name: String, expected: EventCategory) {
        assertEquals(expected, EventCategory.fromEventName(name))
    }
}
