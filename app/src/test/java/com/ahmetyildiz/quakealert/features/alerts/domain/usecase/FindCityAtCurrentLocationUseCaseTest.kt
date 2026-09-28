package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeCitySearchRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeDeviceLocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class FindCityAtCurrentLocationUseCaseTest {

    private val point = GeoPoint(38.46, 27.21)
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = point)
    private val locationRepository = FakeDeviceLocationRepository(result = AppResult.Success(point))
    private val citySearchRepository = FakeCitySearchRepository(cityAtPoint = AppResult.Success(izmir))
    private val findCity = FindCityAtCurrentLocationUseCase(locationRepository, citySearchRepository)

    @Test
    fun `names the device location in the requested locale`() = runTest {
        assertEquals(AppResult.Success(izmir), findCity(TURKISH))
        assertEquals(listOf(point to TURKISH), citySearchRepository.pointQueries)
    }

    @Test
    fun `location failure is returned without a geocoder request`() = runTest {
        locationRepository.result = AppResult.Failure(AppError.LocationDisabled)
        assertEquals(AppResult.Failure(AppError.LocationDisabled), findCity(TURKISH))
        assertTrue(citySearchRepository.pointQueries.isEmpty())
    }

    @Test
    fun `naming failure is returned`() = runTest {
        citySearchRepository.cityAtPoint = AppResult.Failure(AppError.Network)
        assertEquals(AppResult.Failure(AppError.Network), findCity(TURKISH))
    }

    private companion object {
        val TURKISH: Locale = Locale.forLanguageTag("tr")
    }
}
