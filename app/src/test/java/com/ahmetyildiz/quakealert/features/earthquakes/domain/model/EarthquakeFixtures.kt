package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import java.time.Instant

object EarthquakeFixtures {

    const val KM_PER_DEGREE_ON_EQUATOR: Double = 2 * Math.PI * 6371.0 / 360

    fun earthquake(
        id: String,
        magnitude: Double? = 4.5,
        location: GeoPoint = GeoPoint(0.0, 0.0),
        time: Instant = Instant.parse("2026-09-25T12:00:00Z"),
        place: String? = "Place of $id",
    ): Earthquake =
        Earthquake(
            id = id,
            magnitude = magnitude?.let { Magnitude(value = it, type = "mb") },
            place = place,
            time = time,
            location = location,
            depthKm = 10.0,
            detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/$id",
            isReviewed = true,
            hasTsunamiFlag = false,
            feltReportCount = null,
        )

    fun pointOnEquatorAt(distanceKm: Double): GeoPoint = GeoPoint(0.0, distanceKm / KM_PER_DEGREE_ON_EQUATOR)
}
