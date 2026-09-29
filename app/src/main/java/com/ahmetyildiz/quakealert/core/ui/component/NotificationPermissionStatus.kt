package com.ahmetyildiz.quakealert.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun NotificationPermissionStatus(
    access: NotificationAccess,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAllowed: Boolean = access.isAllowed
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium), horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(
                        if (isAllowed) R.drawable.ic_notifications_filled else R.drawable.ic_notifications,
                    ),
                    contentDescription = null,
                    tint = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                PermissionTexts(access = access, modifier = Modifier.weight(1f))
            }
            if (!isAllowed) {
                FilledTonalButton(onClick = onOpenSettings) { Text(text = stringResource(R.string.action_open_settings)) }
            }
        }
    }
}

@Composable
private fun PermissionTexts(access: NotificationAccess, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = stringResource(access.titleRes), style = MaterialTheme.typography.titleSmall)
        blockedMessageOrNull(access)?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val NotificationAccess.titleRes: Int
    get() = when (this) {
        NotificationAccess.ALLOWED -> R.string.notifications_allowed
        NotificationAccess.APP_BLOCKED -> R.string.notifications_blocked
        NotificationAccess.ALERT_CHANNEL_BLOCKED -> R.string.notifications_channel_blocked
    }

@Composable
private fun blockedMessageOrNull(access: NotificationAccess): String? =
    when (access) {
        NotificationAccess.ALLOWED -> null
        NotificationAccess.APP_BLOCKED -> stringResource(R.string.notifications_blocked_message)
        NotificationAccess.ALERT_CHANNEL_BLOCKED -> alertChannelBlockedMessage()
    }

@Composable
fun alertChannelBlockedMessage(): String =
    stringResource(R.string.notifications_channel_blocked_message, stringResource(R.string.notification_channel_alerts_name))

@PreviewLightDark
@Composable
private fun NotificationPermissionStatusPreview() {
    QuakeAlertTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                NotificationAccess.entries.forEach { NotificationPermissionStatus(access = it, onOpenSettings = {}) }
            }
        }
    }
}
