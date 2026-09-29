package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.navigation.notificationSettingsIntent
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AlertPreviewEntry
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AlertsSummaryActions
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AlertsSummaryCard
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AreaSelectorEntry
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.MagnitudeThresholdSelector
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertSettingsUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertSettingsViewModel
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection

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
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenResumed()
        onPauseOrDispose {}
    }
    val actions = AlertSettingsActions(
        onAlertsToggled = viewModel::onAlertsToggled,
        onThresholdChanged = viewModel::onThresholdChanged,
        onAreaSelectionChanged = viewModel::onAreaSelectionChanged,
        onOpenNotificationSettings = {
            context.startActivity(notificationSettingsIntent(context, uiState.notificationAccess))
        },
    )
    AlertSettingsScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
        alertPreview = { AlertPreviewEntry(threshold = it, area = uiState.areaSelection.toAlertAreaOrNull()) },
    ) {
        AreaSelectorEntry(selection = uiState.areaSelection, onSelectionChange = actions.onAreaSelectionChanged)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(
    uiState: AlertSettingsUiState,
    actions: AlertSettingsActions,
    modifier: Modifier = Modifier,
    alertPreview: @Composable (threshold: Double) -> Unit = {},
    areaSelector: @Composable () -> Unit,
) {
    val savedThreshold: Double = uiState.settings.magnitudeThreshold
    var previewThreshold: Double by remember(savedThreshold) { mutableDoubleStateOf(savedThreshold) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { ScreenTitle(text = stringResource(R.string.tab_alerts)) }) },
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
            AlertsSummaryCard(
                settings = uiState.settings,
                notificationAccess = uiState.notificationAccess,
                actions = AlertsSummaryActions(
                    onAlertsToggled = actions.onAlertsToggled,
                    onAllowNotifications = actions.onOpenNotificationSettings,
                ),
            )
            MagnitudeThresholdSelector(
                threshold = savedThreshold,
                onThresholdChange = actions.onThresholdChanged,
                onThresholdDragged = { previewThreshold = it },
            )
            areaSelector()
            alertPreview(previewThreshold)
        }
    }
}
