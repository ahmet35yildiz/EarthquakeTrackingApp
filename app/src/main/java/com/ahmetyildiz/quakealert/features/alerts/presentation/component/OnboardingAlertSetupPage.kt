package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

@Composable
fun OnboardingAlertSetupPage(
    threshold: Double,
    onThresholdChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    areaSelector: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
        Text(
            text = stringResource(R.string.onboarding_setup_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        MagnitudeThresholdSelector(threshold = threshold, onThresholdChange = onThresholdChanged)
        areaSelector()
    }
}

@PreviewLightDark
@Composable
private fun OnboardingAlertSetupPagePreview() {
    QuakeAlertTheme {
        Surface {
            OnboardingAlertSetupPage(
                threshold = AlertConfig.DEFAULT_THRESHOLD,
                onThresholdChanged = {},
                modifier = Modifier.padding(Spacing.large),
            ) {
                Text(text = "Area selector")
            }
        }
    }
}
