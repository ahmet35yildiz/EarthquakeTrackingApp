package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.di.DefaultDispatcher
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.withDistanceFrom
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ObserveRecentEarthquakesUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val computeDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(options: EarthquakeListOptions): Flow<RecentEarthquakes> =
        combine(
            earthquakeRepository.observeCachedEarthquakes(),
            userPreferencesRepository.userPreferences,
        ) { earthquakes, preferences ->
            buildRecentEarthquakes(earthquakes, preferences, options)
        }.flowOn(computeDispatcher)

    private fun buildRecentEarthquakes(
        earthquakes: List<Earthquake>,
        preferences: UserPreferences,
        requestedOptions: EarthquakeListOptions,
    ): RecentEarthquakes {
        val area: AlertArea = preferences.alertSettings.area
        val threshold: Double = preferences.alertSettings.magnitudeThreshold
        val options: EarthquakeListOptions = requestedOptions.applicableTo(area)
        return RecentEarthquakes(
            earthquakes = earthquakes
                .filter { it.isInRegion(options.region, area) && it.hasMagnitude(options.magnitude, threshold) }
                .map { it.withDistanceFrom(area) }
                .sortedWith(comparatorFor(options.sortOrder)),
            appliedOptions = options,
            cachedCount = earthquakes.size,
            area = area,
            magnitudeThreshold = threshold,
            lastRefreshedAt = preferences.lastRefreshedAt,
        )
    }

    private fun EarthquakeListOptions.applicableTo(area: AlertArea): EarthquakeListOptions {
        if (area != AlertArea.WholeWorld) return this
        val sortOrder: EarthquakeSortOrder =
            if (sortOrder == EarthquakeSortOrder.NEAREST_FIRST) EarthquakeSortOrder.NEWEST_FIRST else sortOrder
        return copy(region = RegionFilter.WORLD, sortOrder = sortOrder)
    }

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

    private fun comparatorFor(sortOrder: EarthquakeSortOrder): Comparator<EarthquakeWithDistance> {
        val newestFirst: Comparator<EarthquakeWithDistance> = compareByDescending { it.earthquake.time }
        return when (sortOrder) {
            EarthquakeSortOrder.NEWEST_FIRST -> newestFirst
            EarthquakeSortOrder.LARGEST_FIRST ->
                compareByDescending<EarthquakeWithDistance> { it.magnitudeOrLowest }.then(newestFirst)
            EarthquakeSortOrder.NEAREST_FIRST ->
                compareBy<EarthquakeWithDistance> { it.distanceOrFarthest }.then(newestFirst)
        }
    }

    private val EarthquakeWithDistance.magnitudeOrLowest: Double
        get() = earthquake.magnitude?.value ?: Double.NEGATIVE_INFINITY

    private val EarthquakeWithDistance.distanceOrFarthest: Double
        get() = distanceKm ?: Double.POSITIVE_INFINITY
}
