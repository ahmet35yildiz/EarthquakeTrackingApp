package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.location.distanceKmTo
import com.ahmetyildiz.quakealert.core.model.AlertArea

data class DistanceFromCity(
    val cityName: String,
    val distanceKm: Double,
    val alertRadiusKm: Int,
    val isWithinAlertArea: Boolean,
)

fun AlertArea.distanceFromCityOrNull(earthquake: Earthquake): DistanceFromCity? {
    if (this !is AlertArea.AroundCity) return null
    return DistanceFromCity(
        cityName = city.name,
        distanceKm = city.location.distanceKmTo(earthquake.location),
        alertRadiusKm = radiusKm,
        isWithinAlertArea = contains(earthquake.location),
    )
}
