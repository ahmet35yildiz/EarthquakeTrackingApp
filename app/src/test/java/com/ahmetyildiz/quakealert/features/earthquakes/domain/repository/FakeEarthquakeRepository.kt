package com.ahmetyildiz.quakealert.features.earthquakes.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEarthquakeRepository(cached: List<Earthquake> = emptyList()) : EarthquakeRepository {

    val cachedEarthquakes = MutableStateFlow(cached)

    var remoteEarthquakes: List<Earthquake> = emptyList()

    var failure: AppError? = null

    val receivedQueries: MutableList<EarthquakeQuery> = mutableListOf()

    override fun observeCachedEarthquakes(): Flow<List<Earthquake>> = cachedEarthquakes

    override suspend fun refreshCache(query: EarthquakeQuery): AppResult<Int> {
        receivedQueries += query
        failure?.let { return AppResult.Failure(it) }
        cachedEarthquakes.value = remoteEarthquakes
        return AppResult.Success(remoteEarthquakes.size)
    }

    override suspend fun getEarthquake(id: String): AppResult<Earthquake> {
        failure?.let { return AppResult.Failure(it) }
        val earthquake: Earthquake = (cachedEarthquakes.value + remoteEarthquakes).firstOrNull { it.id == id }
            ?: return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(earthquake)
    }

    override suspend fun fetchEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>> {
        receivedQueries += query
        failure?.let { return AppResult.Failure(it) }
        return AppResult.Success(remoteEarthquakes)
    }
}
