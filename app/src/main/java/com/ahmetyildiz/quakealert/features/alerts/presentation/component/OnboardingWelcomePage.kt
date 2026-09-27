package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun OnboardingWelcomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingHeroIllustration(icon = painterResource(R.drawable.ic_waves))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OnboardingFeatureCard(
            icon = painterResource(R.drawable.ic_waves),
            title = stringResource(R.string.onboarding_feature_list_title),
            message = stringResource(R.string.onboarding_feature_list_message),
        )
        OnboardingFeatureCard(
            icon = rememberVectorPainter(Icons.Rounded.Place),
            title = stringResource(R.string.onboarding_feature_filter_title),
            message = stringResource(R.string.onboarding_feature_filter_message),
        )
        OnboardingFeatureCard(
            icon = painterResource(R.drawable.ic_notifications),
            title = stringResource(R.string.onboarding_feature_alert_title),
            message = stringResource(R.string.onboarding_feature_alert_message),
        )
        DisclaimerCard()
    }
}

@Composable
private fun DisclaimerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Text(text = stringResource(R.string.onboarding_disclaimer), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingWelcomePagePreview() {
    QuakeAlertTheme {
        Surface { OnboardingWelcomePage(modifier = Modifier.padding(Spacing.large)) }
    }
}
