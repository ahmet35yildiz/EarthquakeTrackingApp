package com.ahmetyildiz.quakealert.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEarthquakeDao(initial: List<EarthquakeEntity> = emptyList()) : EarthquakeDao {

    private val earthquakes = MutableStateFlow(initial)

    val storedEarthquakes: List<EarthquakeEntity>
        get() = earthquakes.value

    override fun observeAll(): Flow<List<EarthquakeEntity>> =
        earthquakes.map { stored -> stored.sortedByDescending { it.timeEpochMs } }

    override suspend fun getById(id: String): EarthquakeEntity? = earthquakes.value.firstOrNull { it.id == id }

    override suspend fun insertAll(earthquakes: List<EarthquakeEntity>) {
        val newIds: Set<String> = earthquakes.map { it.id }.toSet()
        this.earthquakes.value = this.earthquakes.value.filterNot { it.id in newIds } + earthquakes
    }

    override suspend fun deleteAll() {
        earthquakes.value = emptyList()
    }
}
