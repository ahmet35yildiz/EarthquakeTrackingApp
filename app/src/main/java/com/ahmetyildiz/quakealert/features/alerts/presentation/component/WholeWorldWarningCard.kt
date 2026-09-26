package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun WholeWorldWarningCard(
    isCitySearchAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(
                    text = stringResource(R.string.alert_area_whole_world_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.alert_area_whole_world_warning_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!isCitySearchAvailable) {
                    Text(
                        text = stringResource(R.string.alert_area_city_search_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WholeWorldWarningCardPreview() {
    QuakeAlertTheme {
        Surface {
            WholeWorldWarningCard(isCitySearchAvailable = false, modifier = Modifier.padding(Spacing.large))
        }
    }
}
