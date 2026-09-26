package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import java.time.Duration

object AlertConfig {
    val THRESHOLD_RANGE: ClosedFloatingPointRange<Double> = 2.5..8.0
    const val THRESHOLD_STEP: Double = 0.5
    const val DEFAULT_THRESHOLD: Double = AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD
    val RADIUS_OPTIONS_KM: List<Int> = listOf(50, 100, 250, 500, 1000)
    const val DEFAULT_RADIUS_KM: Int = 250
    const val CITY_SEARCH_MAX_RESULTS: Int = 10
    val CHECK_INTERVAL: Duration = Duration.ofMinutes(15)
    val MAX_EVENT_AGE: Duration = Duration.ofHours(6)
    val UPDATED_AFTER_OVERLAP: Duration = Duration.ofMinutes(10)
    val NOTIFIED_ID_RETENTION: Duration = Duration.ofDays(30)
    const val MAX_INDIVIDUAL_NOTIFICATIONS: Int = 3
    const val SUMMARY_NOTIFICATION_MAX_LINES: Int = 5
}
