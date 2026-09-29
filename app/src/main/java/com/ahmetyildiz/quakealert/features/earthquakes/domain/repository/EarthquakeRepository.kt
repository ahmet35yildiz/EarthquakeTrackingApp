package com.ahmetyildiz.quakealert.features.earthquakes.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import kotlinx.coroutines.flow.Flow

interface EarthquakeRepository {

    fun observeCachedEarthquakes(): Flow<List<Earthquake>>

    suspend fun refreshCache(query: EarthquakeQuery): AppResult<Int>

    suspend fun getCachedEarthquake(id: String): Earthquake?

    suspend fun fetchEarthquake(id: String): AppResult<Earthquake>

    suspend fun fetchEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>>

    suspend fun addToCache(earthquake: Earthquake)
}
