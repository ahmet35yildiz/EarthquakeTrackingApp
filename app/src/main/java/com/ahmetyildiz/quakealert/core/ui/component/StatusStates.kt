package com.ahmetyildiz.quakealert.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

/** A button shown below a status message. */
data class StateAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description: String = stringResource(R.string.state_loading)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

/** Nothing to show, e.g. no earthquakes match the filters. The optional [action] is a quiet text button. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Rounded.Info,
    action: StateAction? = null,
) {
    MessageState(
        icon = icon,
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        title = title,
        message = message,
        modifier = modifier,
    ) {
        action?.let { TextButton(onClick = it.onClick) { Text(text = it.label) } }
    }
}

/** Loading failed and there is nothing cached to show; the primary action retries. */
@Composable
fun ErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageState(
        icon = Icons.Rounded.Warning,
        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        title = title,
        message = message,
        modifier = modifier,
    ) {
        Button(onClick = onRetry) { Text(text = stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun MessageState(
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.huge, vertical = Spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(64.dp).background(iconContainerColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        action()
    }
}

@PreviewLightDark
@Composable
private fun StatusStatesPreview() {
    QuakeAlertTheme {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.height(120.dp)) { LoadingState() }
                Box(modifier = Modifier.height(300.dp)) {
                    EmptyState(
                        title = "No M4.5+ earthquakes near İzmir in the last 7 days.",
                        message = "That's good news.",
                        action = StateAction(label = "Show all earthquakes", onClick = {}),
                    )
                }
                Box(modifier = Modifier.height(300.dp)) {
                    ErrorState(
                        title = "Couldn't load earthquakes",
                        message = "Check your internet connection and try again.",
                        onRetry = {},
                    )
                }
            }
        }
    }
}
