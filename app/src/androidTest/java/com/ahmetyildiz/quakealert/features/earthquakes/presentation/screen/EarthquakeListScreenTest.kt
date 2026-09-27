package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeListContent
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeListUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class EarthquakeListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val now: Instant = Instant.parse("2026-09-25T12:00:00Z")
    private val clickedIds: MutableList<String> = mutableListOf()
    private var isShowAllClicked: Boolean = false
    private val selectedSortOrders: MutableList<EarthquakeSortOrder> = mutableListOf()
    private val actions = EarthquakeListActions(
        onRefresh = {},
        onRegionFilterSelected = {},
        onMagnitudeFilterSelected = {},
        onSortOrderSelected = { selectedSortOrders += it },
        onShowAllClicked = { isShowAllClicked = true },
        onEarthquakeClick = { clickedIds += it },
    )

    private val izmirEarthquake = EarthquakeWithDistance(
        earthquake = Earthquake(
            id = "us1",
            magnitude = Magnitude(value = 4.6, type = "mb"),
            place = "12 km SW of Seferihisar, Turkey",
            time = now.minus(Duration.ofHours(3)),
            location = GeoPoint(38.1, 26.8),
            depthKm = 9.0,
            detailUrl = "",
            isReviewed = true,
            hasTsunamiFlag = false,
            feltReportCount = null,
        ),
        distanceKm = 41.0,
    )

    @Test
    fun itemsShowPlaceTimeDepthAndDistance() {
        setContent(
            EarthquakeListUiState(
                content = EarthquakeListContent.ITEMS,
                earthquakes = listOf(izmirEarthquake),
                cachedCount = 1,
                nearCityName = "Izmir",
                lastRefreshedAt = now,
            ),
        )
        val depth: String = string(R.string.earthquake_depth, "9")
        val timeAndDepth: String = "${plural(R.plurals.relative_time_hours_ago, 3)} · $depth"
        composeRule.onNodeWithText("12 km SW of Seferihisar, Turkey").assertIsDisplayed()
        composeRule.onNodeWithText(timeAndDepth).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.earthquake_distance_from_city, "41", "Izmir")).assertIsDisplayed()
    }

    @Test
    fun newestFirstGroupsItemsUnderDayHeadersWithCounts() {
        val olderEarthquake: EarthquakeWithDistance = izmirEarthquake.copy(
            earthquake = izmirEarthquake.earthquake.copy(id = "us2", time = now.minus(Duration.ofDays(2))),
        )
        setContent(
            EarthquakeListUiState(
                content = EarthquakeListContent.ITEMS,
                earthquakes = listOf(izmirEarthquake, olderEarthquake),
                cachedCount = 2,
                lastRefreshedAt = now,
            ),
        )
        composeRule.onNodeWithText(plural(R.plurals.earthquake_count, 2)).assertIsDisplayed()
        composeRule.onNodeWithText(dayLabelOf(izmirEarthquake.earthquake.time)).assertIsDisplayed()
        composeRule.onAllNodesWithText(plural(R.plurals.earthquake_count, 1)).assertCountEquals(2)
    }

    @Test
    fun otherSortOrdersShowOneFlatList() {
        setContent(
            itemsState(nearCityName = null)
                .copy(options = EarthquakeListOptions(sortOrder = EarthquakeSortOrder.LARGEST_FIRST)),
        )
        composeRule.onNodeWithText("12 km SW of Seferihisar, Turkey").assertIsDisplayed()
        composeRule.onNodeWithText(dayLabelOf(izmirEarthquake.earthquake.time)).assertDoesNotExist()
        composeRule.onAllNodesWithText(plural(R.plurals.earthquake_count, 1)).assertCountEquals(1)
    }

    @Test
    fun tappingAnItemReportsItsId() {
        setContent(
            EarthquakeListUiState(
                content = EarthquakeListContent.ITEMS,
                earthquakes = listOf(izmirEarthquake),
                cachedCount = 1,
            ),
        )
        composeRule.onNodeWithText("12 km SW of Seferihisar, Turkey").performClick()
        assertEquals(listOf("us1"), clickedIds)
    }

    @Test
    fun staleDataShowsTheOfflineBannerAboveTheList() {
        setContent(
            EarthquakeListUiState(
                content = EarthquakeListContent.ITEMS,
                earthquakes = listOf(izmirEarthquake),
                cachedCount = 1,
                lastRefreshedAt = now,
                refreshError = AppError.Network,
            ),
        )
        val offlinePrefix: String = string(R.string.stale_data_offline_message, "").trim()
        composeRule.onNodeWithText(offlinePrefix, substring = true).assertIsDisplayed()
    }

    @Test
    fun filteredEmptyStateExplainsTheFiltersAndCanShowAll() {
        setContent(
            EarthquakeListUiState(
                content = EarthquakeListContent.EMPTY_FILTERED,
                cachedCount = 12,
                options = EarthquakeListOptions(
                    region = RegionFilter.NEAR_CITY,
                    magnitude = MagnitudeFilter.ABOVE_THRESHOLD,
                ),
                nearCityName = "Izmir",
                magnitudeThreshold = 7.0,
                lastRefreshedAt = now,
            ),
        )
        val threshold: String = string(R.string.magnitude_value, 7.0)
        val expectedTitle: String = composeRule.activity.resources
            .getQuantityString(R.plurals.earthquake_list_empty_near_above, DAYS, threshold, "Izmir", DAYS)
        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_show_all_earthquakes)).performClick()
        assertTrue(isShowAllClicked)
    }

    @Test
    fun sortMenuShowsTheCurrentOrderAndReportsTheChosenOne() {
        setContent(itemsState(nearCityName = "Izmir"))
        val newest: String = string(R.string.sort_newest_first)
        composeRule.onNodeWithContentDescription(string(R.string.sort_button_description, newest)).performClick()
        composeRule.onNodeWithText(string(R.string.sort_nearest_first)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.sort_largest_first)).performClick()
        assertEquals(listOf(EarthquakeSortOrder.LARGEST_FIRST), selectedSortOrders)
    }

    @Test
    fun sortMenuHidesNearestFirstWithoutAnArea() {
        setContent(itemsState(nearCityName = null))
        val newest: String = string(R.string.sort_newest_first)
        composeRule.onNodeWithContentDescription(string(R.string.sort_button_description, newest)).performClick()
        composeRule.onNodeWithText(string(R.string.sort_largest_first)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.sort_nearest_first)).assertDoesNotExist()
    }

    @Test
    fun errorStateOffersRetry() {
        setContent(EarthquakeListUiState(content = EarthquakeListContent.ERROR, refreshError = AppError.Network))
        composeRule.onNodeWithText(string(R.string.earthquake_list_error_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).assertIsDisplayed()
    }

    @Test
    fun emptyStateRenders() {
        setContent(EarthquakeListUiState(content = EarthquakeListContent.EMPTY, lastRefreshedAt = now))
        composeRule.onNodeWithText(string(R.string.earthquake_list_empty_title)).assertIsDisplayed()
    }

    @Test
    fun loadingStateRenders() {
        setContent(EarthquakeListUiState(content = EarthquakeListContent.LOADING))
        composeRule.onNodeWithContentDescription(string(R.string.state_loading)).assertIsDisplayed()
    }

    private fun itemsState(nearCityName: String?): EarthquakeListUiState =
        EarthquakeListUiState(
            content = EarthquakeListContent.ITEMS,
            earthquakes = listOf(izmirEarthquake),
            cachedCount = 1,
            nearCityName = nearCityName,
            lastRefreshedAt = now,
        )

    private fun dayLabelOf(time: Instant): String {
        val zone: ZoneId = ZoneId.systemDefault()
        val isToday: Boolean = time.atZone(zone).toLocalDate() == now.atZone(zone).toLocalDate()
        return string(if (isToday) R.string.day_today else R.string.day_yesterday)
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun plural(id: Int, count: Int): String = composeRule.activity.resources.getQuantityString(id, count, count)

    private fun setContent(uiState: EarthquakeListUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                EarthquakeListScreen(uiState = uiState, actions = actions, now = now)
            }
        }
    }

    private companion object {
        val DAYS: Int = EarthquakesConfig.RECENT_PERIOD.toDays().toInt()
    }
}
