package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.error.map
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.withDistanceFrom
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetEarthquakeUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    suspend operator fun invoke(id: String): AppResult<EarthquakeWithDistance> {
        val area: AlertArea = userPreferencesRepository.userPreferences.first().alertSettings.area
        return earthquakeRepository.getEarthquake(id).map { it.withDistanceFrom(area) }
    }
}
