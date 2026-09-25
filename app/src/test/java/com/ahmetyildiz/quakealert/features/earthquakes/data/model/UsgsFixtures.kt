package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.di.NetworkModule
import kotlinx.serialization.json.Json

object UsgsFixtures {

    private val json: Json = NetworkModule.provideJson()

    fun featureCollection(): UsgsFeatureCollectionDto = json.decodeFromString(read("usgs/feature_collection.json"))

    fun feature(): UsgsFeatureDto = json.decodeFromString(read("usgs/feature.json"))

    private fun read(path: String): String =
        requireNotNull(javaClass.classLoader?.getResource(path)) { "Missing test resource $path" }.readText()
}
