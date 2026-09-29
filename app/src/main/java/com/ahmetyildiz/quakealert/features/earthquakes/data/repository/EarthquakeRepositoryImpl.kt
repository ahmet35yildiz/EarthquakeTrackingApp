package com.ahmetyildiz.quakealert.features.earthquakes.data.repository

import com.ahmetyildiz.quakealert.core.database.EarthquakeDao
import com.ahmetyildiz.quakealert.core.database.EarthquakeEntity
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.error.map
import com.ahmetyildiz.quakealert.core.network.safeApiCall
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toEarthquake
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toEarthquakeOrNull
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toEntity
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toUsgsQueryParameters
import com.ahmetyildiz.quakealert.features.earthquakes.data.source.UsgsApi
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EarthquakeRepositoryImpl @Inject constructor(
    private val api: UsgsApi,
    private val dao: EarthquakeDao,
) : EarthquakeRepository {

    override fun observeCachedEarthquakes(): Flow<List<Earthquake>> =
        dao.observeAll().map { entities -> entities.map(EarthquakeEntity::toEarthquake) }

    override suspend fun refreshCache(query: EarthquakeQuery): AppResult<Int> =
        requestEarthquakes(query).map { earthquakes ->
            dao.replaceAll(earthquakes.map(Earthquake::toEntity))
            earthquakes.size
        }

    override suspend fun getCachedEarthquake(id: String): Earthquake? = dao.getById(id)?.toEarthquake()

    override suspend fun fetchEarthquake(id: String): AppResult<Earthquake> {
        val response: AppResult<UsgsFeatureDto> = safeApiCall { api.getEarthquake(id) }
        val result: AppResult<Earthquake> = when (response) {
            is AppResult.Success -> response.data.toEarthquakeOrNull().toResultOrNotFound()
            is AppResult.Failure -> response
        }
        if (result is AppResult.Success) updateCachedCopies(listOf(result.data))
        return result
    }

    override suspend fun fetchEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>> {
        val result: AppResult<List<Earthquake>> = requestEarthquakes(query)
        if (result is AppResult.Success) updateCachedCopies(result.data)
        return result
    }

    override suspend fun addToCache(earthquake: Earthquake) {
        dao.insertAll(listOf(earthquake.toEntity()))
    }

    private suspend fun requestEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>> =
        safeApiCall { api.getEarthquakes(query.toUsgsQueryParameters()) }
            .map { collection -> collection.features.mapNotNull(UsgsFeatureDto::toEarthquakeOrNull) }

    private suspend fun updateCachedCopies(earthquakes: List<Earthquake>) {
        dao.updateExisting(earthquakes.map(Earthquake::toEntity))
    }

    private fun Earthquake?.toResultOrNotFound(): AppResult<Earthquake> =
        if (this != null) AppResult.Success(this) else AppResult.Failure(AppError.NotFound)
}
