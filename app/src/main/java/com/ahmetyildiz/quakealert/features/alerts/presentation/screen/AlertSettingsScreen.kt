package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.format.rememberCurrentTime
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AlertStatusCard
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AlertsSummaryCard
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AreaSelectorEntry
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.MagnitudeThresholdSelector
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertSettingsUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertSettingsViewModel
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import java.time.Instant

data class AlertSettingsActions(
    val onAlertsToggled: (Boolean) -> Unit,
    val onThresholdChanged: (Double) -> Unit,
    val onAreaSelectionChanged: (AreaSelection) -> Unit,
    val onOpenNotificationSettings: () -> Unit,
)

@Composable
fun AlertSettingsEntry(
    modifier: Modifier = Modifier,
    viewModel: AlertSettingsViewModel = hiltViewModel(),
) {
    val uiState: AlertSettingsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val savedMessage: String = stringResource(R.string.alert_settings_saved)
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenResumed()
        onPauseOrDispose {}
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = savedMessage, duration = SnackbarDuration.Short)
        }
    }
    val actions = AlertSettingsActions(
        onAlertsToggled = viewModel::onAlertsToggled,
        onThresholdChanged = viewModel::onThresholdChanged,
        onAreaSelectionChanged = viewModel::onAreaSelectionChanged,
        onOpenNotificationSettings = { context.startActivity(notificationSettingsIntent(context)) },
    )
    AlertSettingsScreen(uiState = uiState, actions = actions, snackbarHostState = snackbarHostState, modifier = modifier) {
        AreaSelectorEntry(selection = uiState.areaSelection, onSelectionChange = actions.onAreaSelectionChanged)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(
    uiState: AlertSettingsUiState,
    actions: AlertSettingsActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    now: Instant = rememberCurrentTime(),
    areaSelector: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.tab_alerts)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            AlertsSummaryCard(settings = uiState.settings, onAlertsToggled = actions.onAlertsToggled)
            MagnitudeThresholdSelector(
                threshold = uiState.settings.magnitudeThreshold,
                onThresholdChange = actions.onThresholdChanged,
            )
            areaSelector()
            AlertStatusCard(
                areNotificationsAllowed = uiState.areNotificationsAllowed,
                lastCheckedAt = uiState.lastCheckedAt,
                now = now,
                onOpenNotificationSettings = actions.onOpenNotificationSettings,
            )
        }
    }
}

private fun notificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
