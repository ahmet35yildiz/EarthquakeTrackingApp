package com.ahmetyildiz.quakealert.features.earthquakes.data.source

import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureCollectionDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface UsgsApi {

    @GET("fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time")
    suspend fun getEarthquakes(@QueryMap parameters: Map<String, String>): UsgsFeatureCollectionDto

    @GET("fdsnws/event/1/query?format=geojson")
    suspend fun getEarthquake(@Query("eventid") id: String): UsgsFeatureDto
}
