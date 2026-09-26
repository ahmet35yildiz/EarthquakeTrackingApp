package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun SelectedCityCard(
    city: City,
    countryName: String?,
    onChange: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceIcon()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.selected_city_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = city.name, style = MaterialTheme.typography.titleMedium)
                regionText(city.adminArea, countryName)?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            onChange?.let { FilledTonalButton(onClick = it) { Text(text = stringResource(R.string.action_change)) } }
        }
    }
}

@Composable
private fun PlaceIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun regionText(adminArea: String?, countryName: String?): String? =
    when {
        adminArea != null && countryName != null -> stringResource(R.string.city_region_pair, adminArea, countryName)
        else -> adminArea ?: countryName
    }

@PreviewLightDark
@Composable
private fun SelectedCityCardPreview() {
    QuakeAlertTheme {
        Surface {
            SelectedCityCard(
                city = City("Springfield", "Illinois", "US", GeoPoint(39.8, -89.6)),
                countryName = "United States",
                onChange = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
