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

    val fetchedIds: MutableList<String> = mutableListOf()

    override fun observeCachedEarthquakes(): Flow<List<Earthquake>> = cachedEarthquakes

    override suspend fun refreshCache(query: EarthquakeQuery): AppResult<Int> {
        receivedQueries += query
        failure?.let { return AppResult.Failure(it) }
        cachedEarthquakes.value = remoteEarthquakes
        return AppResult.Success(remoteEarthquakes.size)
    }

    override suspend fun getCachedEarthquake(id: String): Earthquake? = cachedEarthquakes.value.firstOrNull { it.id == id }

    override suspend fun fetchEarthquake(id: String): AppResult<Earthquake> {
        fetchedIds += id
        failure?.let { return AppResult.Failure(it) }
        val earthquake: Earthquake = remoteEarthquakes.firstOrNull { it.id == id }
            ?: return AppResult.Failure(AppError.NotFound)
        updateCachedCopies(listOf(earthquake))
        return AppResult.Success(earthquake)
    }

    override suspend fun fetchEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>> {
        receivedQueries += query
        failure?.let { return AppResult.Failure(it) }
        updateCachedCopies(remoteEarthquakes)
        return AppResult.Success(remoteEarthquakes)
    }

    override suspend fun addToCache(earthquake: Earthquake) {
        cachedEarthquakes.value = cachedEarthquakes.value.filterNot { it.id == earthquake.id } + earthquake
    }

    private fun updateCachedCopies(earthquakes: List<Earthquake>) {
        val updates: Map<String, Earthquake> = earthquakes.associateBy(Earthquake::id)
        cachedEarthquakes.value = cachedEarthquakes.value.map { updates[it.id] ?: it }
    }
}
