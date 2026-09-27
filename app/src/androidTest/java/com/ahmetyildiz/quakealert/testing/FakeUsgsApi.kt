package com.ahmetyildiz.quakealert.testing

import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureCollectionDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.source.UsgsApi
import java.io.IOException

class FakeUsgsApi : UsgsApi {

    var features: List<UsgsFeatureDto> = emptyList()

    var failure: Exception? = null

    var listRequestCount: Int = 0
        private set

    override suspend fun getEarthquakes(parameters: Map<String, String>): UsgsFeatureCollectionDto {
        listRequestCount++
        failure?.let { throw it }
        return UsgsFeatureCollectionDto(features = features)
    }

    override suspend fun getEarthquake(id: String): UsgsFeatureDto {
        failure?.let { throw it }
        return features.firstOrNull { it.id == id } ?: throw IOException("Unknown test event $id")
    }
}
