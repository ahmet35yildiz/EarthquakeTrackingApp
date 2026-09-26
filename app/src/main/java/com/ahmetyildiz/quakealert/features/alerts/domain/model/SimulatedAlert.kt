package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake

enum class SimulationOutcome { POSTED, ALREADY_NOTIFIED, NOT_MATCHED, NOTIFICATIONS_OFF, ALERTS_OFF, NOTHING_TO_REPEAT }

data class SimulatedAlert(
    val earthquake: Earthquake?,
    val outcome: SimulationOutcome,
)
