package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.NavigationCard
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.emergency.presentation.component.KeepScreenOnEffect
import com.ahmetyildiz.quakealert.features.emergency.presentation.component.StrobeLightCard
import com.ahmetyildiz.quakealert.features.emergency.presentation.component.WhistleCard
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.EmergencyUiState
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.EmergencyViewModel

data class EmergencyActions(
    val onWhistleToggled: () -> Unit,
    val onStrobeToggled: () -> Unit,
    val onOpenSafetyGuide: () -> Unit,
)

@Composable
fun EmergencyEntry(
    onOpenSafetyGuide: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmergencyViewModel = hiltViewModel(),
) {
    val uiState: EmergencyUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    LifecycleStartEffect(viewModel) {
        onStopOrDispose {
            viewModel.onScreenStopped(isConfigurationChange = activity?.isChangingConfigurations == true)
        }
    }
    KeepScreenOnEffect(isEnabled = uiState.isAnyToolOn)
    val actions = EmergencyActions(
        onWhistleToggled = viewModel::onWhistleToggled,
        onStrobeToggled = viewModel::onStrobeToggled,
        onOpenSafetyGuide = onOpenSafetyGuide,
    )
    EmergencyScreen(uiState = uiState, actions = actions, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(uiState: EmergencyUiState, actions: EmergencyActions, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { ScreenTitle(text = stringResource(R.string.emergency_title)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            WhistleCard(state = uiState.whistle, onToggle = actions.onWhistleToggled)
            StrobeLightCard(
                isAvailable = uiState.isStrobeAvailable,
                state = uiState.strobe,
                onToggle = actions.onStrobeToggled,
            )
            NavigationCard(
                title = stringResource(R.string.safety_guide_title),
                description = stringResource(R.string.safety_guide_card_description),
                icon = rememberVectorPainter(Icons.Rounded.Info),
                onClick = actions.onOpenSafetyGuide,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EmergencyScreenPreview() {
    QuakeAlertTheme {
        Surface {
            EmergencyScreen(
                uiState = EmergencyUiState(isStrobeAvailable = true),
                actions = EmergencyActions(onWhistleToggled = {}, onStrobeToggled = {}, onOpenSafetyGuide = {}),
            )
        }
    }
}
