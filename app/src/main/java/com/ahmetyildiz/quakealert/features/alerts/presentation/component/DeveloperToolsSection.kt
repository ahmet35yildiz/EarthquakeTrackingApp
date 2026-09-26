package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.format.formatDataTime
import com.ahmetyildiz.quakealert.core.ui.format.rememberCurrentTime
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.DeveloperToolsMessage
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.DeveloperToolsUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.DeveloperToolsViewModel
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.SimulationForm

data class DeveloperToolsActions(
    val onMagnitudeChanged: (String) -> Unit,
    val onDistanceChanged: (String) -> Unit,
    val onDelayChanged: (String) -> Unit,
    val onSimulateNow: () -> Unit,
    val onSchedule: () -> Unit,
    val onSimulateAgain: () -> Unit,
    val onRunCheckNow: () -> Unit,
)

@Composable
fun DeveloperToolsEntry(
    modifier: Modifier = Modifier,
    viewModel: DeveloperToolsViewModel = hiltViewModel(),
) {
    val uiState: DeveloperToolsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val simulatedPlace: String = stringResource(R.string.simulated_earthquake_place)
    val actions = DeveloperToolsActions(
        onMagnitudeChanged = viewModel::onMagnitudeChanged,
        onDistanceChanged = viewModel::onDistanceChanged,
        onDelayChanged = viewModel::onDelayChanged,
        onSimulateNow = { viewModel.onSimulateNow(simulatedPlace) },
        onSchedule = { viewModel.onSchedule(simulatedPlace) },
        onSimulateAgain = viewModel::onSimulateAgain,
        onRunCheckNow = viewModel::onRunCheckNow,
    )
    DeveloperToolsSection(uiState = uiState, actions = actions, modifier = modifier)
}

@Composable
fun DeveloperToolsSection(
    uiState: DeveloperToolsUiState,
    actions: DeveloperToolsActions,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.developer_alert_testing_title),
        icon = painterResource(R.drawable.ic_notifications),
        modifier = modifier,
    ) {
        SimulationFields(form = uiState.form, cityName = uiState.cityName, actions = actions)
        ToolButton(R.string.developer_simulate_alert, R.string.developer_simulate_alert_description, uiState.canSimulate, true, actions.onSimulateNow)
        ScheduleRow(form = uiState.form, isEnabled = uiState.canSchedule, actions = actions)
        ToolButton(R.string.developer_simulate_again, R.string.developer_simulate_again_description, !uiState.isSimulating, false, actions.onSimulateAgain)
        ToolButton(R.string.developer_run_check, R.string.developer_run_check_description, true, false, actions.onRunCheckNow)
        uiState.message?.let { ResultMessage(message = it, uiState = uiState) }
    }
}

@Composable
private fun SimulationFields(form: SimulationForm, cityName: String?, actions: DeveloperToolsActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        NumberField(
            value = form.magnitude,
            label = stringResource(R.string.developer_simulation_magnitude),
            isValid = form.magnitudeValue != null,
            onValueChange = actions.onMagnitudeChanged,
            modifier = Modifier.weight(1f),
        )
        if (cityName != null) {
            NumberField(
                value = form.distanceKm,
                label = stringResource(R.string.developer_simulation_distance, cityName),
                isValid = form.distanceKmValue != null,
                onValueChange = actions.onDistanceChanged,
                modifier = Modifier.weight(1f),
            )
        }
    }
    if (cityName == null) {
        Text(
            text = stringResource(R.string.developer_simulation_whole_world_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScheduleRow(form: SimulationForm, isEnabled: Boolean, actions: DeveloperToolsActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small), verticalAlignment = Alignment.CenterVertically) {
            NumberField(
                value = form.delayMinutes,
                label = stringResource(R.string.developer_simulation_delay),
                isValid = form.delayMinutesValue != null,
                onValueChange = actions.onDelayChanged,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = actions.onSchedule, enabled = isEnabled, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.developer_schedule))
            }
        }
        Description(textRes = R.string.developer_schedule_description)
    }
}

@Composable
private fun NumberField(value: String, label: String, isValid: Boolean, onValueChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label, maxLines = 1) },
        isError = !isValid,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
private fun ToolButton(labelRes: Int, descriptionRes: Int, isEnabled: Boolean, isPrimary: Boolean, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
        if (isPrimary) {
            Button(onClick = onClick, enabled = isEnabled, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(labelRes))
            }
        } else {
            OutlinedButton(onClick = onClick, enabled = isEnabled, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(labelRes))
            }
        }
        Description(textRes = descriptionRes)
    }
}

@Composable
private fun Description(textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ResultMessage(message: DeveloperToolsMessage, uiState: DeveloperToolsUiState) {
    val now = rememberCurrentTime()
    val text: String = when (message) {
        DeveloperToolsMessage.SCHEDULED -> stringResource(
            R.string.developer_result_scheduled,
            uiState.scheduledFor?.let { formatDataTime(it, now) }.orEmpty(),
        )
        else -> stringResource(message.textRes)
    }
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.medium))
    }
}

private val DeveloperToolsMessage.textRes: Int
    get() = when (this) {
        DeveloperToolsMessage.ALERT_POSTED -> R.string.developer_result_posted
        DeveloperToolsMessage.ALREADY_NOTIFIED -> R.string.developer_result_already_notified
        DeveloperToolsMessage.NOT_MATCHED -> R.string.developer_result_not_matched
        DeveloperToolsMessage.NOTIFICATIONS_OFF -> R.string.developer_result_notifications_off
        DeveloperToolsMessage.ALERTS_OFF -> R.string.developer_result_alerts_off
        DeveloperToolsMessage.NOTHING_TO_REPEAT -> R.string.developer_result_nothing_to_repeat
        DeveloperToolsMessage.SCHEDULED -> R.string.developer_result_scheduled
        DeveloperToolsMessage.CHECK_STARTED -> R.string.developer_result_check_started
    }

@PreviewLightDark
@Composable
private fun DeveloperToolsSectionPreview() {
    QuakeAlertTheme {
        Surface {
            DeveloperToolsSection(
                uiState = DeveloperToolsUiState(
                    form = SimulationForm(magnitude = "5.0"),
                    cityName = "İzmir",
                    message = DeveloperToolsMessage.NOT_MATCHED,
                ),
                actions = DeveloperToolsActions({}, {}, {}, {}, {}, {}, {}),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
