package com.ahmetyildiz.quakealert.features.earthquakes.data.repository

import com.ahmetyildiz.quakealert.core.database.EarthquakeEntity
import com.ahmetyildiz.quakealert.core.database.FakeEarthquakeDao
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureCollectionDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFeatureDto
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.UsgsFixtures
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toEarthquakeOrNull
import com.ahmetyildiz.quakealert.features.earthquakes.data.model.toEntity
import com.ahmetyildiz.quakealert.features.earthquakes.data.source.FakeUsgsApi
import com.ahmetyildiz.quakealert.features.earthquakes.data.source.httpError
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Instant

class EarthquakeRepositoryImplTest {

    private val collection: UsgsFeatureCollectionDto = UsgsFixtures.featureCollection()
    private val singleFeature: UsgsFeatureDto = UsgsFixtures.feature()
    private val listedEarthquakes: List<Earthquake> = collection.features.mapNotNull { it.toEarthquakeOrNull() }
    private val singleEarthquake: Earthquake = requireNotNull(singleFeature.toEarthquakeOrNull())
    private val query = EarthquakeQuery(startTime = Instant.parse("2026-09-18T12:00:00Z"), minMagnitude = 2.5)

    private val api = FakeUsgsApi(featureCollection = collection, features = mapOf(singleFeature.id to singleFeature))
    private val dao = FakeEarthquakeDao()
    private val repository = EarthquakeRepositoryImpl(api = api, dao = dao)

    @Test
    fun `refresh replaces the whole cache and reports how many earthquakes it stored`() = runTest {
        dao.insertAll(listOf(singleEarthquake.toEntity()))
        val result: AppResult<Int> = repository.refreshCache(query)
        assertEquals(AppResult.Success(listedEarthquakes.size), result)
        assertEquals(listedEarthquakes, repository.observeCachedEarthquakes().first())
    }

    @Test
    fun `refresh sends the query as USGS parameters`() = runTest {
        repository.refreshCache(query)
        assertEquals(mapOf("starttime" to "2026-09-18T12:00:00Z", "minmagnitude" to "2.5"), api.lastQueryParameters)
    }

    @Test
    fun `failed refresh keeps the cache and reports a network error`() = runTest {
        val cached: List<EarthquakeEntity> = listOf(singleEarthquake.toEntity())
        dao.insertAll(cached)
        api.failure = IOException("offline")
        val result: AppResult<Int> = repository.refreshCache(query)
        assertEquals(AppResult.Failure(AppError.Network), result)
        assertEquals(cached, dao.storedEarthquakes)
    }

    @Test
    fun `failed refresh reports the server status`() = runTest {
        api.failure = httpError(code = 503)
        assertEquals(AppResult.Failure(AppError.Server(503)), repository.refreshCache(query))
    }

    @Test
    fun `cached earthquakes are observed newest first`() = runTest {
        dao.insertAll(listedEarthquakes.reversed().map(Earthquake::toEntity))
        val observed: List<Earthquake> = repository.observeCachedEarthquakes().first()
        assertEquals(listedEarthquakes.sortedByDescending { it.time }, observed)
    }

    @Test
    fun `cached earthquake is read without a network request`() = runTest {
        dao.insertAll(listOf(singleEarthquake.toEntity()))
        assertEquals(singleEarthquake, repository.getCachedEarthquake(singleEarthquake.id))
        assertEquals(0, api.singleEventRequestCount)
    }

    @Test
    fun `earthquake that is not cached reads as nothing`() = runTest {
        assertEquals(null, repository.getCachedEarthquake(singleEarthquake.id))
    }

    @Test
    fun `fetched earthquake that is not cached is returned without being added to the cache`() = runTest {
        val result: AppResult<Earthquake> = repository.fetchEarthquake(singleEarthquake.id)
        assertEquals(AppResult.Success(singleEarthquake), result)
        assertEquals(emptyList<EarthquakeEntity>(), dao.storedEarthquakes)
    }

    @Test
    fun `fetched earthquake replaces its cached copy`() = runTest {
        val outdated: Earthquake = singleEarthquake.copy(magnitude = singleEarthquake.magnitude?.copy(value = 1.0))
        dao.insertAll(listOf(outdated.toEntity()))
        repository.fetchEarthquake(singleEarthquake.id)
        assertEquals(singleEarthquake, repository.getCachedEarthquake(singleEarthquake.id))
    }

    @Test
    fun `unknown earthquake id is reported as not found`() = runTest {
        assertEquals(AppResult.Failure(AppError.NotFound), repository.fetchEarthquake("xx00000000"))
    }

    @Test
    fun `event id that is not an earthquake is reported as not found`() = runTest {
        val quarryBlast: UsgsFeatureDto = collection.features[2]
        api.features = mapOf(quarryBlast.id to quarryBlast)
        assertEquals(AppResult.Failure(AppError.NotFound), repository.fetchEarthquake(quarryBlast.id))
    }

    @Test
    fun `fetching one earthquake offline reports a network error and keeps the cached copy`() = runTest {
        dao.insertAll(listOf(singleEarthquake.toEntity()))
        api.failure = IOException("offline")
        assertEquals(AppResult.Failure(AppError.Network), repository.fetchEarthquake(singleEarthquake.id))
        assertEquals(singleEarthquake, repository.getCachedEarthquake(singleEarthquake.id))
    }

    @Test
    fun `fetch returns earthquakes without adding them to the cache`() = runTest {
        val result: AppResult<List<Earthquake>> = repository.fetchEarthquakes(query.copy(updatedAfter = Instant.EPOCH))
        assertEquals(AppResult.Success(listedEarthquakes), result)
        assertEquals(emptyList<EarthquakeEntity>(), dao.storedEarthquakes)
        assertEquals("1970-01-01T00:00:00Z", api.lastQueryParameters?.get("updatedafter"))
    }

    @Test
    fun `fetch replaces the cached copies of revised earthquakes and keeps the rest of the cache`() = runTest {
        val revised: Earthquake = listedEarthquakes.first()
        val outdated: Earthquake = revised.copy(magnitude = revised.magnitude?.copy(value = 1.0))
        dao.insertAll(listOf(outdated.toEntity(), singleEarthquake.toEntity()))
        repository.fetchEarthquakes(query)
        assertEquals(revised, repository.getCachedEarthquake(revised.id))
        assertEquals(singleEarthquake, repository.getCachedEarthquake(singleEarthquake.id))
        assertEquals(2, dao.storedEarthquakes.size)
    }
}
