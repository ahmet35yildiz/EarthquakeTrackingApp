package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFilters
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.withDistanceFrom
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveRecentEarthquakesUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    operator fun invoke(filters: EarthquakeFilters): Flow<RecentEarthquakes> =
        combine(
            earthquakeRepository.observeCachedEarthquakes(),
            userPreferencesRepository.userPreferences,
        ) { earthquakes, preferences ->
            buildRecentEarthquakes(earthquakes, preferences, filters)
        }

    private fun buildRecentEarthquakes(
        earthquakes: List<Earthquake>,
        preferences: UserPreferences,
        requestedFilters: EarthquakeFilters,
    ): RecentEarthquakes {
        val area: AlertArea = preferences.alertSettings.area
        val threshold: Double = preferences.alertSettings.magnitudeThreshold
        val filters: EarthquakeFilters = requestedFilters.applicableTo(area)
        return RecentEarthquakes(
            earthquakes = earthquakes
                .filter { it.isInRegion(filters.region, area) && it.hasMagnitude(filters.magnitude, threshold) }
                .map { it.withDistanceFrom(area) },
            appliedFilters = filters,
            cachedCount = earthquakes.size,
            area = area,
            magnitudeThreshold = threshold,
            lastRefreshedAt = preferences.lastRefreshedAt,
        )
    }

    private fun EarthquakeFilters.applicableTo(area: AlertArea): EarthquakeFilters =
        if (area == AlertArea.WholeWorld) copy(region = RegionFilter.WORLD) else this

    private fun Earthquake.isInRegion(region: RegionFilter, area: AlertArea): Boolean =
        when (region) {
            RegionFilter.WORLD -> true
            RegionFilter.NEAR_CITY -> area.contains(location)
        }

    private fun Earthquake.hasMagnitude(filter: MagnitudeFilter, threshold: Double): Boolean =
        when (filter) {
            MagnitudeFilter.ALL -> true
            MagnitudeFilter.ABOVE_THRESHOLD -> magnitude?.let { it.value >= threshold } ?: false
        }
}
