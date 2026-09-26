package com.ahmetyildiz.quakealert.features.alerts.data.model

import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint

fun GeocodedAddress.toCityOrNull(): City? {
    val name: String = findCityName() ?: return null
    val location: GeoPoint = GeoPoint.createOrNull(latitude, longitude) ?: return null
    val code: String = countryCode?.takeIf { it.isNotBlank() } ?: return null
    return City(
        name = name,
        adminArea = adminArea?.takeIf { it.isNotBlank() && it != name },
        countryCode = code.uppercase(),
        location = location,
    )
}

private fun GeocodedAddress.findCityName(): String? =
    listOf(locality, subAdminArea, adminArea, featureName).firstOrNull { !it.isNullOrBlank() }
