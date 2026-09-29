package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod

enum class StatisticsContent { LOADING, LOADED, EMPTY, ERROR }

data class EarthquakeStatisticsUiState(
    val content: StatisticsContent = StatisticsContent.LOADING,
    val period: StatisticsPeriod = StatisticsPeriod.LAST_7_DAYS,
    val region: RegionFilter = RegionFilter.WORLD,
    val nearCityName: String? = null,
    val statistics: EarthquakeStatistics? = null,
    val error: AppError? = null,
)
