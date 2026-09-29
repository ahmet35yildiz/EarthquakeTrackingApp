package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.EmptyState
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeStatisticsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { ScreenTitle(text = stringResource(R.string.tab_statistics)) }) },
    ) { innerPadding ->
        EmptyState(
            title = stringResource(R.string.statistics_placeholder_title),
            message = stringResource(R.string.statistics_placeholder_message),
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@PreviewLightDark
@Composable
private fun EarthquakeStatisticsScreenPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeStatisticsScreen()
        }
    }
}
