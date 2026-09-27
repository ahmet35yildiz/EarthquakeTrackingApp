package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.quakeAlertFilterChipColors
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

@Composable
fun RadiusSelector(
    radiusKm: Int,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(
            text = stringResource(R.string.alert_radius_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            AlertConfig.RADIUS_OPTIONS_KM.forEach { option ->
                RadiusChip(radiusKm = option, isSelected = option == radiusKm, onClick = { onRadiusSelected(option) })
            }
        }
    }
}

@Composable
private fun RadiusChip(radiusKm: Int, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = stringResource(R.string.radius_km_value, radiusKm)) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        },
        colors = quakeAlertFilterChipColors(),
        border = null,
    )
}

@PreviewLightDark
@Composable
private fun RadiusSelectorPreview() {
    QuakeAlertTheme {
        Surface {
            RadiusSelector(
                radiusKm = AlertConfig.DEFAULT_RADIUS_KM,
                onRadiusSelected = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
