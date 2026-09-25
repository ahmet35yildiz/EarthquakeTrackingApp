package com.ahmetyildiz.quakealert.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.core.ui.theme.MagnitudeLargeTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.MagnitudeTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.SeverityColor
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

enum class MagnitudeBadgeSize(val minSize: Dp, val textStyle: TextStyle, val showsSeverityLabel: Boolean) {
    /** List rows: the number only. */
    COMPACT(minSize = 48.dp, textStyle = MagnitudeTextStyle, showsSeverityLabel = false),

    /** Detail header: the number and the severity name. */
    LARGE(minSize = 72.dp, textStyle = MagnitudeLargeTextStyle, showsSeverityLabel = true),
}

/**
 * Magnitude in a solid box coloured by severity. The number is always visible, so colour is never the only cue.
 * A null magnitude (not yet computed by USGS) shows a dash on a neutral colour.
 */
@Composable
fun MagnitudeBadge(
    magnitude: Double?,
    modifier: Modifier = Modifier,
    size: MagnitudeBadgeSize = MagnitudeBadgeSize.COMPACT,
) {
    val severity: MagnitudeSeverity? = magnitude?.let(MagnitudeSeverity::fromMagnitude)
    val colors: SeverityColor = severity
        ?.let { QuakeAlertTheme.severityColors.colorFor(it) }
        ?: SeverityColor(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
    val valueText: String = magnitude
        ?.let { stringResource(R.string.magnitude_value, it) }
        ?: stringResource(R.string.magnitude_unknown_value)
    val severityText: String? = severity?.let { stringResource(it.labelRes) }
    val description: String = severityText
        ?.let { stringResource(R.string.magnitude_badge_description, valueText, it) }
        ?: stringResource(R.string.magnitude_unknown_description)
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = size.minSize, minHeight = size.minSize)
            .clip(MaterialTheme.shapes.small)
            .background(colors.container)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = valueText, style = size.textStyle, color = colors.onContainer)
        if (size.showsSeverityLabel && severityText != null) {
            Text(text = severityText, style = MaterialTheme.typography.labelSmall, color = colors.onContainer)
        }
    }
}

@PreviewLightDark
@Composable
private fun MagnitudeBadgePreview() {
    QuakeAlertTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    listOf(2.7, 4.6, 5.4, 6.2, 7.8, null).forEach { MagnitudeBadge(magnitude = it) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    listOf(3.2, 4.6, 7.8).forEach { MagnitudeBadge(magnitude = it, size = MagnitudeBadgeSize.LARGE) }
                }
            }
        }
    }
}
