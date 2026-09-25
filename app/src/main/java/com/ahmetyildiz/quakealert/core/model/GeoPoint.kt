package com.ahmetyildiz.quakealert.core.model

/** A position on Earth in decimal degrees (WGS 84). */
data class GeoPoint(val latitude: Double, val longitude: Double) {

    init {
        require(latitude in LATITUDE_RANGE) { "Latitude out of range: $latitude" }
        require(longitude in LONGITUDE_RANGE) { "Longitude out of range: $longitude" }
    }

    private companion object {
        val LATITUDE_RANGE: ClosedFloatingPointRange<Double> = -90.0..90.0
        val LONGITUDE_RANGE: ClosedFloatingPointRange<Double> = -180.0..180.0
    }
}
