package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

@Composable
fun MagnitudeThresholdSelector(
    threshold: Double,
    onThresholdChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue: Float by remember(threshold) { mutableFloatStateOf(threshold.toFloat()) }
    val magnitudeText: String = stringResource(R.string.magnitude_value, snapToThresholdStep(sliderValue))
    val title: String = stringResource(R.string.alert_threshold_title)
    val badgeText: String = stringResource(R.string.alert_threshold_badge, magnitudeText)
    SectionCard(
        title = title,
        icon = painterResource(R.drawable.ic_waves),
        modifier = modifier,
        trailing = { ThresholdBadge(text = badgeText) },
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = snapToThresholdStep(it).toFloat() },
            onValueChangeFinished = { onThresholdChange(snapToThresholdStep(sliderValue)) },
            valueRange = AlertConfig.THRESHOLD_RANGE.start.toFloat()..AlertConfig.THRESHOLD_RANGE.endInclusive.toFloat(),
            steps = countThresholdSliderSteps(),
            modifier = Modifier.semantics {
                contentDescription = title
                stateDescription = badgeText
            },
        )
        ThresholdRangeLabels()
        Text(
            text = stringResource(R.string.alert_threshold_hint, magnitudeText),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThresholdBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
        )
    }
}

@Composable
private fun ThresholdRangeLabels() {
    Row(modifier = Modifier.fillMaxWidth()) {
        RangeLabel(magnitude = AlertConfig.THRESHOLD_RANGE.start)
        Spacer(modifier = Modifier.weight(1f))
        RangeLabel(magnitude = AlertConfig.THRESHOLD_RANGE.endInclusive)
    }
}

@Composable
private fun RangeLabel(magnitude: Double) {
    Text(
        text = stringResource(R.string.magnitude_value, magnitude),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@PreviewLightDark
@Composable
private fun MagnitudeThresholdSelectorPreview() {
    QuakeAlertTheme {
        Surface {
            MagnitudeThresholdSelector(
                threshold = AlertConfig.DEFAULT_THRESHOLD,
                onThresholdChange = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
