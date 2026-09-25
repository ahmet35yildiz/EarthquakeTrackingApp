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

    override suspend fun refreshCache(query: EarthquakeQuery): AppResult<Unit> =
        fetchEarthquakes(query).map { earthquakes -> dao.replaceAll(earthquakes.map(Earthquake::toEntity)) }

    override suspend fun getEarthquake(id: String): AppResult<Earthquake> {
        val cached: EarthquakeEntity? = dao.getById(id)
        if (cached != null) return AppResult.Success(cached.toEarthquake())
        return when (val result: AppResult<UsgsFeatureDto> = safeApiCall { api.getEarthquake(id) }) {
            is AppResult.Success -> result.data.toEarthquakeOrNull().toResultOrNotFound()
            is AppResult.Failure -> result
        }
    }

    override suspend fun fetchEarthquakes(query: EarthquakeQuery): AppResult<List<Earthquake>> =
        safeApiCall { api.getEarthquakes(query.toUsgsQueryParameters()) }
            .map { collection -> collection.features.mapNotNull(UsgsFeatureDto::toEarthquakeOrNull) }

    private fun Earthquake?.toResultOrNotFound(): AppResult<Earthquake> =
        if (this != null) AppResult.Success(this) else AppResult.Failure(AppError.NotFound)
}
