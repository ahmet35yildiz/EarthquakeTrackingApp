package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeDetailContent
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class EarthquakeDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val now: Instant = Instant.parse("2026-09-25T12:00:00Z")
    private val clickedActions: MutableList<String> = mutableListOf()
    private val actions = EarthquakeDetailActions(
        onBack = { clickedActions += "back" },
        onRetry = { clickedActions += "retry" },
        onOpenInMaps = { clickedActions += "maps" },
        onViewOnUsgs = { clickedActions += "usgs" },
        onShare = { clickedActions += "share" },
    )
    private val earthquake = Earthquake(
        id = "us1",
        magnitude = Magnitude(value = 4.6, type = "mww"),
        place = "12 km SW of Seferihisar, Turkey",
        time = now.minus(Duration.ofHours(3)),
        location = GeoPoint(38.21, -26.74),
        depthKm = 9.0,
        detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/us1",
        isReviewed = true,
        hasTsunamiFlag = true,
        feltReportCount = FELT_REPORTS,
    )
    private val distance = DistanceFromCity(
        cityName = "Izmir",
        distanceKm = 41.0,
        alertRadiusKm = 250,
        isWithinAlertArea = true,
    )

    @Test
    fun loadedStateShowsTheKeyFacts() {
        setContent(loaded(distance))
        composeRule.onNodeWithText("12 km SW of Seferihisar, Turkey").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.earthquake_distance_from_city, "41", "Izmir")).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_within_alert_area, 250)).assertIsDisplayed()
        val magnitude: String = string(R.string.magnitude_value, 4.6)
        composeRule.onNodeWithText(string(R.string.detail_magnitude_with_type, magnitude, "mww")).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_depth_value, "9")).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_status_reviewed)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_tsunami_flag_set)).performScrollTo().assertIsDisplayed()
        val felt: String = composeRule.activity.resources
            .getQuantityString(R.plurals.detail_felt_reports_value, FELT_REPORTS, FELT_REPORTS)
        composeRule.onNodeWithText(felt).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun coordinatesShowHemispheres() {
        setContent(loaded(distance))
        val latitude: String = string(R.string.coordinate_value, twoDecimals(38.21), string(R.string.coordinate_north))
        val longitude: String = string(R.string.coordinate_value, twoDecimals(26.74), string(R.string.coordinate_west))
        val coordinates: String = string(R.string.coordinate_pair, latitude, longitude)
        composeRule.onNodeWithText(coordinates).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun distanceCardIsHiddenWithoutAnArea() {
        setContent(loaded(distance = null))
        composeRule.onNodeWithText(string(R.string.detail_within_alert_area, 250)).assertDoesNotExist()
    }

    @Test
    fun actionsReportTheirClicks() {
        setContent(loaded(distance))
        composeRule.onNodeWithContentDescription(string(R.string.action_share)).performClick()
        composeRule.onNodeWithText(string(R.string.action_open_in_maps)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.action_view_on_usgs)).performScrollTo().performClick()
        composeRule.onNodeWithContentDescription(string(R.string.action_back)).performClick()
        assertEquals(listOf("share", "maps", "usgs", "back"), clickedActions)
    }

    @Test
    fun notFoundStateExplainsWhyAndHidesShare() {
        setContent(EarthquakeDetailUiState(content = EarthquakeDetailContent.NOT_FOUND, error = AppError.NotFound))
        composeRule.onNodeWithText(string(R.string.detail_not_found_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.action_share)).assertDoesNotExist()
    }

    @Test
    fun errorStateOffersRetry() {
        setContent(EarthquakeDetailUiState(content = EarthquakeDetailContent.ERROR, error = AppError.Network))
        composeRule.onNodeWithText(string(R.string.action_retry)).performClick()
        assertEquals(listOf("retry"), clickedActions)
    }

    private fun loaded(distance: DistanceFromCity?): EarthquakeDetailUiState =
        EarthquakeDetailUiState(
            content = EarthquakeDetailContent.LOADED,
            details = EarthquakeDetails(earthquake, distance),
        )

    private fun twoDecimals(value: Double): String =
        NumberFormat.getNumberInstance(composeRule.activity.resources.configuration.locales[0]).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(value)

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setContent(uiState: EarthquakeDetailUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                EarthquakeDetailScreen(uiState = uiState, actions = actions, now = now)
            }
        }
    }

    private companion object {
        const val FELT_REPORTS: Int = 23
    }
}
