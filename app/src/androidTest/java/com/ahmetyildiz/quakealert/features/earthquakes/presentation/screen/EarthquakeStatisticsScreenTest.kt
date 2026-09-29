package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.core.ui.format.formatPlace
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DailyCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.SeverityCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeStatisticsUiState
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.StatisticsContent
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EarthquakeStatisticsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val now: Instant = Instant.parse("2026-09-29T12:00:00Z")
    private val clickedActions: MutableList<String> = mutableListOf()
    private val actions = EarthquakeStatisticsActions(
        onPeriodSelected = { clickedActions += "period:$it" },
        onRegionSelected = { clickedActions += "region:$it" },
        onRetry = { clickedActions += "retry" },
        onEarthquakeClick = { clickedActions += "earthquake:$it" },
    )
    private val largest = Earthquake(
        id = "us1",
        magnitude = Magnitude(value = 6.1, type = "mww"),
        place = LARGEST_PLACE,
        time = now.minusSeconds(3600),
        location = GeoPoint(38.21, 26.74),
        depthKm = 10.0,
        detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/us1",
        isReviewed = true,
        hasTsunamiFlag = false,
        feltReportCount = null,
    )
    private val statistics = EarthquakeStatistics(
        totalCount = 42,
        largest = largest,
        averageMagnitude = 3.4,
        severityCounts = MagnitudeSeverity.entries.map { SeverityCount(it, count = 1) },
        dailyCounts = (6 downTo 0).map { DailyCount(LocalDate.of(2026, 9, 29).minusDays(it.toLong()), count = 6 - it) },
        topRegions = listOf(RegionCount(TOP_REGION, 30)),
    )

    @Test
    fun loadedStatisticsShowEveryCard() {
        setContent(loaded())
        composeRule.onNodeWithText("42").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.statistics_magnitude_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.statistics_daily_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(formatPlace(TOP_REGION, composeRule.activity.resources)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun largestEarthquakeOpensItsDetail() {
        setContent(loaded())
        composeRule.onNodeWithText(formatPlace(LARGEST_PLACE, composeRule.activity.resources), substring = true)
            .performScrollTo()
            .performClick()
        assertEquals(listOf("earthquake:us1"), clickedActions)
    }

    @Test
    fun chipsReportTheChosenPeriodAndRegion() {
        setContent(loaded().copy(nearCityName = "Izmir"))
        composeRule.onNodeWithText(string(R.string.statistics_period_7_days)).assertIsSelected()
        composeRule.onNodeWithText(string(R.string.statistics_period_30_days)).performClick()
        composeRule.onNodeWithText(string(R.string.filter_near_city, "Izmir")).performClick()
        assertEquals(listOf("period:${StatisticsPeriod.LAST_30_DAYS}", "region:${RegionFilter.NEAR_CITY}"), clickedActions)
    }

    @Test
    fun areaChipIsHiddenWithoutASavedCity() {
        setContent(loaded())
        composeRule.onNodeWithText(string(R.string.filter_world)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.filter_near_city, "Izmir")).assertDoesNotExist()
    }

    @Test
    fun failedRequestOffersRetry() {
        setContent(EarthquakeStatisticsUiState(content = StatisticsContent.ERROR, error = AppError.Network))
        composeRule.onNodeWithText(string(R.string.statistics_error_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).performClick()
        assertEquals(listOf("retry"), clickedActions)
    }

    @Test
    fun emptyPeriodSaysSo() {
        setContent(EarthquakeStatisticsUiState(content = StatisticsContent.EMPTY))
        composeRule.onNodeWithText(string(R.string.statistics_empty_title)).assertIsDisplayed()
    }

    private fun loaded(): EarthquakeStatisticsUiState =
        EarthquakeStatisticsUiState(content = StatisticsContent.LOADED, statistics = statistics)

    private fun setContent(uiState: EarthquakeStatisticsUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                EarthquakeStatisticsScreen(uiState = uiState, actions = actions, now = now)
            }
        }
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private companion object {
        const val LARGEST_PLACE: String = "12 km SW of Seferihisar, Turkey"
        const val TOP_REGION: String = "Indonesia"
    }
}
