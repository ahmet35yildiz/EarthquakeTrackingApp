package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UsgsFeatureCollectionDto(val features: List<UsgsFeatureDto>)
