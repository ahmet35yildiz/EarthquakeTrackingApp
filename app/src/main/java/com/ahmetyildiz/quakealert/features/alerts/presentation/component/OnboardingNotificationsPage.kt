package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

@Composable
fun OnboardingNotificationsPage(
    areNotificationsAllowed: Boolean,
    isPermissionDenied: Boolean,
    modifier: Modifier = Modifier,
) {
    val minutes: Int = AlertConfig.CHECK_INTERVAL.toMinutes().toInt()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingHeroIcon(icon = painterResource(R.drawable.ic_notifications))
        Text(
            text = stringResource(R.string.onboarding_notifications_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = pluralStringResource(R.plurals.onboarding_notifications_message, minutes, minutes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        when {
            areNotificationsAllowed -> AllowedRow()
            isPermissionDenied -> Text(
                text = stringResource(R.string.onboarding_notifications_denied),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AllowedRow() {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = stringResource(R.string.notifications_allowed), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingNotificationsPagePreview() {
    QuakeAlertTheme {
        Surface {
            OnboardingNotificationsPage(
                areNotificationsAllowed = false,
                isPermissionDenied = true,
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
