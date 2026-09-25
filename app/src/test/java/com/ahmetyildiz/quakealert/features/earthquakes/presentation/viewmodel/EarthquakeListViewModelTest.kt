package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.MagnitudeFilterValue
import com.ahmetyildiz.quakealert.core.analytics.RefreshTrigger
import com.ahmetyildiz.quakealert.core.analytics.RegionFilterValue
import com.ahmetyildiz.quakealert.core.analytics.SortOrderValue
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CheckCacheFreshnessUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.ObserveRecentEarthquakesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.RefreshEarthquakesUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration

class EarthquakeListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val izmirArea = AlertArea.AroundCity(City("Izmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250)
    private val nearStrong: Earthquake =
        earthquake(id = "near-strong", magnitude = 5.0, location = GeoPoint(38.3, 26.9))
    private val nearWeak: Earthquake = earthquake(id = "near-weak", magnitude = 3.0, location = GeoPoint(38.5, 27.2))
    private val farStrong: Earthquake = earthquake(id = "far-strong", magnitude = 6.0, location = GeoPoint(35.7, 139.7))
    private val remote: List<Earthquake> = listOf(nearStrong, nearWeak, farStrong)

    private val earthquakeRepository = FakeEarthquakeRepository().apply { remoteEarthquakes = remote }
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val viewModel: EarthquakeListViewModel by lazy {
        EarthquakeListViewModel(
            observeRecentEarthquakes = ObserveRecentEarthquakesUseCase(
                earthquakeRepository,
                preferencesRepository,
                computeDispatcher = UnconfinedTestDispatcher(),
            ),
            refreshEarthquakes = RefreshEarthquakesUseCase(earthquakeRepository, preferencesRepository, clock),
            checkCacheFreshness = CheckCacheFreshnessUseCase(preferencesRepository, clock),
            analyticsTracker = analyticsTracker,
        )
    }

    @Test
    fun `first open without a cache shows loading, then the refreshed list`() = runViewModelTest {
        assertEquals(EarthquakeListContent.LOADING, viewModel.uiState.value.content)
        viewModel.onScreenStarted()
        assertEquals(EarthquakeListContent.ITEMS, viewModel.uiState.value.content)
        assertEquals(remote.map { it.id }, viewModel.uiState.value.ids())
        assertEquals(clock.now(), viewModel.uiState.value.lastRefreshedAt)
    }

    @Test
    fun `first open logs the view and an initial refresh with the stored count`() = runViewModelTest {
        viewModel.onScreenStarted()
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.EarthquakeListViewed(
                RegionFilterValue.WORLD,
                MagnitudeFilterValue.ALL,
                SortOrderValue.NEWEST_FIRST,
            ),
            AnalyticsEvent.EarthquakeListRefreshed(RefreshTrigger.INITIAL, isSuccessful = true, count = 3),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    @Test
    fun `fresh cache is shown without a refresh`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ofMinutes(4))
        viewModel.onScreenStarted()
        assertTrue(earthquakeRepository.receivedQueries.isEmpty())
        assertEquals(EarthquakeListContent.ITEMS, viewModel.uiState.value.content)
    }

    @Test
    fun `cache older than five minutes is refreshed as stale`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ofMinutes(6))
        viewModel.onScreenStarted()
        assertEquals(1, earthquakeRepository.receivedQueries.size)
        assertEquals(RefreshTrigger.STALE, analyticsTracker.refreshTriggers().single())
    }

    @Test
    fun `failed first load shows the error, and retry shows the list`() = runViewModelTest {
        earthquakeRepository.failure = AppError.Network
        viewModel.onScreenStarted()
        assertEquals(EarthquakeListContent.ERROR, viewModel.uiState.value.content)
        assertEquals(AppError.Network, viewModel.uiState.value.refreshError)
        earthquakeRepository.failure = null
        viewModel.onRefresh()
        assertEquals(EarthquakeListContent.ITEMS, viewModel.uiState.value.content)
        assertEquals(listOf(RefreshTrigger.INITIAL, RefreshTrigger.PULL), analyticsTracker.refreshTriggers())
    }

    @Test
    fun `failed refresh with a cache keeps the list and warns about stale data`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ofMinutes(30))
        earthquakeRepository.failure = AppError.Network
        viewModel.onScreenStarted()
        val state: EarthquakeListUiState = viewModel.uiState.value
        assertEquals(EarthquakeListContent.ITEMS, state.content)
        assertTrue(state.isShowingStaleData)
        val refreshed = analyticsTracker.events.last() as AnalyticsEvent.EarthquakeListRefreshed
        val expected = AnalyticsEvent.EarthquakeListRefreshed(RefreshTrigger.STALE, isSuccessful = false, count = 3)
        assertEquals(expected, refreshed)
    }

    @Test
    fun `successful pull refresh clears the stale data warning`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ofMinutes(30))
        earthquakeRepository.failure = AppError.Server(503)
        viewModel.onScreenStarted()
        earthquakeRepository.failure = null
        viewModel.onRefresh()
        assertFalse(viewModel.uiState.value.isShowingStaleData)
    }

    @Test
    fun `near city chip is offered only when an area is set`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        assertEquals(null, viewModel.uiState.value.nearCityName)
        givenArea(izmirArea)
        assertEquals("Izmir", viewModel.uiState.value.nearCityName)
    }

    @Test
    fun `filters narrow the list and each change is logged once`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        givenArea(izmirArea)
        viewModel.onRegionFilterSelected(RegionFilter.NEAR_CITY)
        viewModel.onRegionFilterSelected(RegionFilter.NEAR_CITY)
        viewModel.onMagnitudeFilterSelected(MagnitudeFilter.ABOVE_THRESHOLD)
        assertEquals(listOf("near-strong"), viewModel.uiState.value.ids())
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.RegionFilterChanged(RegionFilterValue.NEAR_CITY),
            AnalyticsEvent.MagnitudeFilterChanged(MagnitudeFilterValue.ABOVE_THRESHOLD),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    @Test
    fun `no match for the filters shows the filtered empty state and show all resets them`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        givenArea(izmirArea.copy(radiusKm = 50))
        preferencesRepository.update { it.copy(alertSettings = it.alertSettings.copy(magnitudeThreshold = 7.0)) }
        viewModel.onMagnitudeFilterSelected(MagnitudeFilter.ABOVE_THRESHOLD)
        assertEquals(EarthquakeListContent.EMPTY_FILTERED, viewModel.uiState.value.content)
        viewModel.onShowAllClicked()
        assertEquals(EarthquakeListContent.ITEMS, viewModel.uiState.value.content)
        assertEquals(3, viewModel.uiState.value.earthquakes.size)
    }

    @Test
    fun `rotation does not count as a new view, returning to the screen does`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        viewModel.onScreenStarted()
        viewModel.onScreenStopped(isConfigurationChange = true)
        viewModel.onScreenStarted()
        viewModel.onScreenStopped(isConfigurationChange = false)
        viewModel.onScreenStarted()
        assertEquals(2, analyticsTracker.events.count { it is AnalyticsEvent.EarthquakeListViewed })
    }

    @Test
    fun `sort order reorders the list and each change is logged once`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        viewModel.onSortOrderSelected(EarthquakeSortOrder.LARGEST_FIRST)
        viewModel.onSortOrderSelected(EarthquakeSortOrder.LARGEST_FIRST)
        assertEquals(listOf("far-strong", "near-strong", "near-weak"), viewModel.uiState.value.ids())
        assertEquals(EarthquakeSortOrder.LARGEST_FIRST, viewModel.uiState.value.options.sortOrder)
        assertEquals(listOf(AnalyticsEvent.ListSortChanged(SortOrderValue.LARGEST_FIRST)), analyticsTracker.events)
    }

    @Test
    fun `nearest first is offered only with an area and orders by distance`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        assertFalse(viewModel.uiState.value.isNearestSortAvailable)
        givenArea(izmirArea)
        viewModel.onSortOrderSelected(EarthquakeSortOrder.NEAREST_FIRST)
        assertTrue(viewModel.uiState.value.isNearestSortAvailable)
        assertEquals(listOf("near-weak", "near-strong", "far-strong"), viewModel.uiState.value.ids())
    }

    @Test
    fun `show all resets the filters but keeps the sort order`() = runViewModelTest {
        givenCache(refreshedAgo = Duration.ZERO)
        viewModel.onSortOrderSelected(EarthquakeSortOrder.LARGEST_FIRST)
        viewModel.onMagnitudeFilterSelected(MagnitudeFilter.ABOVE_THRESHOLD)
        viewModel.onShowAllClicked()
        assertEquals(EarthquakeSortOrder.LARGEST_FIRST, viewModel.uiState.value.options.sortOrder)
        assertEquals(MagnitudeFilter.ALL, viewModel.uiState.value.options.magnitude)
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    private fun givenCache(refreshedAgo: Duration) {
        earthquakeRepository.cachedEarthquakes.value = remote
        preferencesRepository.update { it.copy(lastRefreshedAt = clock.now().minus(refreshedAgo)) }
    }

    private fun givenArea(area: AlertArea) {
        preferencesRepository.update {
            it.copy(alertSettings = AlertSettings(isEnabled = true, magnitudeThreshold = 4.5, area = area))
        }
    }

    private fun EarthquakeListUiState.ids(): List<String> = earthquakes.map { it.earthquake.id }

    private fun FakeAnalyticsTracker.refreshTriggers(): List<RefreshTrigger> =
        events.filterIsInstance<AnalyticsEvent.EarthquakeListRefreshed>().map { it.trigger }
}
