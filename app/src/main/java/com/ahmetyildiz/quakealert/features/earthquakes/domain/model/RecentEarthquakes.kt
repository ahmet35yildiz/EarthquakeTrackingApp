package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.AlertArea
import java.time.Instant

data class RecentEarthquakes(
    val earthquakes: List<EarthquakeWithDistance>,
    val appliedFilters: EarthquakeFilters,
    val cachedCount: Int,
    val area: AlertArea,
    val magnitudeThreshold: Double,
    val lastRefreshedAt: Instant?,
)
