package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.AlertArea
import java.time.Instant

data class EarthquakeQuery(
    val startTime: Instant,
    val minMagnitude: Double,
    val area: AlertArea = AlertArea.WholeWorld,
    val updatedAfter: Instant? = null,
)
