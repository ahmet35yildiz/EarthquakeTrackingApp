package com.ahmetyildiz.quakealert.testing

import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsGeometryDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsPropertiesDto
import java.time.Instant

object UsgsTestData {

    private const val EVENT_PAGE_URL: String = "https://earthquake.usgs.gov/earthquakes/eventpage/"
    private const val LONGITUDE: Double = 26.84
    private const val LATITUDE: Double = 38.18
    private const val DEPTH_KM: Double = 10.0

    fun earthquake(id: String, magnitude: Double, place: String, time: Instant): UsgsFeatureDto =
        UsgsFeatureDto(
            id = id,
            properties = UsgsPropertiesDto(
                mag = magnitude,
                magType = "mww",
                place = place,
                time = time.toEpochMilli(),
                url = EVENT_PAGE_URL + id,
                status = "reviewed",
                type = "earthquake",
            ),
            geometry = UsgsGeometryDto(coordinates = listOf(LONGITUDE, LATITUDE, DEPTH_KM)),
        )
}
