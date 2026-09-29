package com.ahmetyildiz.quakealert.features.emergency.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun StrobeLightCard(
    isAvailable: Boolean,
    isOn: Boolean,
    hasFailed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.strobe_title),
        icon = painterResource(R.drawable.ic_emergency),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(if (isAvailable) R.string.strobe_description else R.string.strobe_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasFailed) {
            Text(
                text = stringResource(R.string.strobe_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (isAvailable) ToolToggleButton(isOn = isOn, onToggle = onToggle)
    }
}

@PreviewLightDark
@Composable
private fun StrobeLightCardPreview() {
    QuakeAlertTheme {
        Surface {
            StrobeLightCard(
                isAvailable = true,
                isOn = true,
                hasFailed = false,
                onToggle = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
