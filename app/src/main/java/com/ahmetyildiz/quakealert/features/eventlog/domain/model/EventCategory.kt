package com.ahmetyildiz.quakealert.features.eventlog.domain.model

enum class EventCategory {
    ALERT,
    BACKGROUND,
    SETTINGS,
    USAGE;

    companion object {
        private val ALERT_PREFIXES: List<String> = listOf("alert_notification_", "developer_simulated_alert")
        private val BACKGROUND_PREFIXES: List<String> =
            listOf("background_check_", "earthquake_list_refreshed", "developer_check_triggered")
        private val SETTINGS_PREFIXES: List<String> = listOf(
            "onboarding_",
            "notification_permission_",
            "alerts_toggled",
            "alert_threshold_",
            "alert_area_",
            "city_search_",
            "language_changed",
            "theme_changed",
        )

        fun fromEventName(name: String): EventCategory = when {
            ALERT_PREFIXES.any(name::startsWith) -> ALERT
            BACKGROUND_PREFIXES.any(name::startsWith) -> BACKGROUND
            SETTINGS_PREFIXES.any(name::startsWith) -> SETTINGS
            else -> USAGE
        }
    }
}
