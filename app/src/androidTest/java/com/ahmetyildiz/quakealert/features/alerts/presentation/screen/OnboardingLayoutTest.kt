package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AreaSelector
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AreaSelectorActions
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.CurrentLocationActions
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaMode
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingPage
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingLayoutTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = OnboardingActions({}, {}, {}, {}, {}, {}, {})
    private val sacramento = City("Sacramento", "California", "US", GeoPoint(38.58, -121.49))
    private val areaActions = AreaSelectorActions(
        onModeSelected = {},
        onRadiusSelected = {},
        onCitySelected = {},
        onCountrySelected = {},
        onSearch = {},
        onSearchDismissed = {},
        location = CurrentLocationActions(onUseMyLocation = {}, onOpenAppSettings = {}, onOpenLocationSettings = {}),
    )

    @Test
    fun welcomePageFitsWithoutScrolling() {
        setContent(OnboardingUiState(page = OnboardingPage.WELCOME))
        assertNothingToScroll()
    }

    @Test
    fun setupPageForTheWholeWorldFitsWithoutScrolling() {
        setContent(OnboardingUiState(page = OnboardingPage.ALERT_SETUP), AreaSelection(AreaMode.WHOLE_WORLD, null, 250))
        assertNothingToScroll()
    }

    @Test
    fun setupPageWithACityFitsWithoutScrolling() {
        setContent(OnboardingUiState(page = OnboardingPage.ALERT_SETUP), AreaSelection(AreaMode.NEAR_CITY, sacramento, 250))
        assertNothingToScroll()
    }

    @Test
    fun notificationsPageFitsWithoutScrolling() {
        setContent(OnboardingUiState(page = OnboardingPage.NOTIFICATIONS, isPermissionDenied = true))
        assertNothingToScroll()
    }

    private fun assertNothingToScroll() {
        val range: ScrollAxisRange? = composeRule.onNode(hasScrollAction()).fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
        assertEquals(0f, range?.maxValue?.invoke() ?: 0f)
    }

    private fun setContent(
        uiState: OnboardingUiState,
        selection: AreaSelection = uiState.areaSelection,
    ) {
        composeRule.setContent {
            QuakeAlertTheme {
                OnboardingScreen(
                    uiState = uiState.copy(areaSelection = selection),
                    actions = actions,
                    isPermissionRequestSupported = true,
                ) { RealAreaSelector(selection) }
            }
        }
    }

    @Composable
    private fun RealAreaSelector(selection: AreaSelection) {
        AreaSelector(
            selection = selection,
            citySearch = CitySearchUiState(countries = listOf(Country("US", "United States"))),
            actions = areaActions,
        )
    }
}
