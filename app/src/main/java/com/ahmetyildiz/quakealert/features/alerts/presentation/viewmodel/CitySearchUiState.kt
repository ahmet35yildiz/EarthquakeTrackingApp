package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country

data class CitySearchUiState(
    val isAvailable: Boolean = true,
    val countries: List<Country> = emptyList(),
    val selectedCountry: Country? = null,
    val result: CitySearchResult = CitySearchResult.Idle,
)

sealed interface CitySearchResult {

    data object Idle : CitySearchResult

    data object Loading : CitySearchResult

    data class Found(val cities: List<City>) : CitySearchResult

    data class NoResults(val searchedName: String) : CitySearchResult

    data class Failed(val error: AppError) : CitySearchResult
}
