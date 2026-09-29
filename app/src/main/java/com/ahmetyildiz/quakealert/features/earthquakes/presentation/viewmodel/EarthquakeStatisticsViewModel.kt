package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.RegionFilterValue
import com.ahmetyildiz.quakealert.core.analytics.StatisticsPeriodValue
import com.ahmetyildiz.quakealert.core.di.DefaultDispatcher
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsInput
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CalculateEarthquakeStatisticsUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.FetchStatisticsEarthquakesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.ObserveRecentEarthquakesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakeStatisticsViewModel @Inject constructor(
    observeRecentEarthquakes: ObserveRecentEarthquakesUseCase,
    private val fetchStatisticsEarthquakes: FetchStatisticsEarthquakesUseCase,
    private val calculateStatistics: CalculateEarthquakeStatisticsUseCase,
    private val clock: Clock,
    private val analyticsTracker: AnalyticsTracker,
    @DefaultDispatcher computeDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val options = MutableStateFlow(StatisticsOptions())
    private val fetchedPeriods = MutableStateFlow<Map<StatisticsPeriod, FetchState>>(emptyMap())
    private var isRestartAfterConfigurationChange: Boolean = false

    val uiState: StateFlow<EarthquakeStatisticsUiState> = combine(
        options,
        observeRecentEarthquakes(EarthquakeListOptions()),
        fetchedPeriods,
        ::toUiState,
    ).flowOn(computeDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EarthquakeStatisticsUiState())

    fun onScreenStarted() {
        if (!isRestartAfterConfigurationChange) trackViewed()
        isRestartAfterConfigurationChange = false
    }

    fun onScreenStopped(isConfigurationChange: Boolean) {
        isRestartAfterConfigurationChange = isConfigurationChange
    }

    fun onPeriodSelected(period: StatisticsPeriod) {
        if (options.value.period == period) return
        options.update { it.copy(period = period) }
        if (fetchedPeriods.value[period] !is FetchState.Loaded) fetch(period)
        trackViewed()
    }

    fun onRegionSelected(region: RegionFilter) {
        if (options.value.region == region) return
        options.update { it.copy(region = region) }
        trackViewed()
    }

    fun onRetry() {
        fetch(options.value.period)
    }

    private fun fetch(period: StatisticsPeriod) {
        if (period.isCoveredByCache() || fetchedPeriods.value[period] == FetchState.Loading) return
        fetchedPeriods.update { it + (period to FetchState.Loading) }
        viewModelScope.launch {
            val state: FetchState = when (val result: AppResult<List<Earthquake>> = fetchStatisticsEarthquakes(period)) {
                is AppResult.Success -> FetchState.Loaded(result.data)
                is AppResult.Failure -> FetchState.Failed(result.error)
            }
            fetchedPeriods.update { it + (period to state) }
        }
    }

    private fun toUiState(
        options: StatisticsOptions,
        recent: RecentEarthquakes,
        fetched: Map<StatisticsPeriod, FetchState>,
    ): EarthquakeStatisticsUiState {
        val region: RegionFilter = if (recent.area == AlertArea.WholeWorld) RegionFilter.WORLD else options.region
        val base = EarthquakeStatisticsUiState(
            period = options.period,
            region = region,
            nearCityName = (recent.area as? AlertArea.AroundCity)?.city?.name,
        )
        val source: FetchState =
            if (options.period.isCoveredByCache()) FetchState.Loaded(recent.earthquakes.map { it.earthquake })
            else fetched[options.period] ?: FetchState.Loading
        return when (source) {
            FetchState.Loading -> base
            is FetchState.Failed -> base.copy(content = StatisticsContent.ERROR, error = source.error)
            is FetchState.Loaded -> loadedState(base, source.earthquakes, recent.area)
        }
    }

    private fun loadedState(
        base: EarthquakeStatisticsUiState,
        earthquakes: List<Earthquake>,
        area: AlertArea,
    ): EarthquakeStatisticsUiState {
        val input = StatisticsInput(earthquakes, base.period, base.region, area, clock.now(), clock.zone())
        val statistics: EarthquakeStatistics = calculateStatistics(input)
        val content: StatisticsContent =
            if (statistics.totalCount == 0) StatisticsContent.EMPTY else StatisticsContent.LOADED
        return base.copy(content = content, statistics = statistics)
    }

    private fun trackViewed() {
        val selected: StatisticsOptions = options.value
        val event = AnalyticsEvent.StatisticsViewed(selected.period.toAnalyticsValue(), selected.region.toAnalyticsValue())
        analyticsTracker.track(event)
    }

    private fun StatisticsPeriod.isCoveredByCache(): Boolean =
        dayCount <= EarthquakesConfig.RECENT_PERIOD.toDays()

    private fun StatisticsPeriod.toAnalyticsValue(): StatisticsPeriodValue =
        when (this) {
            StatisticsPeriod.LAST_7_DAYS -> StatisticsPeriodValue.LAST_7_DAYS
            StatisticsPeriod.LAST_30_DAYS -> StatisticsPeriodValue.LAST_30_DAYS
        }

    private fun RegionFilter.toAnalyticsValue(): RegionFilterValue =
        when (this) {
            RegionFilter.WORLD -> RegionFilterValue.WORLD
            RegionFilter.NEAR_CITY -> RegionFilterValue.NEAR_CITY
        }

    private data class StatisticsOptions(
        val period: StatisticsPeriod = StatisticsPeriod.LAST_7_DAYS,
        val region: RegionFilter = RegionFilter.WORLD,
    )

    private sealed interface FetchState {
        data object Loading : FetchState
        data class Loaded(val earthquakes: List<Earthquake>) : FetchState
        data class Failed(val error: AppError) : FetchState
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
