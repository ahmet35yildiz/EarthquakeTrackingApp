package com.ahmetyildiz.quakealert.features.settings.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.navigation.browserIntent
import com.ahmetyildiz.quakealert.core.navigation.notificationSettingsIntent
import com.ahmetyildiz.quakealert.core.navigation.tryStartActivity
import com.ahmetyildiz.quakealert.core.ui.component.NotificationPermissionStatus
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.settings.SettingsConfig
import com.ahmetyildiz.quakealert.features.settings.presentation.component.AboutSection
import com.ahmetyildiz.quakealert.features.settings.presentation.component.LanguageSection
import com.ahmetyildiz.quakealert.features.settings.presentation.component.SettingsNavigationCard
import com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel.SettingsUiState
import com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

data class SettingsActions(
    val onLanguageSelected: (AppLanguage?) -> Unit,
    val onOpenNotificationSettings: () -> Unit,
    val onOpenUsgsWebsite: () -> Unit,
)

@Composable
fun SettingsEntry(
    modifier: Modifier = Modifier,
    onOpenDeveloperTools: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState: SettingsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noAppMessage: String = stringResource(R.string.error_no_app_for_action)
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenResumed()
        onPauseOrDispose {}
    }
    val actions = SettingsActions(
        onLanguageSelected = viewModel::onLanguageSelected,
        onOpenNotificationSettings = { context.startActivity(notificationSettingsIntent(context)) },
        onOpenUsgsWebsite = {
            if (!context.tryStartActivity(browserIntent(SettingsConfig.USGS_WEBSITE_URL))) {
                scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
            }
        },
    )
    SettingsScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onOpenDeveloperTools = onOpenDeveloperTools,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenDeveloperTools: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { ScreenTitle(text = stringResource(R.string.tab_settings)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            LanguageSection(
                languages = uiState.languages,
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = actions.onLanguageSelected,
            )
            SectionCard(
                title = stringResource(R.string.settings_notifications_title),
                icon = painterResource(R.drawable.ic_notifications),
            ) {
                NotificationPermissionStatus(
                    isAllowed = uiState.areNotificationsAllowed,
                    onOpenSettings = actions.onOpenNotificationSettings,
                )
            }
            AboutSection(appVersion = uiState.appVersion, onOpenUsgsWebsite = actions.onOpenUsgsWebsite)
            onOpenDeveloperTools?.let { onClick ->
                SettingsNavigationCard(
                    title = stringResource(R.string.developer_tools_title),
                    description = stringResource(R.string.settings_developer_tools_description),
                    icon = rememberVectorPainter(Icons.Rounded.Build),
                    onClick = onClick,
                )
            }
        }
    }
}
