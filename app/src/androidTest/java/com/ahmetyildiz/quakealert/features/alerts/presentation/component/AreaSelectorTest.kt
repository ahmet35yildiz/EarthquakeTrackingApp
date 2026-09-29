package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaMode
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchResult
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.LocationLookup
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AreaSelectorTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val turkiye = Country("TR", "Türkiye")
    private val japan = Country("JP", "Japan")
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val wholeWorld = AreaSelection(mode = AreaMode.WHOLE_WORLD, city = null, radiusKm = 250)
    private val nearNoCity = AreaSelection(mode = AreaMode.NEAR_CITY, city = null, radiusKm = 250)
    private val nearIzmir = AreaSelection(mode = AreaMode.NEAR_CITY, city = izmir, radiusKm = 250)
    private val searchState = CitySearchUiState(countries = listOf(japan, turkiye), selectedCountry = turkiye)
    private val selectedModes: MutableList<AreaMode> = mutableListOf()
    private val selectedRadii: MutableList<Int> = mutableListOf()
    private val selectedCities: MutableList<City> = mutableListOf()
    private val selectedCountries: MutableList<String> = mutableListOf()
    private val searchedNames: MutableList<String> = mutableListOf()
    private var dismissCount: Int = 0
    private var useMyLocationCount: Int = 0
    private var appSettingsCount: Int = 0
    private var locationSettingsCount: Int = 0
    private val actions = AreaSelectorActions(
        onModeSelected = { selectedModes += it },
        onRadiusSelected = { selectedRadii += it },
        onCitySelected = { selectedCities += it },
        onCountrySelected = { selectedCountries += it },
        onSearch = { searchedNames += it },
        onSearchDismissed = { dismissCount++ },
        location = CurrentLocationActions(
            onUseMyLocation = { useMyLocationCount++ },
            onOpenAppSettings = { appSettingsCount++ },
            onOpenLocationSettings = { locationSettingsCount++ },
        ),
    )

    @Test
    fun wholeWorldShowsTheWarningAndOffersNearCity() {
        setContent(wholeWorld, searchState)
        composeRule.onNodeWithText(string(R.string.alert_area_whole_world_warning_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alert_area_near_city)).performClick()
        assertEquals(listOf(AreaMode.NEAR_CITY), selectedModes)
    }

    @Test
    fun choosingNearCityWithoutACityOpensTheCitySearch() {
        setContent(wholeWorld, searchState)
        composeRule.onNodeWithText(string(R.string.alert_area_near_city)).performClick()
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertIsDisplayed()
    }

    @Test
    fun nearCityWithoutACityOffersTheCitySearchInsteadOfShowingIt() {
        setContent(nearNoCity, searchState)
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertDoesNotExist()
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertIsDisplayed()
    }

    @Test
    fun closingTheCitySearchDismissesIt() {
        setContent(nearNoCity, searchState)
        openCitySearch()
        composeRule.onNodeWithContentDescription(string(R.string.action_close)).performClick()
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertDoesNotExist()
        assertEquals(1, dismissCount)
    }

    @Test
    fun unavailableCitySearchHidesTheModeChoice() {
        setContent(wholeWorld, searchState.copy(isAvailable = false))
        composeRule.onNodeWithText(string(R.string.alert_area_near_city)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.alert_area_city_search_unavailable)).assertIsDisplayed()
    }

    @Test
    fun unavailableCitySearchStillLetsASavedCityBeClearedToTheWholeWorld() {
        setContent(nearIzmir, searchState.copy(isAvailable = false))
        composeRule.onNodeWithText("İzmir").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.alert_area_city_change_unavailable)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_change)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.city_search_title)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.alert_area_whole_world)).performClick()
        assertEquals(listOf(AreaMode.WHOLE_WORLD), selectedModes)
    }

    @Test
    fun unavailableCitySearchLetsTheClearedCityBePickedAgain() {
        setContent(nearIzmir.copy(mode = AreaMode.WHOLE_WORLD), searchState.copy(isAvailable = false))
        composeRule.onNodeWithText(string(R.string.alert_area_near_city)).performClick()
        assertEquals(listOf(AreaMode.NEAR_CITY), selectedModes)
    }

    @Test
    fun typedCityNameIsSearched() {
        setContent(nearNoCity, searchState)
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.city_search_label)).performTextInput("Izmir")
        composeRule.onNodeWithContentDescription(string(R.string.action_search)).performClick()
        assertEquals(listOf("Izmir"), searchedNames)
    }

    @Test
    fun foundCityCanBePicked() {
        setContent(nearNoCity, searchState.copy(result = CitySearchResult.Found(listOf(izmir))))
        openCitySearch()
        composeRule.onNodeWithText("İzmir").performScrollTo().performClick()
        assertEquals(listOf(izmir), selectedCities)
    }

    @Test
    fun noResultsNamesTheSearchAndTheCountry() {
        setContent(nearNoCity, searchState.copy(result = CitySearchResult.NoResults("Atlantis")))
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.city_search_no_results, "Atlantis", "Türkiye"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun selectedCityCanBeChangedAndTheChangeCancelled() {
        setContent(nearIzmir, searchState)
        composeRule.onNodeWithText("İzmir").assertIsDisplayed()
        composeRule.onNodeWithText("Türkiye").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_change)).performClick()
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.action_close)).performClick()
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertDoesNotExist()
        assertEquals(listOf("TR"), selectedCountries)
        assertEquals(1, dismissCount)
    }

    @Test
    fun radiusOptionCanBeSelected() {
        setContent(nearIzmir, searchState)
        composeRule.onNodeWithText(string(R.string.radius_km_value, 500)).performScrollTo().performClick()
        assertEquals(listOf(500), selectedRadii)
    }

    @Test
    fun countryIsPickedFromTheSearchableList() {
        setContent(nearNoCity, searchState)
        openCitySearch()
        composeRule.onNodeWithText("Türkiye").performClick()
        composeRule.onNodeWithText(string(R.string.country_search_hint)).performTextInput("jap")
        composeRule.onAllNodesWithText("Türkiye").assertCountEquals(1)
        composeRule.onNodeWithText("Japan").performClick()
        assertEquals(listOf("JP"), selectedCountries)
    }

    @Test
    fun useMyLocationAsksForTheLocation() {
        setContent(nearNoCity, searchState)
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.location_use_mine)).performClick()
        assertEquals(1, useMyLocationCount)
    }

    @Test
    fun locatingShowsProgressAndDisablesTheButton() {
        setContent(nearNoCity, searchState.copy(location = LocationLookup.Locating))
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.location_finding)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.location_use_mine)).assertIsNotEnabled()
    }

    @Test
    fun deniedPermissionOffersTheAppSettings() {
        setContent(nearNoCity, searchState.copy(location = LocationLookup.Failed(AppError.LocationPermissionDenied)))
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.location_error_permission)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performClick()
        assertEquals(1, appSettingsCount)
    }

    @Test
    fun locationTurnedOffOffersTheLocationSettings() {
        setContent(nearNoCity, searchState.copy(location = LocationLookup.Failed(AppError.LocationDisabled)))
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.location_error_disabled)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_open_settings)).performClick()
        assertEquals(1, locationSettingsCount)
    }

    @Test
    fun locationNotFoundCanBeRetried() {
        setContent(nearNoCity, searchState.copy(location = LocationLookup.Failed(AppError.LocationUnavailable)))
        openCitySearch()
        composeRule.onNodeWithText(string(R.string.action_retry)).performClick()
        assertEquals(1, useMyLocationCount)
    }

    @Test
    fun locatedCityIsPickedAndClosesTheSearch() {
        val citySearch: MutableState<CitySearchUiState> = mutableStateOf(searchState)
        val bornova = City(name = "Bornova", adminArea = "İzmir", countryCode = "TR", location = GeoPoint(38.46, 27.21))
        composeRule.setContent {
            QuakeAlertTheme {
                AreaSelector(selection = nearIzmir, citySearch = citySearch.value, actions = actions)
            }
        }
        composeRule.onNodeWithText(string(R.string.action_change)).performClick()
        citySearch.value = searchState.copy(location = LocationLookup.Found(bornova))
        composeRule.onNodeWithText(string(R.string.city_search_label)).assertDoesNotExist()
        assertEquals(listOf(bornova), selectedCities)
    }

    private fun openCitySearch() {
        composeRule.onNodeWithText(string(R.string.city_search_title)).performClick()
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setContent(selection: AreaSelection, citySearch: CitySearchUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AreaSelector(selection = selection, citySearch = citySearch, actions = actions)
                }
            }
        }
    }
}
