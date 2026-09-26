package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.error.map
import com.ahmetyildiz.quakealert.core.location.distanceKmTo
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetEarthquakeUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    suspend operator fun invoke(id: String): AppResult<EarthquakeDetails> {
        val area: AlertArea = userPreferencesRepository.userPreferences.first().alertSettings.area
        return earthquakeRepository.getEarthquake(id).map { earthquake ->
            EarthquakeDetails(earthquake = earthquake, distanceFromCity = distanceFromCity(earthquake, area))
        }
    }

    private fun distanceFromCity(earthquake: Earthquake, area: AlertArea): DistanceFromCity? {
        if (area !is AlertArea.AroundCity) return null
        return DistanceFromCity(
            cityName = area.city.name,
            distanceKm = area.city.location.distanceKmTo(earthquake.location),
            alertRadiusKm = area.radiusKm,
            isWithinAlertArea = area.contains(earthquake.location),
        )
    }
}
