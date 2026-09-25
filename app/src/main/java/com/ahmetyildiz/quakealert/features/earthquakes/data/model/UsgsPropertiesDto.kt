package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsgsPropertiesDto(
    val mag: Double? = null,
    val magType: String? = null,
    val place: String? = null,
    val time: Long,
    val url: String,
    val status: String,
    val tsunami: Int = 0,
    val felt: Int? = null,
    val type: String,
)
