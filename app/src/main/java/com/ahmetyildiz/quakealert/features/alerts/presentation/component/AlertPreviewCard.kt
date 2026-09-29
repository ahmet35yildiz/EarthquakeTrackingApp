package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AlertPreviewUiState

@Composable
fun AlertPreviewCard(
    state: AlertPreviewUiState,
    modifier: Modifier = Modifier,
) {
    val text: String = previewText(state) ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(R.drawable.ic_notifications), contentDescription = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun previewText(state: AlertPreviewUiState): String? =
    when (state) {
        AlertPreviewUiState.Hidden -> null
        AlertPreviewUiState.Loading -> stringResource(R.string.alert_preview_loading)
        AlertPreviewUiState.Unavailable -> stringResource(R.string.alert_preview_unavailable)
        is AlertPreviewUiState.Ready -> matchesText(state.preview)
    }

@Composable
private fun matchesText(preview: AlertPreview): String {
    val days: Int = preview.period.toDays().toInt()
    val period: String = pluralStringResource(R.plurals.alert_preview_period, days, days)
    if (preview.matchCount == 0) return stringResource(R.string.alert_preview_none, period)
    return pluralStringResource(R.plurals.alert_preview_matches, preview.matchCount, preview.matchCount, period)
}

@PreviewLightDark
@Composable
private fun AlertPreviewCardPreview() {
    QuakeAlertTheme {
        Surface {
            AlertPreviewCard(
                state = AlertPreviewUiState.Ready(AlertPreview(matchCount = 5, period = AlertConfig.PREVIEW_PERIOD)),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
