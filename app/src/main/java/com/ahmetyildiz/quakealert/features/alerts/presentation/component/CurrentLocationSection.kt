package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.LocationLookup

data class CurrentLocationActions(
    val onUseMyLocation: () -> Unit,
    val onOpenAppSettings: () -> Unit,
    val onOpenLocationSettings: () -> Unit,
)

@Composable
fun CurrentLocationSection(
    lookup: LocationLookup,
    actions: CurrentLocationActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        OutlinedButton(
            onClick = actions.onUseMyLocation,
            enabled = lookup != LocationLookup.Locating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(text = stringResource(R.string.location_use_mine), modifier = Modifier.padding(start = Spacing.small))
        }
        when (lookup) {
            LocationLookup.Idle, is LocationLookup.Found -> Unit
            LocationLookup.Locating -> ProgressMessage(text = stringResource(R.string.location_finding))
            is LocationLookup.Failed -> LocationFailure(error = lookup.error, actions = actions)
        }
    }
}

@Composable
private fun LocationFailure(error: AppError, actions: CurrentLocationActions) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ResultMessage(text = stringResource(error.toLocationMessageRes()), modifier = Modifier.weight(1f))
        when (error) {
            AppError.LocationPermissionDenied -> SettingsButton(onClick = actions.onOpenAppSettings)
            AppError.LocationDisabled -> SettingsButton(onClick = actions.onOpenLocationSettings)
            else -> TextButton(onClick = actions.onUseMyLocation) { Text(text = stringResource(R.string.action_retry)) }
        }
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(text = stringResource(R.string.action_open_settings)) }
}

private fun AppError.toLocationMessageRes(): Int =
    when (this) {
        AppError.LocationPermissionDenied -> R.string.location_error_permission
        AppError.LocationDisabled -> R.string.location_error_disabled
        AppError.LocationUnavailable -> R.string.location_error_not_found
        AppError.Network -> R.string.error_message_network
        else -> R.string.error_message_generic
    }

@PreviewLightDark
@Composable
private fun CurrentLocationSectionPreview() {
    QuakeAlertTheme {
        Surface {
            CurrentLocationSection(
                lookup = LocationLookup.Failed(AppError.LocationPermissionDenied),
                actions = CurrentLocationActions(onUseMyLocation = {}, onOpenAppSettings = {}, onOpenLocationSettings = {}),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
