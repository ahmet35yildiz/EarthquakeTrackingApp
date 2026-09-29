package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetEarthquakeUseCaseTest {

    private val cachedEarthquake: Earthquake = earthquake(id = "us1", location = GeoPoint(0.0, 1.0))
    private val earthquakeRepository = FakeEarthquakeRepository(cached = listOf(cachedEarthquake))
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val useCase = GetEarthquakeUseCase(earthquakeRepository, preferencesRepository)

    @Test
    fun `earthquake comes without a distance when no area is set`() = runTest {
        val result: AppResult<EarthquakeDetails> = useCase("us1")
        assertEquals(AppResult.Success(EarthquakeDetails(cachedEarthquake, distanceFromCity = null)), result)
    }

    @Test
    fun `earthquake comes with the distance to the user's city and the alert radius`() = runTest {
        givenAreaAroundCenter(radiusKm = 250)
        val distance: DistanceFromCity = requireNotNull(detailsOf("us1").distanceFromCity)
        assertEquals("Center", distance.cityName)
        assertEquals(250, distance.alertRadiusKm)
        assertEquals(111.195, distance.distanceKm, 0.001)
    }

    @Test
    fun `earthquake inside the radius is within the alert area`() = runTest {
        givenAreaAroundCenter(radiusKm = 250)
        assertTrue(requireNotNull(detailsOf("us1").distanceFromCity).isWithinAlertArea)
    }

    @Test
    fun `earthquake beyond the radius is outside the alert area`() = runTest {
        givenAreaAroundCenter(radiusKm = 50)
        assertFalse(requireNotNull(detailsOf("us1").distanceFromCity).isWithinAlertArea)
    }

    @Test
    fun `cached earthquake is read without asking USGS`() = runTest {
        useCase("us1")
        assertTrue(earthquakeRepository.fetchedIds.isEmpty())
    }

    @Test
    fun `earthquake that is not cached is fetched from USGS`() = runTest {
        val remote: Earthquake = earthquake(id = "us2")
        earthquakeRepository.remoteEarthquakes = listOf(remote)
        assertEquals(AppResult.Success(EarthquakeDetails(remote, distanceFromCity = null)), useCase("us2"))
    }

    @Test
    fun `missing earthquake is reported as not found`() = runTest {
        assertEquals(AppResult.Failure(AppError.NotFound), useCase("unknown"))
    }

    @Test
    fun `network failure is passed on when nothing is cached`() = runTest {
        earthquakeRepository.failure = AppError.Network
        assertEquals(AppResult.Failure(AppError.Network), useCase("unknown"))
    }

    @Test
    fun `revalidation shows the revised earthquake instead of the cached one`() = runTest {
        val revised: Earthquake = cachedEarthquake.copy(magnitude = Magnitude(value = 5.0, type = "mww"))
        earthquakeRepository.remoteEarthquakes = listOf(revised)
        val details: EarthquakeDetails = (useCase("us1", shouldRevalidate = true) as AppResult.Success).data
        assertEquals(revised, details.earthquake)
        assertFalse(details.isSavedCopyAfterFailedRefresh)
        assertEquals(listOf(revised), earthquakeRepository.cachedEarthquakes.value)
    }

    @Test
    fun `failed revalidation falls back to the cached copy and says so`() = runTest {
        earthquakeRepository.failure = AppError.Network
        val details: EarthquakeDetails = (useCase("us1", shouldRevalidate = true) as AppResult.Success).data
        assertEquals(cachedEarthquake, details.earthquake)
        assertTrue(details.isSavedCopyAfterFailedRefresh)
    }

    @Test
    fun `revalidation of an earthquake USGS does not know keeps the cached copy without a notice`() = runTest {
        val details: EarthquakeDetails = (useCase("us1", shouldRevalidate = true) as AppResult.Success).data
        assertEquals(cachedEarthquake, details.earthquake)
        assertFalse(details.isSavedCopyAfterFailedRefresh)
    }

    @Test
    fun `failed revalidation without a cached copy reports the error`() = runTest {
        earthquakeRepository.failure = AppError.Network
        assertEquals(AppResult.Failure(AppError.Network), useCase("unknown", shouldRevalidate = true))
    }

    private suspend fun detailsOf(id: String): EarthquakeDetails = (useCase(id) as AppResult.Success).data

    private fun givenAreaAroundCenter(radiusKm: Int) {
        val area = AlertArea.AroundCity(City("Center", null, "XX", GeoPoint(0.0, 0.0)), radiusKm = radiusKm)
        preferencesRepository.update {
            it.copy(alertSettings = AlertSettings(isEnabled = true, magnitudeThreshold = 4.5, area = area))
        }
    }
}
