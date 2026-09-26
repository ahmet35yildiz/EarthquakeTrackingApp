package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import java.time.Instant
import javax.inject.Inject

class AlertMatcher @Inject constructor() {

    fun findMatches(earthquakes: List<Earthquake>, criteria: AlertMatchCriteria): List<Earthquake> =
        earthquakes.filter { matches(it, criteria) }

    fun matches(earthquake: Earthquake, criteria: AlertMatchCriteria): Boolean {
        val settings: AlertSettings = criteria.settings
        return settings.isEnabled &&
            isAtOrAboveThreshold(earthquake, settings.magnitudeThreshold) &&
            settings.area.contains(earthquake.location) &&
            earthquake.id !in criteria.notifiedEarthquakeIds &&
            isAtOrAfterBaseline(earthquake, criteria.alertBaselineAt) &&
            isWithinMaxEventAge(earthquake, criteria.checkedAt)
    }

    private fun isAtOrAboveThreshold(earthquake: Earthquake, threshold: Double): Boolean {
        val magnitude: Double = earthquake.magnitude?.value ?: return false
        return magnitude >= threshold
    }

    private fun isAtOrAfterBaseline(earthquake: Earthquake, alertBaselineAt: Instant?): Boolean =
        alertBaselineAt != null && !earthquake.time.isBefore(alertBaselineAt)

    private fun isWithinMaxEventAge(earthquake: Earthquake, checkedAt: Instant): Boolean =
        !earthquake.time.isBefore(checkedAt - AlertConfig.MAX_EVENT_AGE)
}
