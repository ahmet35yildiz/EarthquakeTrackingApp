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
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun NotificationPermissionStatus(
    isAllowed: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                PermissionTexts(isAllowed = isAllowed, modifier = Modifier.weight(1f))
            }
            if (!isAllowed) {
                FilledTonalButton(onClick = onOpenSettings) { Text(text = stringResource(R.string.action_open_settings)) }
            }
        }
    }
}

@Composable
private fun PermissionTexts(isAllowed: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(if (isAllowed) R.string.notifications_allowed else R.string.notifications_blocked),
            style = MaterialTheme.typography.titleSmall,
        )
        if (!isAllowed) {
            Text(
                text = stringResource(R.string.notifications_blocked_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationPermissionStatusPreview() {
    QuakeAlertTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                NotificationPermissionStatus(isAllowed = true, onOpenSettings = {})
                NotificationPermissionStatus(isAllowed = false, onOpenSettings = {})
            }
        }
    }
}
