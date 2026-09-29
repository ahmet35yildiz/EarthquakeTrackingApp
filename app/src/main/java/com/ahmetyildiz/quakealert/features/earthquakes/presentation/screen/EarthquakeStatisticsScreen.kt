package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.core.ui.component.EmptyState
import com.ahmetyildiz.quakealert.core.ui.component.ErrorState
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.format.rememberCurrentTime
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DailyCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.SeverityCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.DailyCountCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.MagnitudeDistributionCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.StatisticsFilterChips
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.StatisticsSummaryCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.TopRegionsCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeStatisticsUiState
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeStatisticsViewModel
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.StatisticsContent
import java.time.Instant
import java.time.LocalDate

data class EarthquakeStatisticsActions(
    val onPeriodSelected: (StatisticsPeriod) -> Unit,
    val onRegionSelected: (RegionFilter) -> Unit,
    val onRetry: () -> Unit,
    val onEarthquakeClick: (String) -> Unit,
)

@Composable
fun EarthquakeStatisticsEntry(
    onEarthquakeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EarthquakeStatisticsViewModel = hiltViewModel(),
) {
    val uiState: EarthquakeStatisticsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    LifecycleStartEffect(viewModel) {
        viewModel.onScreenStarted()
        onStopOrDispose {
            viewModel.onScreenStopped(isConfigurationChange = activity?.isChangingConfigurations == true)
        }
    }
    val actions = EarthquakeStatisticsActions(
        onPeriodSelected = viewModel::onPeriodSelected,
        onRegionSelected = viewModel::onRegionSelected,
        onRetry = viewModel::onRetry,
        onEarthquakeClick = onEarthquakeClick,
    )
    EarthquakeStatisticsScreen(uiState = uiState, actions = actions, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeStatisticsScreen(
    uiState: EarthquakeStatisticsUiState,
    actions: EarthquakeStatisticsActions,
    modifier: Modifier = Modifier,
    now: Instant = rememberCurrentTime(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { ScreenTitle(text = stringResource(R.string.tab_statistics)) }) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            StatisticsFilterChips(
                period = uiState.period,
                region = uiState.region,
                nearCityName = uiState.nearCityName,
                onPeriodSelected = actions.onPeriodSelected,
                onRegionSelected = actions.onRegionSelected,
                modifier = Modifier.padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            )
            StatisticsContentArea(uiState = uiState, actions = actions, now = now, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatisticsContentArea(
    uiState: EarthquakeStatisticsUiState,
    actions: EarthquakeStatisticsActions,
    now: Instant,
    modifier: Modifier = Modifier,
) {
    when (uiState.content) {
        StatisticsContent.LOADING -> LoadingState(modifier = modifier)
        StatisticsContent.ERROR -> ErrorState(
            title = stringResource(R.string.statistics_error_title),
            message = stringResource(
                if (uiState.error == AppError.Network) R.string.error_message_network else R.string.error_message_generic,
            ),
            onRetry = actions.onRetry,
            modifier = modifier,
        )
        StatisticsContent.EMPTY -> EmptyState(
            title = stringResource(R.string.statistics_empty_title),
            message = stringResource(R.string.statistics_empty_message),
            modifier = modifier,
        )
        StatisticsContent.LOADED -> uiState.statistics?.let {
            key(uiState.period, uiState.region) {
                StatisticsCards(statistics = it, now = now, onEarthquakeClick = actions.onEarthquakeClick, modifier = modifier)
            }
        }
    }
}

@Composable
private fun StatisticsCards(
    statistics: EarthquakeStatistics,
    now: Instant,
    onEarthquakeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        StatisticsSummaryCard(statistics = statistics, now = now, onEarthquakeClick = onEarthquakeClick)
        MagnitudeDistributionCard(severityCounts = statistics.severityCounts)
        DailyCountCard(dailyCounts = statistics.dailyCounts)
        if (statistics.topRegions.isNotEmpty()) TopRegionsCard(topRegions = statistics.topRegions)
    }
}

private val PreviewNow: Instant = Instant.parse("2026-09-29T12:00:00Z")

@PreviewLightDark
@Composable
private fun EarthquakeStatisticsScreenPreview() {
    val statistics = EarthquakeStatistics(
        totalCount = 320,
        largest = null,
        averageMagnitude = 3.4,
        severityCounts = MagnitudeSeverity.entries.mapIndexed { index, severity -> SeverityCount(severity, 200 / (index + 1)) },
        dailyCounts = (6 downTo 0).map { DailyCount(LocalDate.of(2026, 9, 29).minusDays(it.toLong()), 30 + it * 5) },
        topRegions = listOf(RegionCount("Alaska", 120), RegionCount("Indonesia", 30), RegionCount("Japan", 12)),
    )
    QuakeAlertTheme {
        Surface {
            EarthquakeStatisticsScreen(
                uiState = EarthquakeStatisticsUiState(content = StatisticsContent.LOADED, statistics = statistics),
                actions = EarthquakeStatisticsActions({}, {}, {}, {}),
                now = PreviewNow,
            )
        }
    }
}
