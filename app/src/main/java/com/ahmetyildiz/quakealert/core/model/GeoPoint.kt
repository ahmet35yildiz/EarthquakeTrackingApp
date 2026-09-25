package com.ahmetyildiz.quakealert.core.model

data class GeoPoint(val latitude: Double, val longitude: Double) {

    init {
        require(isValid(latitude, longitude)) { "Coordinates out of range: $latitude, $longitude" }
    }

    companion object {
        private val LATITUDE_RANGE: ClosedFloatingPointRange<Double> = -90.0..90.0
        private val LONGITUDE_RANGE: ClosedFloatingPointRange<Double> = -180.0..180.0

        fun createOrNull(latitude: Double, longitude: Double): GeoPoint? =
            if (isValid(latitude, longitude)) GeoPoint(latitude, longitude) else null

        private fun isValid(latitude: Double, longitude: Double): Boolean =
            latitude in LATITUDE_RANGE && longitude in LONGITUDE_RANGE
    }
}
