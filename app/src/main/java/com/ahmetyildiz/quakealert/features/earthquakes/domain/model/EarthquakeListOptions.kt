package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

data class EarthquakeListOptions(
    val region: RegionFilter = RegionFilter.WORLD,
    val magnitude: MagnitudeFilter = MagnitudeFilter.ALL,
    val sortOrder: EarthquakeSortOrder = EarthquakeSortOrder.NEWEST_FIRST,
)
