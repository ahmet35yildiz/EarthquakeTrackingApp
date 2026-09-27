package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.quakeAlertFilterChipColors
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter

@Composable
fun EarthquakeFilterChips(
    options: EarthquakeListOptions,
    nearCityName: String?,
    magnitudeThreshold: Double,
    onRegionFilterSelected: (RegionFilter) -> Unit,
    onMagnitudeFilterSelected: (MagnitudeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        ChipGroup {
            SelectableChip(
                label = stringResource(R.string.filter_world),
                icon = painterResource(R.drawable.ic_language),
                isSelected = options.region == RegionFilter.WORLD,
                onClick = { onRegionFilterSelected(RegionFilter.WORLD) },
            )
            if (nearCityName != null) {
                SelectableChip(
                    label = stringResource(R.string.filter_near_city, nearCityName),
                    icon = rememberVectorPainter(Icons.Rounded.Place),
                    isSelected = options.region == RegionFilter.NEAR_CITY,
                    onClick = { onRegionFilterSelected(RegionFilter.NEAR_CITY) },
                )
            }
        }
        ChipGroup {
            SelectableChip(
                label = stringResource(
                    R.string.filter_all_magnitudes,
                    magnitudeText(EarthquakesConfig.RECENT_MIN_MAGNITUDE),
                ),
                icon = painterResource(R.drawable.ic_waves),
                isSelected = options.magnitude == MagnitudeFilter.ALL,
                onClick = { onMagnitudeFilterSelected(MagnitudeFilter.ALL) },
            )
            SelectableChip(
                label = stringResource(R.string.filter_above_threshold, magnitudeText(magnitudeThreshold)),
                icon = painterResource(R.drawable.ic_notifications),
                isSelected = options.magnitude == MagnitudeFilter.ABOVE_THRESHOLD,
                onClick = { onMagnitudeFilterSelected(MagnitudeFilter.ABOVE_THRESHOLD) },
            )
        }
    }
}

@Composable
private fun ChipGroup(chips: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) { chips() }
}

@Composable
private fun magnitudeText(magnitude: Double): String = stringResource(R.string.magnitude_value, magnitude)

@Composable
private fun SelectableChip(label: String, icon: Painter, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        },
        colors = quakeAlertFilterChipColors(),
        border = null,
    )
}

@PreviewLightDark
@Composable
private fun EarthquakeFilterChipsPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeFilterChips(
                options = EarthquakeListOptions(region = RegionFilter.NEAR_CITY),
                nearCityName = "Izmir",
                magnitudeThreshold = 4.5,
                onRegionFilterSelected = {},
                onMagnitudeFilterSelected = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
