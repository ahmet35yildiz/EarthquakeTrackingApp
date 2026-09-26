package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import java.time.Instant

data class AlertMatchCriteria(
    val settings: AlertSettings,
    val alertBaselineAt: Instant?,
    val notifiedEarthquakeIds: Set<String>,
    val checkedAt: Instant,
)
