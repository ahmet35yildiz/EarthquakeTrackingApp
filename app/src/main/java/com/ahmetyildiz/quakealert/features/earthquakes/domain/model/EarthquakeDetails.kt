package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

data class EarthquakeDetails(
    val earthquake: Earthquake,
    val distanceFromCity: DistanceFromCity?,
    val isSavedCopyAfterFailedRefresh: Boolean = false,
)
