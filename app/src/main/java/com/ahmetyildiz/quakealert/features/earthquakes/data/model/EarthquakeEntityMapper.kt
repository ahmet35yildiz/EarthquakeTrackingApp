package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.database.EarthquakeEntity
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import java.time.Instant

fun Earthquake.toEntity(): EarthquakeEntity =
    EarthquakeEntity(
        id = id,
        magnitude = magnitude?.value,
        magnitudeType = magnitude?.type,
        place = place,
        timeEpochMs = time.toEpochMilli(),
        latitude = location.latitude,
        longitude = location.longitude,
        depthKm = depthKm,
        detailUrl = detailUrl,
        isReviewed = isReviewed,
        hasTsunamiFlag = hasTsunamiFlag,
        feltReportCount = feltReportCount,
    )

fun EarthquakeEntity.toEarthquake(): Earthquake =
    Earthquake(
        id = id,
        magnitude = magnitude?.let { Magnitude(value = it, type = magnitudeType) },
        place = place,
        time = Instant.ofEpochMilli(timeEpochMs),
        location = GeoPoint(latitude = latitude, longitude = longitude),
        depthKm = depthKm,
        detailUrl = detailUrl,
        isReviewed = isReviewed,
        hasTsunamiFlag = hasTsunamiFlag,
        feltReportCount = feltReportCount,
    )
