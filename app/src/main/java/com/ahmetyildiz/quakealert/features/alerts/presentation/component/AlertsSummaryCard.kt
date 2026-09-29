package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

data class AlertsSummaryActions(
    val onAlertsToggled: (Boolean) -> Unit,
    val onAllowNotifications: () -> Unit,
)

@Composable
fun AlertsSummaryCard(
    settings: AlertSettings,
    notificationAccess: NotificationAccess,
    actions: AlertsSummaryActions,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            AlertsSwitchRow(isEnabled = settings.isEnabled, onAlertsToggled = actions.onAlertsToggled)
            if (settings.isEnabled && !notificationAccess.isAllowed) {
                NotificationsBlockedNotice(access = notificationAccess, onAllowNotifications = actions.onAllowNotifications)
            } else {
                Text(text = summaryText(settings), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AlertsSwitchRow(isEnabled: Boolean, onAlertsToggled: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isEnabled, role = Role.Switch, onValueChange = onAlertsToggled),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.alerts_switch_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = isEnabled, onCheckedChange = null)
    }
}

@Composable
private fun NotificationsBlockedNotice(access: NotificationAccess, onAllowNotifications: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium), horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                Icon(
                    painter = painterResource(R.drawable.ic_notifications),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    Text(text = stringResource(R.string.alerts_blocked_title), style = MaterialTheme.typography.titleSmall)
                    Text(text = blockedMessage(access), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                onClick = onAllowNotifications,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(text = stringResource(R.string.action_allow_notifications))
            }
        }
    }
}

@Composable
private fun blockedMessage(access: NotificationAccess): String =
    if (access == NotificationAccess.ALERT_CHANNEL_BLOCKED) {
        stringResource(R.string.alerts_blocked_channel_message, stringResource(R.string.notification_channel_alerts_name))
    } else {
        stringResource(R.string.alerts_blocked_message)
    }

@Composable
private fun summaryText(settings: AlertSettings): String {
    val magnitude: String = stringResource(R.string.magnitude_value, settings.magnitudeThreshold)
    return when {
        !settings.isEnabled -> stringResource(R.string.alerts_summary_off)
        settings.area is AlertArea.AroundCity -> stringResource(
            R.string.alerts_summary_city,
            magnitude,
            settings.area.radiusKm,
            settings.area.city.name,
        )
        else -> stringResource(R.string.alerts_summary_world, magnitude)
    }
}

private val previewSettings: AlertSettings = AlertSettings(
    isEnabled = true,
    magnitudeThreshold = 4.5,
    area = AlertArea.AroundCity(City("İzmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250),
)

@PreviewLightDark
@Composable
private fun AlertsSummaryCardPreview() {
    QuakeAlertTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                AlertsSummaryCard(
                    settings = previewSettings,
                    notificationAccess = NotificationAccess.ALLOWED,
                    actions = AlertsSummaryActions(onAlertsToggled = {}, onAllowNotifications = {}),
                )
                AlertsSummaryCard(
                    settings = previewSettings,
                    notificationAccess = NotificationAccess.ALERT_CHANNEL_BLOCKED,
                    actions = AlertsSummaryActions(onAlertsToggled = {}, onAllowNotifications = {}),
                )
            }
        }
    }
}
