package com.ahmetyildiz.quakealert.features.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.settings.presentation.component.SettingsNavigationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperToolsScreen(
    onBack: () -> Unit,
    onOpenEventLog: () -> Unit,
    modifier: Modifier = Modifier,
    alertTools: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.developer_tools_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            SettingsNavigationCard(
                title = stringResource(R.string.event_log_title),
                description = stringResource(R.string.event_log_description),
                icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.List),
                onClick = onOpenEventLog,
            )
            alertTools()
        }
    }
}
