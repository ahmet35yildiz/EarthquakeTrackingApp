package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import java.time.Instant

data class AlertSettingsStatus(
    val settings: AlertSettings,
    val lastCheckedAt: Instant?,
)
