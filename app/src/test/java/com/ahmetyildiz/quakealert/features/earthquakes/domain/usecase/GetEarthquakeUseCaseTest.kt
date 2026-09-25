package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetEarthquakeUseCaseTest {

    private val cachedEarthquake: Earthquake = earthquake(id = "us1", location = GeoPoint(0.0, 1.0))
    private val earthquakeRepository = FakeEarthquakeRepository(cached = listOf(cachedEarthquake))
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val useCase = GetEarthquakeUseCase(earthquakeRepository, preferencesRepository)

    @Test
    fun `earthquake comes without distance when no area is set`() = runTest {
        val result: AppResult<EarthquakeWithDistance> = useCase("us1")
        assertEquals(AppResult.Success(EarthquakeWithDistance(cachedEarthquake, distanceKm = null)), result)
    }

    @Test
    fun `earthquake comes with the distance to the user's city`() = runTest {
        val area = AlertArea.AroundCity(City("Center", null, "XX", GeoPoint(0.0, 0.0)), radiusKm = 50)
        preferencesRepository.update {
            it.copy(alertSettings = AlertSettings(isEnabled = true, magnitudeThreshold = 4.5, area = area))
        }
        val result = useCase("us1") as AppResult.Success
        assertEquals(111.195, requireNotNull(result.data.distanceKm), 0.001)
    }

    @Test
    fun `missing earthquake is reported as not found`() = runTest {
        assertEquals(AppResult.Failure(AppError.NotFound), useCase("unknown"))
    }
}
