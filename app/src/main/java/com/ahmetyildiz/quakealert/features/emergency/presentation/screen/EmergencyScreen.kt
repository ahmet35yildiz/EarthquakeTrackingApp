package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.NavigationCard
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onOpenSafetyGuide: () -> Unit, modifier: Modifier = Modifier) {
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
            NavigationCard(
                title = stringResource(R.string.safety_guide_title),
                description = stringResource(R.string.safety_guide_card_description),
                icon = rememberVectorPainter(Icons.Rounded.Info),
                onClick = onOpenSafetyGuide,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EmergencyScreenPreview() {
    QuakeAlertTheme {
        Surface {
            EmergencyScreen(onOpenSafetyGuide = {})
        }
    }
}
