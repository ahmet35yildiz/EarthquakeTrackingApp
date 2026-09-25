package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.MagnitudeFilterValue
import com.ahmetyildiz.quakealert.core.analytics.RefreshTrigger
import com.ahmetyildiz.quakealert.core.analytics.RegionFilterValue
import com.ahmetyildiz.quakealert.core.analytics.SortOrderValue
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.CacheFreshness
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CheckCacheFreshnessUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.ObserveRecentEarthquakesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.RefreshEarthquakesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EarthquakeListViewModel @Inject constructor(
    observeRecentEarthquakes: ObserveRecentEarthquakesUseCase,
    private val refreshEarthquakes: RefreshEarthquakesUseCase,
    private val checkCacheFreshness: CheckCacheFreshnessUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val requestedOptions = MutableStateFlow(EarthquakeListOptions())
    private val refreshStatus = MutableStateFlow(RefreshStatus())
    private var isRestartAfterConfigurationChange: Boolean = false

    val uiState: StateFlow<EarthquakeListUiState> = combine(
        requestedOptions.flatMapLatest { observeRecentEarthquakes(it) },
        refreshStatus,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EarthquakeListUiState())

    fun onScreenStarted() {
        if (!isRestartAfterConfigurationChange) trackListViewed()
        isRestartAfterConfigurationChange = false
        viewModelScope.launch { refreshIfNeeded() }
    }

    fun onScreenStopped(isConfigurationChange: Boolean) {
        isRestartAfterConfigurationChange = isConfigurationChange
    }

    fun onRefresh() {
        viewModelScope.launch { refresh(RefreshTrigger.PULL) }
    }

    fun onRegionFilterSelected(region: RegionFilter) {
        if (requestedOptions.value.region == region) return
        requestedOptions.update { it.copy(region = region) }
        analyticsTracker.track(AnalyticsEvent.RegionFilterChanged(region.toAnalyticsValue()))
    }

    fun onMagnitudeFilterSelected(magnitude: MagnitudeFilter) {
        if (requestedOptions.value.magnitude == magnitude) return
        requestedOptions.update { it.copy(magnitude = magnitude) }
        analyticsTracker.track(AnalyticsEvent.MagnitudeFilterChanged(magnitude.toAnalyticsValue()))
    }

    fun onSortOrderSelected(sortOrder: EarthquakeSortOrder) {
        if (requestedOptions.value.sortOrder == sortOrder) return
        requestedOptions.update { it.copy(sortOrder = sortOrder) }
        analyticsTracker.track(AnalyticsEvent.ListSortChanged(sortOrder.toAnalyticsValue()))
    }

    fun onShowAllClicked() {
        onRegionFilterSelected(RegionFilter.WORLD)
        onMagnitudeFilterSelected(MagnitudeFilter.ALL)
    }

    private suspend fun refreshIfNeeded() {
        when (checkCacheFreshness()) {
            CacheFreshness.MISSING -> refresh(RefreshTrigger.INITIAL)
            CacheFreshness.STALE -> refresh(RefreshTrigger.STALE)
            CacheFreshness.FRESH -> Unit
        }
    }

    private suspend fun refresh(trigger: RefreshTrigger) {
        if (refreshStatus.value.isRefreshing) return
        refreshStatus.value = RefreshStatus(isRefreshing = true)
        val result: AppResult<Int> = refreshEarthquakes()
        refreshStatus.value = RefreshStatus(error = (result as? AppResult.Failure)?.error)
        trackRefreshed(trigger, result)
    }

    private fun trackRefreshed(trigger: RefreshTrigger, result: AppResult<Int>) {
        val event = AnalyticsEvent.EarthquakeListRefreshed(
            trigger = trigger,
            isSuccessful = result is AppResult.Success,
            count = (result as? AppResult.Success)?.data ?: uiState.value.cachedCount,
        )
        analyticsTracker.track(event)
    }

    private fun trackListViewed() {
        val options: EarthquakeListOptions = requestedOptions.value
        val event = AnalyticsEvent.EarthquakeListViewed(
            regionFilter = options.region.toAnalyticsValue(),
            magnitudeFilter = options.magnitude.toAnalyticsValue(),
            sortOrder = options.sortOrder.toAnalyticsValue(),
        )
        analyticsTracker.track(event)
    }

    private fun toUiState(recent: RecentEarthquakes, status: RefreshStatus): EarthquakeListUiState =
        EarthquakeListUiState(
            content = contentOf(recent, status),
            earthquakes = recent.earthquakes,
            cachedCount = recent.cachedCount,
            options = recent.appliedOptions,
            nearCityName = (recent.area as? AlertArea.AroundCity)?.city?.name,
            magnitudeThreshold = recent.magnitudeThreshold,
            lastRefreshedAt = recent.lastRefreshedAt,
            isRefreshing = status.isRefreshing,
            refreshError = status.error,
        )

    private fun contentOf(recent: RecentEarthquakes, status: RefreshStatus): EarthquakeListContent =
        when {
            recent.earthquakes.isNotEmpty() -> EarthquakeListContent.ITEMS
            recent.cachedCount > 0 -> EarthquakeListContent.EMPTY_FILTERED
            status.error != null -> EarthquakeListContent.ERROR
            recent.lastRefreshedAt == null -> EarthquakeListContent.LOADING
            else -> EarthquakeListContent.EMPTY
        }

    private fun RegionFilter.toAnalyticsValue(): RegionFilterValue =
        when (this) {
            RegionFilter.WORLD -> RegionFilterValue.WORLD
            RegionFilter.NEAR_CITY -> RegionFilterValue.NEAR_CITY
        }

    private fun MagnitudeFilter.toAnalyticsValue(): MagnitudeFilterValue =
        when (this) {
            MagnitudeFilter.ALL -> MagnitudeFilterValue.ALL
            MagnitudeFilter.ABOVE_THRESHOLD -> MagnitudeFilterValue.ABOVE_THRESHOLD
        }

    private fun EarthquakeSortOrder.toAnalyticsValue(): SortOrderValue =
        when (this) {
            EarthquakeSortOrder.NEWEST_FIRST -> SortOrderValue.NEWEST_FIRST
            EarthquakeSortOrder.LARGEST_FIRST -> SortOrderValue.LARGEST_FIRST
            EarthquakeSortOrder.NEAREST_FIRST -> SortOrderValue.NEAREST_FIRST
        }

    private data class RefreshStatus(
        val isRefreshing: Boolean = false,
        val error: AppError? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
