package com.ahmetyildiz.quakealert.features.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel.AppVersion

@Composable
fun AboutSection(
    appVersion: AppVersion,
    onOpenUsgsWebsite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.settings_about_title),
        icon = rememberVectorPainter(Icons.Rounded.Info),
        modifier = modifier,
    ) {
        Text(text = stringResource(R.string.settings_about_data_source), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = onOpenUsgsWebsite) { Text(text = stringResource(R.string.settings_about_open_usgs)) }
        DisclaimerNote()
        Text(
            text = stringResource(R.string.settings_about_version, appVersion.name, appVersion.code),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DisclaimerNote() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(
                    text = stringResource(R.string.settings_about_disclaimer_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.settings_about_disclaimer_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AboutSectionPreview() {
    QuakeAlertTheme {
        Surface {
            AboutSection(
                appVersion = AppVersion(name = "1.0", code = 1),
                onOpenUsgsWebsite = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
