package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.CitySearchFailureReason
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.features.alerts.domain.model.CitySearchQuery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeCitySearchRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.GetCountriesUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SearchCitiesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class CitySearchViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val repository = FakeCitySearchRepository(result = AppResult.Success(listOf(izmir)))
    private val analyticsTracker = FakeAnalyticsTracker()
    private var deviceRegionCode: String? = "TR"
    private val viewModel: CitySearchViewModel by lazy {
        CitySearchViewModel(
            searchCities = SearchCitiesUseCase(repository),
            getCountries = GetCountriesUseCase(),
            deviceRegionProvider = { deviceRegionCode },
            analyticsTracker = analyticsTracker,
        )
    }

    private val state: CitySearchUiState
        get() = viewModel.uiState.value

    @Test
    fun `availability comes from the city search`() {
        repository.isAvailable = false
        assertFalse(state.isAvailable)
    }

    @Test
    fun `device region is the default country`() {
        viewModel.onLocaleChanged(TURKISH)
        assertEquals(Country("TR", "Türkiye"), state.selectedCountry)
        assertTrue(state.countries.size > MIN_COUNTRY_COUNT)
    }

    @Test
    fun `no country is selected when the device has no region`() {
        deviceRegionCode = null
        viewModel.onLocaleChanged(Locale.ENGLISH)
        assertNull(state.selectedCountry)
    }

    @Test
    fun `language change renames countries and keeps the selection`() {
        viewModel.onLocaleChanged(Locale.ENGLISH)
        viewModel.onCountrySelected("DE")
        viewModel.onLocaleChanged(TURKISH)
        assertEquals(Country("DE", "Almanya"), state.selectedCountry)
    }

    @Test
    fun `selecting another country clears the previous results`() {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        viewModel.onCountrySelected("JP")
        assertEquals("JP", state.selectedCountry?.code)
        assertEquals(CitySearchResult.Idle, state.result)
    }

    @Test
    fun `unknown country code is ignored`() {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onCountrySelected("??")
        assertEquals("TR", state.selectedCountry?.code)
    }

    @Test
    fun `search sends name, country and display locale`() {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        assertEquals(listOf(CitySearchQuery("Izmir", Country("TR", "Türkiye"), TURKISH)), repository.queries)
    }

    @Test
    fun `search shows loading until the result arrives`() = runTest {
        val pending = CompletableDeferred<AppResult<List<City>>>()
        repository.pendingResult = pending
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        assertEquals(CitySearchResult.Loading, state.result)
        pending.complete(AppResult.Success(listOf(izmir)))
        assertEquals(CitySearchResult.Found(listOf(izmir)), state.result)
    }

    @Test
    fun `found cities are shown and tracked with the country`() {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        assertEquals(CitySearchResult.Found(listOf(izmir)), state.result)
        assertEquals(listOf(AnalyticsEvent.CitySearchPerformed(countryCode = "TR", resultCount = 1)), analyticsTracker.events)
    }

    @Test
    fun `empty result shows the trimmed searched name`() {
        repository.result = AppResult.Success(emptyList())
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("  Atlantis ")
        assertEquals(CitySearchResult.NoResults("Atlantis"), state.result)
        assertEquals(listOf(AnalyticsEvent.CitySearchPerformed(countryCode = "TR", resultCount = 0)), analyticsTracker.events)
    }

    @Test
    fun `failure is shown and tracked with its reason`() {
        repository.result = AppResult.Failure(AppError.Network)
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        assertEquals(CitySearchResult.Failed(AppError.Network), state.result)
        assertEquals(listOf(AnalyticsEvent.CitySearchFailed(CitySearchFailureReason.NETWORK)), analyticsTracker.events)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `blank name does not search`(name: String) {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch(name)
        assertTrue(repository.queries.isEmpty())
        assertEquals(CitySearchResult.Idle, state.result)
    }

    @Test
    fun `search waits for the country list`() {
        viewModel.onSearch("Izmir")
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `dismissing clears the results`() {
        viewModel.onLocaleChanged(TURKISH)
        viewModel.onSearch("Izmir")
        viewModel.onSearchDismissed()
        assertEquals(CitySearchResult.Idle, state.result)
    }

    private companion object {
        val TURKISH: Locale = Locale.forLanguageTag("tr")
        const val MIN_COUNTRY_COUNT: Int = 200
    }
}
