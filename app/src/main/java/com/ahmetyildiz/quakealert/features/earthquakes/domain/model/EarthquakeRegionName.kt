package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.PlaceDescription

object EarthquakeRegionName {

    private const val NAME_SEPARATOR: String = ", "
    private val ABBREVIATIONS: Map<String, String> = mapOf("CA" to "California", "MX" to "Mexico")

    fun of(place: String?): String? {
        if (place.isNullOrBlank()) return null
        val name: String = when (val description: PlaceDescription = PlaceDescription.parse(place)) {
            is PlaceDescription.NearPlace -> description.placeName
            is PlaceDescription.Named -> description.name
        }
        val region: String = name.substringAfterLast(NAME_SEPARATOR).trim()
        return ABBREVIATIONS[region] ?: region
    }
}
