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
import com.ahmetyildiz.quakealert.core.ui.format.currentLocale
import com.ahmetyildiz.quakealert.core.ui.theme.MagnitudeLargeTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.MagnitudeTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.SeverityColor
import com.ahmetyildiz.quakealert.core.ui.theme.SeverityLabelTextStyle
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

enum class MagnitudeBadgeSize(val minSize: Dp, val textStyle: TextStyle, val usesShortSeverityLabel: Boolean) {
    COMPACT(minSize = 52.dp, textStyle = MagnitudeTextStyle, usesShortSeverityLabel = true),

    LARGE(minSize = 72.dp, textStyle = MagnitudeLargeTextStyle, usesShortSeverityLabel = false),
}

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
    val badgeLabel: String? = severity
        ?.let { stringResource(if (size.usesShortSeverityLabel) it.shortLabelRes else it.labelRes) }
        ?.uppercase(currentLocale())
    val description: String = severityText
        ?.let { stringResource(R.string.magnitude_badge_description, valueText, it) }
        ?: stringResource(R.string.magnitude_unknown_description)
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = size.minSize, minHeight = size.minSize)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.container)
            .padding(horizontal = Spacing.extraSmall, vertical = Spacing.extraSmall)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = valueText, style = size.textStyle, color = colors.onContainer)
        if (badgeLabel != null) {
            Text(text = badgeLabel, style = SeverityLabelTextStyle, color = colors.onContainer, maxLines = 1)
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
                    listOf(3.2, 5.4, 7.8).forEach { MagnitudeBadge(magnitude = it, size = MagnitudeBadgeSize.LARGE) }
                }
            }
        }
    }
}
