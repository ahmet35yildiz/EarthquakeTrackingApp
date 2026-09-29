package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.analytics.DetailAction
import com.ahmetyildiz.quakealert.core.analytics.DetailSource
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.navigation.browserIntent
import com.ahmetyildiz.quakealert.core.navigation.shareIntent
import com.ahmetyildiz.quakealert.core.navigation.tryStartActivity
import com.ahmetyildiz.quakealert.core.ui.component.EmptyState
import com.ahmetyildiz.quakealert.core.ui.component.ErrorState
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.component.StaleDataBanner
import com.ahmetyildiz.quakealert.core.ui.format.formatLocalDateTime
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.format.localizedPlace
import com.ahmetyildiz.quakealert.core.ui.format.rememberCurrentTime
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.DetailFactsCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.DetailHeaderCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.component.DistanceCard
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeDetailContent
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeDetailUiState
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel.EarthquakeDetailViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class EarthquakeDetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onOpenInMaps: () -> Unit,
    val onViewOnUsgs: () -> Unit,
    val onShare: () -> Unit,
    val onReportFelt: () -> Unit,
)

@Composable
fun EarthquakeDetailEntry(
    earthquakeId: String,
    source: DetailSource,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    feedback: @Composable () -> Unit = {},
    viewModel: EarthquakeDetailViewModel = hiltViewModel<EarthquakeDetailViewModel, EarthquakeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(earthquakeId, source) },
    ),
) {
    val uiState: EarthquakeDetailUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noAppMessage: String = stringResource(R.string.error_no_app_for_action)
    val intents: DetailIntents? = uiState.details?.let { rememberDetailIntents(it) }
    val open: (Intent?) -> Unit = { intent ->
        if (intent != null && !context.tryStartActivity(intent)) {
            scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
        }
    }
    val launch: (DetailAction, Intent?) -> Unit = { action, intent ->
        viewModel.onActionClicked(action)
        open(intent)
    }
    val actions = EarthquakeDetailActions(
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onOpenInMaps = { launch(DetailAction.MAP, intents?.maps) },
        onViewOnUsgs = { launch(DetailAction.USGS, intents?.usgs) },
        onShare = { launch(DetailAction.SHARE, intents?.share) },
        onReportFelt = {
            viewModel.onFeltReported()
            open(intents?.feltReport)
        },
    )
    EarthquakeDetailScreen(
        uiState = uiState,
        actions = actions,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        feedback = feedback,
    )
}

private data class DetailIntents(
    val maps: Intent,
    val usgs: Intent,
    val share: Intent,
    val feltReport: Intent,
)

@Composable
private fun rememberDetailIntents(details: EarthquakeDetails): DetailIntents {
    val earthquake: Earthquake = details.earthquake
    val place: String = localizedPlace(earthquake.place)
    val shareText: String = shareText(earthquake, place)
    val chooserTitle: String = stringResource(R.string.share_chooser_title)
    return remember(details, shareText) {
        DetailIntents(
            maps = mapsIntent(earthquake.location, place),
            usgs = browserIntent(earthquake.detailUrl),
            share = shareIntent(shareText, chooserTitle),
            feltReport = browserIntent(feltReportUrl(earthquake.detailUrl)),
        )
    }
}

@Composable
private fun shareText(earthquake: Earthquake, place: String): String {
    val magnitude: String = earthquake.magnitude
        ?.let { stringResource(R.string.magnitude_value, it.value) }
        ?: stringResource(R.string.magnitude_unknown_value)
    val depth: String = formatWholeNumber(earthquake.depthKm.coerceAtLeast(0.0))
    return stringResource(
        R.string.share_text,
        magnitude,
        place,
        formatLocalDateTime(earthquake.time),
        depth,
        earthquake.detailUrl,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeDetailScreen(
    uiState: EarthquakeDetailUiState,
    actions: EarthquakeDetailActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    now: Instant = rememberCurrentTime(),
    feedback: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { ScreenTitle(text = stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (uiState.content == EarthquakeDetailContent.LOADED) {
                        IconButton(onClick = actions.onShare) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.action_share),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier: Modifier = Modifier.padding(innerPadding)
        when (uiState.content) {
            EarthquakeDetailContent.LOADING -> LoadingState(modifier = contentModifier)
            EarthquakeDetailContent.NOT_FOUND -> EmptyState(
                title = stringResource(R.string.detail_not_found_title),
                message = stringResource(R.string.detail_not_found_message),
                modifier = contentModifier,
            )
            EarthquakeDetailContent.ERROR -> ErrorState(
                title = stringResource(R.string.detail_error_title),
                message = errorMessage(uiState.error),
                onRetry = actions.onRetry,
                modifier = contentModifier,
            )
            EarthquakeDetailContent.LOADED -> uiState.details?.let {
                DetailContent(details = it, actions = actions, now = now, feedback = feedback, modifier = contentModifier)
            }
        }
    }
}

@Composable
private fun DetailContent(
    details: EarthquakeDetails,
    actions: EarthquakeDetailActions,
    now: Instant,
    feedback: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        if (details.isSavedCopyAfterFailedRefresh) {
            StaleDataBanner(message = stringResource(R.string.detail_saved_copy_message), onRetry = actions.onRetry)
        }
        DetailHeaderCard(earthquake = details.earthquake, now = now)
        details.distanceFromCity?.let { DistanceCard(distance = it) }
        feedback()
        DetailFactsCard(earthquake = details.earthquake)
        Button(onClick = actions.onOpenInMaps, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = stringResource(R.string.action_open_in_maps),
                modifier = Modifier.padding(start = Spacing.small),
            )
        }
        OutlinedButton(onClick = actions.onReportFelt, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_report_felt))
        }
        OutlinedButton(onClick = actions.onViewOnUsgs, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_view_on_usgs))
        }
        Text(
            text = stringResource(R.string.detail_data_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.small),
        )
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

private val PreviewActions = EarthquakeDetailActions({}, {}, {}, {}, {}, {})

@PreviewLightDark
@Composable
private fun EarthquakeDetailScreenPreview() {
    val earthquake = Earthquake(
        id = "preview",
        magnitude = Magnitude(value = 4.6, type = "mww"),
        place = "12 km SW of Seferihisar, Turkey",
        time = PreviewNow.minus(Duration.ofHours(3)),
        location = GeoPoint(38.21, 26.74),
        depthKm = 9.0,
        detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/preview",
        isReviewed = true,
        hasTsunamiFlag = false,
        feltReportCount = 23,
    )
    val distance = DistanceFromCity(
        cityName = "Izmir",
        distanceKm = 41.0,
        alertRadiusKm = 250,
        isWithinAlertArea = true,
    )
    QuakeAlertTheme {
        Surface {
            EarthquakeDetailScreen(
                uiState = EarthquakeDetailUiState(
                    content = EarthquakeDetailContent.LOADED,
                    details = EarthquakeDetails(earthquake, distance),
                ),
                actions = PreviewActions,
                now = PreviewNow,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EarthquakeDetailScreenNotFoundPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeDetailScreen(
                uiState = EarthquakeDetailUiState(content = EarthquakeDetailContent.NOT_FOUND),
                actions = PreviewActions,
                now = PreviewNow,
            )
        }
    }
}
