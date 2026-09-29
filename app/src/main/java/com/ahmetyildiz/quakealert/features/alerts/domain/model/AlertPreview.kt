package com.ahmetyildiz.quakealert.features.alerts.domain.model

import java.time.Duration

data class AlertPreview(
    val matchCount: Int,
    val period: Duration,
)
