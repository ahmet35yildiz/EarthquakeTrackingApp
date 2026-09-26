package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

data class DistanceFromCity(
    val cityName: String,
    val distanceKm: Double,
    val alertRadiusKm: Int,
    val isWithinAlertArea: Boolean,
)
