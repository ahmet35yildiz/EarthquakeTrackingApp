package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.component.MagnitudeBadge
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.format.localizedPlace
import com.ahmetyildiz.quakealert.core.ui.format.relativeTimeText
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import java.time.Duration
import java.time.Instant

@Composable
fun EarthquakeListItem(
    item: EarthquakeWithDistance,
    cityName: String?,
    now: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val earthquake: Earthquake = item.earthquake
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MagnitudeBadge(magnitude = earthquake.magnitude?.value)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(
                    text = localizedPlace(earthquake.place),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = timeAndDepthText(earthquake, now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.distanceKm != null && cityName != null) {
                    DistanceFromCity(distanceKm = item.distanceKm, cityName = cityName)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun timeAndDepthText(earthquake: Earthquake, now: Instant): String {
    val depthKm: String = formatWholeNumber(earthquake.depthKm.coerceAtLeast(0.0))
    val depth: String = stringResource(R.string.earthquake_depth, depthKm)
    return "${relativeTimeText(earthquake.time, now)} · $depth"
}

@Composable
private fun DistanceFromCity(distanceKm: Double, cityName: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.earthquake_distance_from_city, formatWholeNumber(distanceKm), cityName),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@PreviewLightDark
@Composable
private fun EarthquakeListItemPreview() {
    val now: Instant = Instant.parse("2026-09-25T12:00:00Z")
    val earthquake = Earthquake(
        id = "preview",
        magnitude = Magnitude(value = 4.6, type = "mb"),
        place = "12 km SW of Seferihisar, Turkey",
        time = now.minus(Duration.ofHours(3)),
        location = GeoPoint(38.1, 26.8),
        depthKm = 9.0,
        detailUrl = "",
        isReviewed = true,
        hasTsunamiFlag = false,
        feltReportCount = null,
    )
    QuakeAlertTheme {
        Surface {
            EarthquakeListItem(
                item = EarthquakeWithDistance(earthquake, distanceKm = 41.0),
                cityName = "Izmir",
                now = now,
                onClick = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
