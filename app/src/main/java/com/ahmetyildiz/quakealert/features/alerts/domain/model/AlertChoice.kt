package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.core.model.AlertArea

data class AlertChoice(
    val magnitudeThreshold: Double,
    val area: AlertArea,
)
