package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsgsFeatureDto(
    val id: String,
    val properties: UsgsPropertiesDto,
    val geometry: UsgsGeometryDto? = null,
)
