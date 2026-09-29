package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod

@Composable
fun StatisticsFilterChips(
    period: StatisticsPeriod,
    region: RegionFilter,
    nearCityName: String?,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onRegionSelected: (RegionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            StatisticsPeriod.entries.forEach { entry ->
                SelectableFilterChip(
                    label = stringResource(entry.labelRes),
                    icon = painterResource(R.drawable.ic_schedule),
                    isSelected = entry == period,
                    onClick = { onPeriodSelected(entry) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SelectableFilterChip(
                label = stringResource(R.string.filter_world),
                icon = painterResource(R.drawable.ic_language),
                isSelected = region == RegionFilter.WORLD,
                onClick = { onRegionSelected(RegionFilter.WORLD) },
            )
            if (nearCityName != null) {
                SelectableFilterChip(
                    label = stringResource(R.string.filter_near_city, nearCityName),
                    icon = rememberVectorPainter(Icons.Rounded.Place),
                    isSelected = region == RegionFilter.NEAR_CITY,
                    onClick = { onRegionSelected(RegionFilter.NEAR_CITY) },
                )
            }
        }
    }
}

@get:StringRes
private val StatisticsPeriod.labelRes: Int
    get() = when (this) {
        StatisticsPeriod.LAST_7_DAYS -> R.string.statistics_period_7_days
        StatisticsPeriod.LAST_30_DAYS -> R.string.statistics_period_30_days
    }
