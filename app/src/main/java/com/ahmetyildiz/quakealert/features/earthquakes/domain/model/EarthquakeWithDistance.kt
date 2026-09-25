package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.location.distanceKmTo
import com.ahmetyildiz.quakealert.core.model.AlertArea

data class EarthquakeWithDistance(
    val earthquake: Earthquake,
    val distanceKm: Double?,
)

fun Earthquake.withDistanceFrom(area: AlertArea): EarthquakeWithDistance =
    EarthquakeWithDistance(
        earthquake = this,
        distanceKm = when (area) {
            AlertArea.WholeWorld -> null
            is AlertArea.AroundCity -> area.city.location.distanceKmTo(location)
        },
    )
