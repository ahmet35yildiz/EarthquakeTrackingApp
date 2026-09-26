package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.NotificationPermissionStatus
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.format.relativeTimeText
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import java.time.Duration
import java.time.Instant

@Composable
fun AlertStatusCard(
    areNotificationsAllowed: Boolean,
    lastCheckedAt: Instant?,
    now: Instant,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.alert_status_title),
        icon = rememberVectorPainter(Icons.Rounded.Info),
        modifier = modifier,
    ) {
        NotificationPermissionStatus(isAllowed = areNotificationsAllowed, onOpenSettings = onOpenNotificationSettings)
        Text(
            text = lastCheckedAt?.let { stringResource(R.string.last_checked, relativeTimeText(it, now)) }
                ?: stringResource(R.string.last_checked_never),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = checkIntervalText(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun checkIntervalText(): String {
    val minutes: Int = AlertConfig.CHECK_INTERVAL.toMinutes().toInt()
    return pluralStringResource(R.plurals.alert_check_interval_info, minutes, minutes)
}

@PreviewLightDark
@Composable
private fun AlertStatusCardPreview() {
    val now: Instant = Instant.parse("2026-09-26T12:00:00Z")
    QuakeAlertTheme {
        Surface {
            AlertStatusCard(
                areNotificationsAllowed = false,
                lastCheckedAt = now - Duration.ofMinutes(6),
                now = now,
                onOpenNotificationSettings = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
