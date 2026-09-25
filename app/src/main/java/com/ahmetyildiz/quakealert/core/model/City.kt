package com.ahmetyildiz.quakealert.core.model

data class City(
    val name: String,
    val adminArea: String?,
    val countryCode: String,
    val location: GeoPoint,
)
