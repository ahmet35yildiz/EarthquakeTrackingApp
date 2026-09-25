package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

data class EarthquakeFilters(
    val region: RegionFilter = RegionFilter.WORLD,
    val magnitude: MagnitudeFilter = MagnitudeFilter.ALL,
)
