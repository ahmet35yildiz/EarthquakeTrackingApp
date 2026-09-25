package com.ahmetyildiz.quakealert.features.earthquakes.data.source

import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureCollectionDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto

class FakeUsgsApi(
    var featureCollection: UsgsFeatureCollectionDto = UsgsFeatureCollectionDto(features = emptyList()),
    var features: Map<String, UsgsFeatureDto> = emptyMap(),
) : UsgsApi {

    var failure: Exception? = null

    var lastQueryParameters: Map<String, String>? = null
        private set

    var singleEventRequestCount: Int = 0
        private set

    override suspend fun getEarthquakes(parameters: Map<String, String>): UsgsFeatureCollectionDto {
        lastQueryParameters = parameters
        failure?.let { throw it }
        return featureCollection
    }

    override suspend fun getEarthquake(id: String): UsgsFeatureDto {
        singleEventRequestCount++
        failure?.let { throw it }
        return features[id] ?: throw httpNotFound()
    }
}
