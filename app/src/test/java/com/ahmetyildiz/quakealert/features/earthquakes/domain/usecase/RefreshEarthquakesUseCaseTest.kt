package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class RefreshEarthquakesUseCaseTest {

    private val clock = FakeClock(Instant.parse("2026-09-25T12:00:00Z"))
    private val earthquakeRepository = FakeEarthquakeRepository()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val useCase = RefreshEarthquakesUseCase(earthquakeRepository, preferencesRepository, clock)

    @Test
    fun `refresh asks for the last 7 days of M2_5+ earthquakes worldwide`() = runTest {
        useCase()
        val expected = EarthquakeQuery(startTime = Instant.parse("2026-09-18T12:00:00Z"), minMagnitude = 2.5)
        assertEquals(listOf(expected), earthquakeRepository.receivedQueries)
    }

    @Test
    fun `refresh stays worldwide when the user has an area`() = runTest {
        val area = AlertArea.AroundCity(City("Tokyo", null, "JP", GeoPoint(35.68, 139.69)), radiusKm = 100)
        preferencesRepository.update {
            it.copy(alertSettings = AlertSettings(isEnabled = true, magnitudeThreshold = 6.0, area = area))
        }
        useCase()
        assertEquals(AlertArea.WholeWorld, earthquakeRepository.receivedQueries.single().area)
    }

    @Test
    fun `successful refresh records when it happened`() = runTest {
        earthquakeRepository.remoteEarthquakes = listOf(earthquake(id = "a"))
        val result: AppResult<Int> = useCase()
        assertEquals(AppResult.Success(1), result)
        assertEquals(clock.now(), preferencesRepository.userPreferences.value.lastRefreshedAt)
    }

    @Test
    fun `failed refresh keeps the previous refresh time`() = runTest {
        earthquakeRepository.failure = AppError.Network
        val result: AppResult<Int> = useCase()
        assertEquals(AppResult.Failure(AppError.Network), result)
        assertNull(preferencesRepository.userPreferences.value.lastRefreshedAt)
    }

    @Test
    fun `failed refresh does not overwrite an earlier successful time`() = runTest {
        val earlier: Instant = Instant.parse("2026-09-25T11:00:00Z")
        preferencesRepository.update { UserPreferences.DEFAULT.copy(lastRefreshedAt = earlier) }
        earthquakeRepository.failure = AppError.Server(503)
        useCase()
        assertEquals(earlier, preferencesRepository.userPreferences.value.lastRefreshedAt)
    }
}
