package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.error.map
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.distanceFromCityOrNull
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
            EarthquakeDetails(earthquake = earthquake, distanceFromCity = area.distanceFromCityOrNull(earthquake))
        }
    }
}
