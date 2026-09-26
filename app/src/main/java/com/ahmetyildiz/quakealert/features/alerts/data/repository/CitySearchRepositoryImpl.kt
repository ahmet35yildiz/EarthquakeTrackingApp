package com.ahmetyildiz.quakealert.features.alerts.data.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.error.map
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.network.safeApiCall
import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddress
import com.ahmetyildiz.quakealert.features.alerts.data.model.toCityOrNull
import com.ahmetyildiz.quakealert.features.alerts.data.source.CityGeocoder
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.CitySearchRepository
import javax.inject.Inject

class CitySearchRepositoryImpl @Inject constructor(
    private val cityGeocoder: CityGeocoder,
) : CitySearchRepository {

    override fun isCitySearchAvailable(): Boolean = cityGeocoder.isPresent()

    override suspend fun searchCities(query: CitySearchQuery): AppResult<List<City>> {
        if (!cityGeocoder.isPresent()) return AppResult.Failure(AppError.GeocoderUnavailable)
        val locationName: String = "${query.name}, ${query.country.name}"
        return safeApiCall { cityGeocoder.findAddresses(locationName, query.locale) }
            .map { addresses -> toCitiesInCountry(addresses, query) }
    }

    private fun toCitiesInCountry(addresses: List<GeocodedAddress>, query: CitySearchQuery): List<City> =
        addresses
            .filter { !it.isCountryLevel || it.featureName.equals(query.name, ignoreCase = true) }
            .mapNotNull(GeocodedAddress::toCityOrNull)
            .filter { it.countryCode == query.country.code }
            .distinctBy { Triple(it.name, it.adminArea, it.countryCode) }
}
