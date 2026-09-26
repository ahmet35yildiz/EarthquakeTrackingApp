package com.ahmetyildiz.quakealert.features.alerts.domain.model

data class SimulationRequest(
    val place: String,
    val magnitude: Double,
    val distanceFromCityKm: Double,
)
