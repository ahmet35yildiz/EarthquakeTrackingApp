package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.CitySearchRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.DeviceLocationRepository
import java.util.Locale
import javax.inject.Inject

class FindCityAtCurrentLocationUseCase @Inject constructor(
    private val deviceLocationRepository: DeviceLocationRepository,
    private val citySearchRepository: CitySearchRepository,
) {

    suspend operator fun invoke(locale: Locale): AppResult<City> =
        when (val location: AppResult<GeoPoint> = deviceLocationRepository.getCurrentLocation()) {
            is AppResult.Failure -> location
            is AppResult.Success -> citySearchRepository.findCityAt(location.data, locale)
        }
}
