package com.ahmetyildiz.quakealert.features.alerts.presentation.screen

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.navigation.notificationSettingsIntent
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.AreaSelectorEntry
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.OnboardingAlertSetupPage
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.OnboardingNotificationsPage
import com.ahmetyildiz.quakealert.features.alerts.presentation.component.OnboardingWelcomePage
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingPage
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.OnboardingViewModel

data class OnboardingActions(
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onThresholdChanged: (Double) -> Unit,
    val onAreaSelectionChanged: (AreaSelection) -> Unit,
    val onAllowNotifications: () -> Unit,
    val onOpenNotificationSettings: () -> Unit,
    val onFinish: () -> Unit,
)

@Composable
fun OnboardingEntry(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState: OnboardingUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCompleted: Boolean by viewModel.isCompleted.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onPermissionResult,
    )
    LaunchedEffect(isCompleted) {
        if (isCompleted) onFinished()
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenResumed()
        onPauseOrDispose {}
    }
    BackHandler(enabled = uiState.page != OnboardingPage.WELCOME, onBack = viewModel::onBack)
    val actions = OnboardingActions(
        onNext = viewModel::onNext,
        onBack = viewModel::onBack,
        onThresholdChanged = viewModel::onThresholdChanged,
        onAreaSelectionChanged = viewModel::onAreaSelectionChanged,
        onAllowNotifications = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewModel.onPermissionRequested()
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onOpenNotificationSettings = { context.startActivity(notificationSettingsIntent(context)) },
        onFinish = viewModel::onFinish,
    )
    OnboardingScreen(
        uiState = uiState,
        actions = actions,
        isPermissionRequestSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        modifier = modifier,
    ) {
        AreaSelectorEntry(selection = uiState.areaSelection, onSelectionChange = actions.onAreaSelectionChanged)
    }
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    actions: OnboardingActions,
    isPermissionRequestSupported: Boolean,
    modifier: Modifier = Modifier,
    areaSelector: @Composable () -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.safeDrawingPadding().imePadding()) {
            StepIndicator(page = uiState.page, onBack = actions.onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenMargin, vertical = Spacing.large),
            ) {
                PageContent(uiState = uiState, actions = actions, areaSelector = areaSelector)
            }
            BottomActions(uiState = uiState, actions = actions, isPermissionRequestSupported = isPermissionRequestSupported)
        }
    }
}

@Composable
private fun StepIndicator(page: OnboardingPage, onBack: () -> Unit) {
    val pageCount: Int = OnboardingPage.entries.size
    Column(modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (page != OnboardingPage.WELCOME) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
            Text(
                text = stringResource(R.string.onboarding_step_indicator, page.ordinal + 1, pageCount),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(Spacing.medium),
            )
        }
        LinearProgressIndicator(
            progress = { (page.ordinal + 1).toFloat() / pageCount },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.small),
        )
    }
}

@Composable
private fun PageContent(uiState: OnboardingUiState, actions: OnboardingActions, areaSelector: @Composable () -> Unit) {
    when (uiState.page) {
        OnboardingPage.WELCOME -> OnboardingWelcomePage()
        OnboardingPage.ALERT_SETUP -> OnboardingAlertSetupPage(
            threshold = uiState.threshold,
            onThresholdChanged = actions.onThresholdChanged,
            areaSelector = areaSelector,
        )
        OnboardingPage.NOTIFICATIONS -> OnboardingNotificationsPage(
            areNotificationsAllowed = uiState.areNotificationsAllowed,
            isPermissionDenied = uiState.isPermissionDenied,
        )
    }
}

@Composable
private fun BottomActions(uiState: OnboardingUiState, actions: OnboardingActions, isPermissionRequestSupported: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenMargin, vertical = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (uiState.page) {
            OnboardingPage.WELCOME -> PrimaryButton(R.string.action_get_started, onClick = actions.onNext)
            OnboardingPage.ALERT_SETUP -> AlertSetupActions(isEnabled = uiState.canLeaveAlertSetup, onNext = actions.onNext)
            OnboardingPage.NOTIFICATIONS -> NotificationActions(uiState, actions, isPermissionRequestSupported)
        }
    }
}

@Composable
private fun AlertSetupActions(isEnabled: Boolean, onNext: () -> Unit) {
    if (!isEnabled) {
        Text(
            text = stringResource(R.string.onboarding_setup_city_required),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    PrimaryButton(R.string.action_next, onClick = onNext, isEnabled = isEnabled)
}

@Composable
private fun NotificationActions(uiState: OnboardingUiState, actions: OnboardingActions, isPermissionRequestSupported: Boolean) {
    val isEnabled: Boolean = !uiState.isFinishing
    val canRequest: Boolean = isPermissionRequestSupported && !uiState.areNotificationsAllowed && !uiState.isPermissionDenied
    if (canRequest) {
        PrimaryButton(R.string.action_allow_notifications, onClick = actions.onAllowNotifications, isEnabled = isEnabled)
        SecondaryButton(R.string.action_not_now, onClick = actions.onFinish, isEnabled = isEnabled)
        return
    }
    PrimaryButton(R.string.action_finish, onClick = actions.onFinish, isEnabled = isEnabled)
    if (!uiState.areNotificationsAllowed) {
        SecondaryButton(R.string.action_open_settings, onClick = actions.onOpenNotificationSettings, isEnabled = isEnabled)
    }
}

@Composable
private fun PrimaryButton(labelRes: Int, onClick: () -> Unit, isEnabled: Boolean = true) {
    Button(onClick = onClick, enabled = isEnabled, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(labelRes))
    }
}

@Composable
private fun SecondaryButton(labelRes: Int, onClick: () -> Unit, isEnabled: Boolean) {
    TextButton(onClick = onClick, enabled = isEnabled) { Text(text = stringResource(labelRes)) }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenPreview() {
    QuakeAlertTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(page = OnboardingPage.NOTIFICATIONS),
            actions = OnboardingActions({}, {}, {}, {}, {}, {}, {}),
            isPermissionRequestSupported = true,
        ) {}
    }
}
