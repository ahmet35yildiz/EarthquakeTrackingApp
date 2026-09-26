package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake

data class EarthquakeAlert(
    val earthquake: Earthquake,
    val distanceFromCity: DistanceFromCity?,
)
