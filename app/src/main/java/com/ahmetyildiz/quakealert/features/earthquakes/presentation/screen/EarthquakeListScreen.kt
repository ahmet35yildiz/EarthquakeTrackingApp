package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.component.EmptyState
import com.ahmetyildiz.quakealert.core.ui.component.ErrorState
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.component.StaleDataBanner
import com.ahmetyildiz.quakealert.core.ui.component.StateAction
import com.ahmetyildiz.quakealert.core.ui.format.formatDataTime
import com.ahmetyildiz.quakealert.core.ui.format.rememberCurrentTime
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.EarthquakeFilterChips
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.EarthquakeListItem
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.EarthquakeListTopBar
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.EarthquakeSortMenu
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeListContent
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeListUiState
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeListViewModel
import java.time.Duration
import java.time.Instant

data class EarthquakeListActions(
    val onRefresh: () -> Unit,
    val onRegionFilterSelected: (RegionFilter) -> Unit,
    val onMagnitudeFilterSelected: (MagnitudeFilter) -> Unit,
    val onSortOrderSelected: (EarthquakeSortOrder) -> Unit,
    val onShowAllClicked: () -> Unit,
    val onEarthquakeClick: (String) -> Unit,
)

@Composable
fun EarthquakeListEntry(
    onEarthquakeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EarthquakeListViewModel = hiltViewModel(),
) {
    val uiState: EarthquakeListUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    LifecycleStartEffect(viewModel) {
        viewModel.onScreenStarted()
        onStopOrDispose {
            viewModel.onScreenStopped(isConfigurationChange = activity?.isChangingConfigurations == true)
        }
    }
    val actions = EarthquakeListActions(
        onRefresh = viewModel::onRefresh,
        onRegionFilterSelected = viewModel::onRegionFilterSelected,
        onMagnitudeFilterSelected = viewModel::onMagnitudeFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onShowAllClicked = viewModel::onShowAllClicked,
        onEarthquakeClick = onEarthquakeClick,
    )
    EarthquakeListScreen(uiState = uiState, actions = actions, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListScreen(
    uiState: EarthquakeListUiState,
    actions: EarthquakeListActions,
    modifier: Modifier = Modifier,
    now: Instant = rememberCurrentTime(),
) {
    Column(modifier = modifier.fillMaxSize()) {
        EarthquakeListTopBar(
            lastRefreshedAt = uiState.lastRefreshedAt,
            now = now,
            isRefreshEnabled = !uiState.isRefreshing,
            onRefresh = actions.onRefresh,
        )
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing && uiState.content != EarthquakeListContent.LOADING,
            onRefresh = actions.onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (uiState.content) {
                EarthquakeListContent.LOADING -> LoadingState()
                EarthquakeListContent.ERROR -> ErrorState(
                    title = stringResource(R.string.earthquake_list_error_title),
                    message = errorMessage(uiState.refreshError),
                    onRetry = actions.onRefresh,
                )
                EarthquakeListContent.EMPTY -> EmptyState(
                    title = stringResource(R.string.earthquake_list_empty_title),
                    message = stringResource(R.string.earthquake_list_empty_message),
                )
                EarthquakeListContent.EMPTY_FILTERED,
                EarthquakeListContent.ITEMS -> EarthquakeList(uiState = uiState, actions = actions, now = now)
            }
        }
    }
}

@Composable
private fun EarthquakeList(uiState: EarthquakeListUiState, actions: EarthquakeListActions, now: Instant) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.isShowingStaleData) {
            StaleDataHeader(
                uiState = uiState,
                now = now,
                onRetry = actions.onRefresh,
                modifier = Modifier.padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            )
        }
        EarthquakeFilterChips(
            options = uiState.options,
            nearCityName = uiState.nearCityName,
            magnitudeThreshold = uiState.magnitudeThreshold,
            onRegionFilterSelected = actions.onRegionFilterSelected,
            onMagnitudeFilterSelected = actions.onMagnitudeFilterSelected,
            modifier = Modifier.padding(horizontal = Spacing.screenMargin),
        )
        if (uiState.content == EarthquakeListContent.EMPTY_FILTERED) {
            FilteredEmptyState(uiState = uiState, onShowAll = actions.onShowAllClicked, modifier = Modifier.weight(1f))
        } else {
            ListHeader(
                uiState = uiState,
                onSortOrderSelected = actions.onSortOrderSelected,
                modifier = Modifier.padding(start = Spacing.screenMargin, end = Spacing.small),
            )
            EarthquakeItems(uiState = uiState, actions = actions, now = now, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EarthquakeItems(
    uiState: EarthquakeListUiState,
    actions: EarthquakeListActions,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    val listState: LazyListState = rememberSaveable(uiState.options, saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = Spacing.screenMargin, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        items(items = uiState.earthquakes, key = { it.earthquake.id }) { item ->
            EarthquakeListItem(
                item = item,
                cityName = uiState.nearCityName,
                now = now,
                onClick = { actions.onEarthquakeClick(item.earthquake.id) },
            )
        }
    }
}

@Composable
private fun StaleDataHeader(
    uiState: EarthquakeListUiState,
    now: Instant,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dataTime: String = uiState.lastRefreshedAt?.let { formatDataTime(it, now) }.orEmpty()
    val messageRes: Int = if (uiState.refreshError == AppError.Network) {
        R.string.stale_data_offline_message
    } else {
        R.string.stale_data_failed_message
    }
    StaleDataBanner(message = stringResource(messageRes, dataTime), onRetry = onRetry, modifier = modifier)
}

@Composable
private fun ListHeader(
    uiState: EarthquakeListUiState,
    onSortOrderSelected: (EarthquakeSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count: Int = uiState.earthquakes.size
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = pluralStringResource(R.plurals.earthquake_count, count, count),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        EarthquakeSortMenu(
            selected = uiState.options.sortOrder,
            isNearestAvailable = uiState.isNearestSortAvailable,
            onSortOrderSelected = onSortOrderSelected,
        )
    }
}

@Composable
private fun FilteredEmptyState(uiState: EarthquakeListUiState, onShowAll: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        title = filteredEmptyTitle(uiState),
        modifier = modifier,
        action = StateAction(label = stringResource(R.string.action_show_all_earthquakes), onClick = onShowAll),
    )
}

@Composable
private fun filteredEmptyTitle(uiState: EarthquakeListUiState): String {
    val days: Int = EarthquakesConfig.RECENT_PERIOD.toDays().toInt()
    val threshold: String = stringResource(R.string.magnitude_value, uiState.magnitudeThreshold)
    val city: String = uiState.nearCityName.orEmpty()
    val isNearCity: Boolean = uiState.options.region == RegionFilter.NEAR_CITY
    val isAboveThreshold: Boolean = uiState.options.magnitude == MagnitudeFilter.ABOVE_THRESHOLD
    return when {
        isNearCity && isAboveThreshold ->
            pluralStringResource(R.plurals.earthquake_list_empty_near_above, days, threshold, city, days)
        isNearCity -> pluralStringResource(R.plurals.earthquake_list_empty_near, days, city, days)
        else -> pluralStringResource(R.plurals.earthquake_list_empty_above, days, threshold, days)
    }
}

@Composable
private fun errorMessage(error: AppError?): String =
    if (error == AppError.Network) {
        stringResource(R.string.error_message_network)
    } else {
        stringResource(R.string.error_message_generic)
    }

private val PreviewNow: Instant = Instant.parse("2026-09-25T12:00:00Z")

private val PreviewActions = EarthquakeListActions({}, {}, {}, {}, {}, {})

private fun previewEarthquake(id: String, magnitude: Double, place: String, hoursAgo: Long): EarthquakeWithDistance =
    EarthquakeWithDistance(
        earthquake = Earthquake(
            id = id,
            magnitude = Magnitude(value = magnitude, type = "mb"),
            place = place,
            time = PreviewNow.minus(Duration.ofHours(hoursAgo)),
            location = GeoPoint(38.0, 27.0),
            depthKm = 10.0,
            detailUrl = "",
            isReviewed = true,
            hasTsunamiFlag = false,
            feltReportCount = null,
        ),
        distanceKm = 41.0,
    )

@PreviewLightDark
@Composable
private fun EarthquakeListScreenItemsPreview() {
    val earthquakes: List<EarthquakeWithDistance> = listOf(
        previewEarthquake(id = "a", magnitude = 5.4, place = "87 km E of Hualien City, Taiwan", hoursAgo = 1),
        previewEarthquake(id = "b", magnitude = 4.6, place = "12 km SW of Seferihisar, Turkey", hoursAgo = 3),
        previewEarthquake(id = "c", magnitude = 2.7, place = "21 km S of Volcano, Hawaii", hoursAgo = 30),
    )
    QuakeAlertTheme {
        Surface {
            EarthquakeListScreen(
                uiState = EarthquakeListUiState(
                    content = EarthquakeListContent.ITEMS,
                    earthquakes = earthquakes,
                    cachedCount = earthquakes.size,
                    options = EarthquakeListOptions(
                        region = RegionFilter.NEAR_CITY,
                        sortOrder = EarthquakeSortOrder.NEAREST_FIRST,
                    ),
                    nearCityName = "Izmir",
                    lastRefreshedAt = PreviewNow.minus(Duration.ofMinutes(2)),
                    refreshError = AppError.Network,
                ),
                actions = PreviewActions,
                now = PreviewNow,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EarthquakeListScreenFilteredEmptyPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeListScreen(
                uiState = EarthquakeListUiState(
                    content = EarthquakeListContent.EMPTY_FILTERED,
                    cachedCount = 12,
                    options = EarthquakeListOptions(
                        region = RegionFilter.NEAR_CITY,
                        magnitude = MagnitudeFilter.ABOVE_THRESHOLD,
                    ),
                    nearCityName = "Izmir",
                    lastRefreshedAt = PreviewNow,
                ),
                actions = PreviewActions,
                now = PreviewNow,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EarthquakeListScreenErrorPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeListScreen(
                uiState = EarthquakeListUiState(content = EarthquakeListContent.ERROR, refreshError = AppError.Network),
                actions = PreviewActions,
                now = PreviewNow,
            )
        }
    }
}
