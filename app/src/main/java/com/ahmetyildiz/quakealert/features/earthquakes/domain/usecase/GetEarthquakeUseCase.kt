package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.distanceFromCityOrNull
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetEarthquakeUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    suspend operator fun invoke(id: String, shouldRevalidate: Boolean = false): AppResult<EarthquakeDetails> {
        val area: AlertArea = userPreferencesRepository.userPreferences.first().alertSettings.area
        val cached: Earthquake? = earthquakeRepository.getCachedEarthquake(id)
        if (cached != null && !shouldRevalidate) return AppResult.Success(area.detailsOf(cached))
        return fetchOrFallBack(id, cached, area)
    }

    private suspend fun fetchOrFallBack(
        id: String,
        cached: Earthquake?,
        area: AlertArea,
    ): AppResult<EarthquakeDetails> {
        return when (val fetched: AppResult<Earthquake> = earthquakeRepository.fetchEarthquake(id)) {
            is AppResult.Success -> AppResult.Success(area.detailsOf(fetched.data))
            is AppResult.Failure -> fallBackToCached(cached, fetched.error, area)
        }
    }

    private fun fallBackToCached(cached: Earthquake?, error: AppError, area: AlertArea): AppResult<EarthquakeDetails> {
        if (cached == null) return AppResult.Failure(error)
        val isRefreshFailed: Boolean = error != AppError.NotFound
        return AppResult.Success(area.detailsOf(cached, isRefreshFailed = isRefreshFailed))
    }

    private fun AlertArea.detailsOf(earthquake: Earthquake, isRefreshFailed: Boolean = false): EarthquakeDetails =
        EarthquakeDetails(
            earthquake = earthquake,
            distanceFromCity = distanceFromCityOrNull(earthquake),
            isSavedCopyAfterFailedRefresh = isRefreshFailed,
        )
}
