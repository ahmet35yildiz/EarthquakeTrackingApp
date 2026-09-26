package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails

data class EarthquakeDetailUiState(
    val content: EarthquakeDetailContent = EarthquakeDetailContent.LOADING,
    val details: EarthquakeDetails? = null,
    val error: AppError? = null,
)
