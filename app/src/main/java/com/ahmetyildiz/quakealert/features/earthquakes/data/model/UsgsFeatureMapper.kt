package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import java.time.Instant

private const val EARTHQUAKE_TYPE: String = "earthquake"
private const val REVIEWED_STATUS: String = "reviewed"
private const val TSUNAMI_FLAG_SET: Int = 1
private const val LONGITUDE_INDEX: Int = 0
private const val LATITUDE_INDEX: Int = 1
private const val DEPTH_INDEX: Int = 2

fun UsgsFeatureDto.toEarthquakeOrNull(): Earthquake? {
    if (properties.type != EARTHQUAKE_TYPE) return null
    val coordinates: List<Double> = geometry?.coordinates ?: return null
    val location: GeoPoint = GeoPoint.createOrNull(
        latitude = coordinates.getOrNull(LATITUDE_INDEX) ?: return null,
        longitude = coordinates.getOrNull(LONGITUDE_INDEX) ?: return null,
    ) ?: return null
    return Earthquake(
        id = id,
        magnitude = properties.mag?.let { Magnitude(value = it, type = properties.magType) },
        place = properties.place,
        time = Instant.ofEpochMilli(properties.time),
        location = location,
        depthKm = coordinates.getOrNull(DEPTH_INDEX) ?: return null,
        detailUrl = properties.url,
        isReviewed = properties.status == REVIEWED_STATUS,
        hasTsunamiFlag = properties.tsunami == TSUNAMI_FLAG_SET,
        feltReportCount = properties.felt,
    )
}
