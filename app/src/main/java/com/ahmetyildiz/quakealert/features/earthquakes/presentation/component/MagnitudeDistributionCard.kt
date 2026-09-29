package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.component.labelRes
import com.ahmetyildiz.quakealert.core.ui.format.formatWholeNumber
import com.ahmetyildiz.quakealert.core.ui.theme.LocalSeverityColors
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.SeverityCount

private val BarHeight: Dp = 8.dp
private val MinVisibleBarWidth: Dp = 4.dp
private val BarShape: RoundedCornerShape = RoundedCornerShape(4.dp)
private const val MAGNITUDE_STEP_BELOW_NEXT_CLASS: Double = 0.1

@Composable
fun MagnitudeDistributionCard(severityCounts: List<SeverityCount>, modifier: Modifier = Modifier) {
    val maxCount: Int = severityCounts.maxOf { it.count }.coerceAtLeast(1)
    SectionCard(
        title = stringResource(R.string.statistics_magnitude_title),
        icon = painterResource(R.drawable.ic_waves),
        modifier = modifier,
    ) {
        severityCounts.forEach { SeverityRow(severityCount = it, maxCount = maxCount) }
    }
}

@Composable
private fun SeverityRow(severityCount: SeverityCount, maxCount: Int) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text(
                text = stringResource(
                    R.string.statistics_severity_label,
                    stringResource(severityCount.severity.labelRes),
                    magnitudeRangeText(severityCount.severity),
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(text = formatWholeNumber(severityCount.count.toDouble()), style = MaterialTheme.typography.labelLarge)
        }
        SeverityBar(severity = severityCount.severity, fraction = severityCount.count.toFloat() / maxCount)
    }
}

@Composable
private fun SeverityBar(severity: MagnitudeSeverity, fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight)
            .clip(BarShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .widthIn(min = MinVisibleBarWidth)
                    .fillMaxHeight()
                    .clip(BarShape)
                    .background(LocalSeverityColors.current.colorFor(severity).container),
            )
        }
    }
}

@Composable
private fun magnitudeRangeText(severity: MagnitudeSeverity): String {
    val lower: Double = severity.lowerBound.coerceAtLeast(EarthquakesConfig.RECENT_MIN_MAGNITUDE)
    val lowerText: String = stringResource(R.string.magnitude_value, lower)
    val next: MagnitudeSeverity = MagnitudeSeverity.entries.getOrNull(severity.ordinal + 1)
        ?: return stringResource(R.string.statistics_magnitude_open_range, lowerText)
    val upperText: String = stringResource(R.string.magnitude_value, next.lowerBound - MAGNITUDE_STEP_BELOW_NEXT_CLASS)
    return stringResource(R.string.statistics_magnitude_range, lowerText, upperText)
}
