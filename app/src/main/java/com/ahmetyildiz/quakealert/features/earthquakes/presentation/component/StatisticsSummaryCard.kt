package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import java.time.Instant

@Composable
fun StatisticsSummaryCard(
    statistics: EarthquakeStatistics,
    now: Instant,
    onEarthquakeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.statistics_overview_title),
        icon = painterResource(R.drawable.ic_bar_chart),
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.large)) {
            StatTile(
                value = formatWholeNumber(statistics.totalCount.toDouble()),
                label = stringResource(R.string.statistics_total_label),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = statistics.averageMagnitude
                    ?.let { stringResource(R.string.statistics_magnitude_value, stringResource(R.string.magnitude_value, it)) }
                    ?: stringResource(R.string.statistics_no_value),
                label = stringResource(R.string.statistics_average_label),
                modifier = Modifier.weight(1f),
            )
        }
        statistics.largest?.let { largest ->
            Text(text = stringResource(R.string.statistics_largest_label), style = MaterialTheme.typography.labelLarge)
            EarthquakeListItem(
                item = EarthquakeWithDistance(earthquake = largest, distanceKm = null),
                cityName = null,
                now = now,
                onClick = { onEarthquakeClick(largest.id) },
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
