package com.ahmetyildiz.quakealert.features.alerts.data.model

data class GeocodedAddress(
    val locality: String?,
    val subAdminArea: String?,
    val adminArea: String?,
    val featureName: String?,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
) {

    val isCountryLevel: Boolean
        get() = listOf(locality, subAdminArea, adminArea).all { it.isNullOrBlank() }
}
