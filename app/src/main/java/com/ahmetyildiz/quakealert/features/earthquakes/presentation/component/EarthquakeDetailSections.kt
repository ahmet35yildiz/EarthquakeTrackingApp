package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.style.TextAlign
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.MagnitudeBadge
import com.ahmetyildiz.quakealert.core.ui.component.MagnitudeBadgeSize
import com.ahmetyildiz.quakealert.core.ui.format.formatCoordinates
import com.ahmetyildiz.quakealert.core.ui.format.formatLocalDateTime
import com.ahmetyildiz.quakealert.core.ui.format.formatUtcDateTime
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.format.relativeTimeText
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DistanceFromCity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import java.time.Instant

@Composable
fun DetailHeaderCard(earthquake: Earthquake, now: Instant, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MagnitudeBadge(magnitude = earthquake.magnitude?.value, size = MagnitudeBadgeSize.LARGE)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                Text(
                    text = earthquake.place ?: stringResource(R.string.earthquake_unknown_place),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = relativeTimeText(earthquake.time, now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun DistanceCard(distance: DistanceFromCity, modifier: Modifier = Modifier) {
    val areaTextRes: Int =
        if (distance.isWithinAlertArea) R.string.detail_within_alert_area else R.string.detail_outside_alert_area
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Rounded.Place, contentDescription = null)
            Column {
                Text(
                    text = stringResource(
                        R.string.earthquake_distance_from_city,
                        formatWholeNumber(distance.distanceKm),
                        distance.cityName,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(areaTextRes, distance.alertRadiusKm),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun DetailFactsCard(earthquake: Earthquake, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small)) {
            Text(
                text = stringResource(R.string.detail_facts_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = Spacing.small).semantics { heading() },
            )
            FactRow(R.string.detail_row_magnitude, magnitudeText(earthquake.magnitude))
            FactRow(R.string.detail_row_local_time, formatLocalDateTime(earthquake.time))
            FactRow(R.string.detail_row_utc_time, formatUtcDateTime(earthquake.time))
            FactRow(R.string.detail_row_depth, depthText(earthquake.depthKm))
            FactRow(R.string.detail_row_coordinates, formatCoordinates(earthquake.location))
            FactRow(R.string.detail_row_review_status, stringResource(reviewStatusRes(earthquake.isReviewed)))
            FactRow(R.string.detail_row_tsunami, stringResource(tsunamiRes(earthquake.hasTsunamiFlag)))
            FeltReportsRow(earthquake.feltReportCount)
        }
    }
}

@Composable
private fun FeltReportsRow(feltReportCount: Int?) {
    if (feltReportCount == null || feltReportCount <= 0) return
    FactRow(
        R.string.detail_row_felt_reports,
        pluralStringResource(R.plurals.detail_felt_reports_value, feltReportCount, feltReportCount),
    )
}

@Composable
private fun FactRow(labelRes: Int, value: String) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun magnitudeText(magnitude: Magnitude?): String {
    if (magnitude == null) return stringResource(R.string.magnitude_unknown_value)
    val value: String = stringResource(R.string.magnitude_value, magnitude.value)
    val type: String = magnitude.type ?: return value
    return stringResource(R.string.detail_magnitude_with_type, value, type)
}

@Composable
private fun depthText(depthKm: Double): String =
    stringResource(R.string.detail_depth_value, formatWholeNumber(depthKm.coerceAtLeast(0.0)))

private fun reviewStatusRes(isReviewed: Boolean): Int =
    if (isReviewed) R.string.detail_status_reviewed else R.string.detail_status_automatic

private fun tsunamiRes(hasTsunamiFlag: Boolean): Int =
    if (hasTsunamiFlag) R.string.detail_tsunami_flag_set else R.string.detail_tsunami_flag_not_set
