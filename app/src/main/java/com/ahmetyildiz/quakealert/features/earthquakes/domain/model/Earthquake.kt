package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import java.time.Instant

data class Earthquake(
    val id: String,
    val magnitude: Magnitude?,
    val place: String?,
    val time: Instant,
    val location: GeoPoint,
    val depthKm: Double,
    val detailUrl: String,
    val isReviewed: Boolean,
    val hasTsunamiFlag: Boolean,
    val feltReportCount: Int?,
)
