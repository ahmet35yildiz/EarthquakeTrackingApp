package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsgsGeometryDto(val coordinates: List<Double>)
