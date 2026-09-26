package com.ahmetyildiz.quakealert.features.alerts.data.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddress
import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddressFixtures.address
import com.ahmetyildiz.quakealert.features.alerts.data.source.FakeCityGeocoder
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Locale

class CitySearchRepositoryImplTest {

    private val geocoder = FakeCityGeocoder()
    private val repository = CitySearchRepositoryImpl(geocoder)
    private val query = CitySearchQuery(name = "Izmir", country = Country("TR", "Türkiye"), locale = TURKISH)

    @Test
    fun `availability follows the geocoder`() {
        assertTrue(repository.isCitySearchAvailable())
        geocoder.isGeocoderPresent = false
        assertFalse(repository.isCitySearchAvailable())
    }

    @Test
    fun `missing geocoder fails without a request`() = runTest {
        geocoder.isGeocoderPresent = false
        assertEquals(AppResult.Failure(AppError.GeocoderUnavailable), repository.searchCities(query))
        assertTrue(geocoder.requests.isEmpty())
    }

    @Test
    fun `searches the name within the country name in the requested locale`() = runTest {
        repository.searchCities(query)
        assertEquals(listOf("Izmir, Türkiye" to TURKISH), geocoder.requests)
    }

    @Test
    fun `keeps only cities in the selected country`() = runTest {
        geocoder.addresses = listOf(
            address(locality = "Izmir", countryCode = "TR"),
            address(locality = "Smyrna", adminArea = "Georgia", countryCode = "US"),
        )
        assertEquals(listOf("Izmir"), searchCityNames())
    }

    @Test
    fun `removes duplicates with the same name and admin area`() = runTest {
        geocoder.addresses = listOf(
            address(locality = "Izmir", latitude = 38.42),
            address(locality = "Izmir", latitude = 38.43),
            address(locality = "Izmir", adminArea = "Other Province"),
        )
        val cities: List<City> = (repository.searchCities(query) as AppResult.Success).data
        assertEquals(listOf("Izmir Province", "Other Province"), cities.map(City::adminArea))
    }

    @Test
    fun `country level result for an unknown name is dropped`() = runTest {
        geocoder.addresses = listOf(countryLevelAddress(featureName = "Türkiye", countryCode = "TR"))
        assertEquals(emptyList<String>(), searchCityNames())
    }

    @Test
    fun `country level result named like the search is kept for city states`() = runTest {
        geocoder.addresses = listOf(countryLevelAddress(featureName = "Singapore", countryCode = "SG"))
        val singapore: CitySearchQuery = CitySearchQuery(name = "singapore", country = Country("SG", "Singapore"), locale = TURKISH)
        val cities: List<City> = (repository.searchCities(singapore) as AppResult.Success).data
        assertEquals(listOf("Singapore"), cities.map(City::name))
    }

    @Test
    fun `no addresses is an empty success`() = runTest {
        assertEquals(AppResult.Success(emptyList<City>()), repository.searchCities(query))
    }

    @Test
    fun `io failure maps to a network error`() = runTest {
        geocoder.failure = IOException("grpc failed")
        assertEquals(AppResult.Failure(AppError.Network), repository.searchCities(query))
    }

    @Test
    fun `unexpected failure maps to an unknown error`() = runTest {
        geocoder.failure = IllegalStateException("service error")
        assertEquals(AppResult.Failure(AppError.Unknown), repository.searchCities(query))
    }

    private fun countryLevelAddress(featureName: String, countryCode: String): GeocodedAddress =
        address(locality = null, adminArea = null, featureName = featureName, countryCode = countryCode)

    private suspend fun searchCityNames(): List<String> =
        (repository.searchCities(query) as AppResult.Success).data.map(City::name)

    private companion object {
        val TURKISH: Locale = Locale.forLanguageTag("tr")
    }
}
