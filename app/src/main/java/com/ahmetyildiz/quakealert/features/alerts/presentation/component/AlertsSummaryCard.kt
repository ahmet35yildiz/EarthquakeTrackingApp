package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun AlertsSummaryCard(
    settings: AlertSettings,
    onAlertsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = settings.isEnabled, role = Role.Switch, onValueChange = onAlertsToggled),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.alerts_switch_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = settings.isEnabled, onCheckedChange = null)
            }
            Text(text = summaryText(settings), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun summaryText(settings: AlertSettings): String {
    val magnitude: String = stringResource(R.string.magnitude_value, settings.magnitudeThreshold)
    return when {
        !settings.isEnabled -> stringResource(R.string.alerts_summary_off)
        settings.area is AlertArea.AroundCity -> stringResource(
            R.string.alerts_summary_city,
            magnitude,
            settings.area.radiusKm,
            settings.area.city.name,
        )
        else -> stringResource(R.string.alerts_summary_world, magnitude)
    }
}

@PreviewLightDark
@Composable
private fun AlertsSummaryCardPreview() {
    QuakeAlertTheme {
        Surface {
            AlertsSummaryCard(
                settings = AlertSettings(
                    isEnabled = true,
                    magnitudeThreshold = 4.5,
                    area = AlertArea.AroundCity(City("İzmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250),
                ),
                onAlertsToggled = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
