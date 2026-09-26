package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.CitySearchFailureReason
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.locale.DeviceRegionProvider
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.GetCountriesUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
    private val getCountries: GetCountriesUseCase,
    private val deviceRegionProvider: DeviceRegionProvider,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val state = MutableStateFlow(CitySearchUiState(isAvailable = searchCities.isAvailable()))
    private var displayLocale: Locale? = null
    private var searchJob: Job? = null

    val uiState: StateFlow<CitySearchUiState> = state.asStateFlow()

    fun onLocaleChanged(locale: Locale) {
        if (locale == displayLocale) return
        displayLocale = locale
        val countries: List<Country> = getCountries(locale)
        val selectedCode: String? = state.value.selectedCountry?.code ?: deviceRegionProvider.getDeviceRegionCode()
        state.update { current ->
            current.copy(countries = countries, selectedCountry = countries.firstOrNull { it.code == selectedCode })
        }
    }

    fun onCountrySelected(countryCode: String) {
        if (state.value.selectedCountry?.code == countryCode) return
        val country: Country = state.value.countries.firstOrNull { it.code == countryCode } ?: return
        searchJob?.cancel()
        state.update { it.copy(selectedCountry = country, result = CitySearchResult.Idle) }
    }

    fun onSearch(name: String) {
        val country: Country = state.value.selectedCountry ?: return
        val locale: Locale = displayLocale ?: return
        if (name.isBlank()) return
        searchJob?.cancel()
        state.update { it.copy(result = CitySearchResult.Loading) }
        searchJob = viewModelScope.launch {
            val result: AppResult<List<City>> = searchCities(CitySearchQuery(name, country, locale))
            state.update { it.copy(result = result.toSearchResult(name.trim())) }
            trackSearch(result, country)
        }
    }

    fun onSearchDismissed() {
        searchJob?.cancel()
        state.update { it.copy(result = CitySearchResult.Idle) }
    }

    private fun AppResult<List<City>>.toSearchResult(searchedName: String): CitySearchResult =
        when (this) {
            is AppResult.Success -> if (data.isEmpty()) {
                CitySearchResult.NoResults(searchedName)
            } else {
                CitySearchResult.Found(data)
            }
            is AppResult.Failure -> CitySearchResult.Failed(error)
        }

    private fun trackSearch(result: AppResult<List<City>>, country: Country) {
        val event: AnalyticsEvent = when (result) {
            is AppResult.Success -> AnalyticsEvent.CitySearchPerformed(country.code, result.data.size)
            is AppResult.Failure -> AnalyticsEvent.CitySearchFailed(result.error.toFailureReason())
        }
        analyticsTracker.track(event)
    }

    private fun AppError.toFailureReason(): CitySearchFailureReason =
        when (this) {
            AppError.Network -> CitySearchFailureReason.NETWORK
            AppError.GeocoderUnavailable -> CitySearchFailureReason.UNAVAILABLE
            else -> CitySearchFailureReason.UNKNOWN
        }
}
