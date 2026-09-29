package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.RegionFilterValue
import com.ahmetyildiz.quakealert.core.analytics.StatisticsPeriodValue
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CalculateEarthquakeStatisticsUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.FetchStatisticsEarthquakesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.ObserveRecentEarthquakesUseCase
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeStatisticsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val now: Instant = Instant.parse("2026-09-29T12:00:00Z")
    private val clock = FakeClock(now)
    private val izmirArea = AlertArea.AroundCity(City("Izmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250)
    private val nearToday: Earthquake = earthquake(id = "near", time = now, location = GeoPoint(38.3, 26.9))
    private val farToday: Earthquake = earthquake(id = "far", time = now, location = GeoPoint(35.7, 139.7))
    private val twentyDaysAgo: Earthquake = earthquake(id = "old", time = now.minus(Duration.ofDays(20)))

    private val earthquakeRepository = FakeEarthquakeRepository(cached = listOf(nearToday, farToday))
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val viewModel: EarthquakeStatisticsViewModel by lazy {
        EarthquakeStatisticsViewModel(
            observeRecentEarthquakes = ObserveRecentEarthquakesUseCase(
                earthquakeRepository,
                preferencesRepository,
                computeDispatcher = UnconfinedTestDispatcher(),
            ),
            fetchStatisticsEarthquakes = FetchStatisticsEarthquakesUseCase(earthquakeRepository, clock),
            calculateStatistics = CalculateEarthquakeStatisticsUseCase(),
            clock = clock,
            analyticsTracker = analyticsTracker,
            computeDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `last 7 days come from the cache without a request`() = runViewModelTest {
        assertEquals(StatisticsContent.LOADED, viewModel.uiState.value.content)
        assertEquals(2, viewModel.uiState.value.statistics?.totalCount)
        assertEquals(emptyList<Any>(), earthquakeRepository.receivedQueries)
    }

    @Test
    fun `last 30 days are fetched once and include older earthquakes`() = runViewModelTest {
        earthquakeRepository.remoteEarthquakes = listOf(nearToday, farToday, twentyDaysAgo)
        viewModel.onPeriodSelected(StatisticsPeriod.LAST_30_DAYS)
        assertEquals(3, viewModel.uiState.value.statistics?.totalCount)
        viewModel.onPeriodSelected(StatisticsPeriod.LAST_7_DAYS)
        viewModel.onPeriodSelected(StatisticsPeriod.LAST_30_DAYS)
        assertEquals(1, earthquakeRepository.receivedQueries.size)
    }

    @Test
    fun `a failed 30-day request shows the error and retry loads it`() = runViewModelTest {
        earthquakeRepository.failure = AppError.Network
        viewModel.onPeriodSelected(StatisticsPeriod.LAST_30_DAYS)
        assertEquals(StatisticsContent.ERROR, viewModel.uiState.value.content)
        assertEquals(AppError.Network, viewModel.uiState.value.error)
        earthquakeRepository.failure = null
        earthquakeRepository.remoteEarthquakes = listOf(twentyDaysAgo)
        viewModel.onRetry()
        assertEquals(StatisticsContent.LOADED, viewModel.uiState.value.content)
        assertEquals(1, viewModel.uiState.value.statistics?.totalCount)
    }

    @Test
    fun `a period without earthquakes shows the empty state`() = runViewModelTest {
        earthquakeRepository.cachedEarthquakes.value = emptyList()
        assertEquals(StatisticsContent.EMPTY, viewModel.uiState.value.content)
    }

    @Test
    fun `my area counts only earthquakes around the saved city`() = runViewModelTest {
        givenIzmirArea()
        viewModel.onRegionSelected(RegionFilter.NEAR_CITY)
        assertEquals("Izmir", viewModel.uiState.value.nearCityName)
        assertEquals(RegionFilter.NEAR_CITY, viewModel.uiState.value.region)
        assertEquals(1, viewModel.uiState.value.statistics?.totalCount)
    }

    @Test
    fun `without a saved area the whole world is shown`() = runViewModelTest {
        viewModel.onRegionSelected(RegionFilter.NEAR_CITY)
        assertNull(viewModel.uiState.value.nearCityName)
        assertEquals(RegionFilter.WORLD, viewModel.uiState.value.region)
        assertEquals(2, viewModel.uiState.value.statistics?.totalCount)
    }

    @Test
    fun `opening the screen and each change are recorded, a rotation is not`() = runViewModelTest {
        givenIzmirArea()
        viewModel.onScreenStarted()
        viewModel.onScreenStopped(isConfigurationChange = true)
        viewModel.onScreenStarted()
        viewModel.onPeriodSelected(StatisticsPeriod.LAST_30_DAYS)
        viewModel.onRegionSelected(RegionFilter.NEAR_CITY)
        viewModel.onRegionSelected(RegionFilter.NEAR_CITY)
        val expected: List<AnalyticsEvent> = listOf(
            viewed(StatisticsPeriodValue.LAST_7_DAYS, RegionFilterValue.WORLD),
            viewed(StatisticsPeriodValue.LAST_30_DAYS, RegionFilterValue.WORLD),
            viewed(StatisticsPeriodValue.LAST_30_DAYS, RegionFilterValue.NEAR_CITY),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    private fun givenIzmirArea() {
        preferencesRepository.update { it.copy(alertSettings = it.alertSettings.copy(area = izmirArea)) }
    }

    private fun viewed(period: StatisticsPeriodValue, region: RegionFilterValue): AnalyticsEvent =
        AnalyticsEvent.StatisticsViewed(period, region)

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }
}
