package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import java.time.Instant

data class EarthquakeListUiState(
    val content: EarthquakeListContent = EarthquakeListContent.LOADING,
    val earthquakes: List<EarthquakeWithDistance> = emptyList(),
    val cachedCount: Int = 0,
    val options: EarthquakeListOptions = EarthquakeListOptions(),
    val nearCityName: String? = null,
    val magnitudeThreshold: Double = AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD,
    val lastRefreshedAt: Instant? = null,
    val isRefreshing: Boolean = false,
    val refreshError: AppError? = null,
) {

    val isShowingStaleData: Boolean
        get() = refreshError != null && cachedCount > 0

    val isNearestSortAvailable: Boolean
        get() = nearCityName != null
}
