package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.SimulatedAlertScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulatedAlert
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationOutcome
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.ObserveAlertSettingsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SimulateAlertUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DeveloperToolsViewModel @Inject constructor(
    observeAlertSettings: ObserveAlertSettingsUseCase,
    private val simulateAlert: SimulateAlertUseCase,
    private val simulatedAlertScheduler: SimulatedAlertScheduler,
    private val alertCheckScheduler: AlertCheckScheduler,
    private val analyticsTracker: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {

    private val state = MutableStateFlow(DeveloperToolsUiState())

    val uiState: StateFlow<DeveloperToolsUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlertSettings().collect { settings -> state.update { it.withSettings(settings) } }
        }
    }

    fun onMagnitudeChanged(text: String) {
        state.update { it.copy(form = it.form.copy(magnitude = text)) }
    }

    fun onDistanceChanged(text: String) {
        state.update { it.copy(form = it.form.copy(distanceKm = text)) }
    }

    fun onDelayChanged(text: String) {
        state.update { it.copy(form = it.form.copy(delayMinutes = text)) }
    }

    fun onSimulateNow(place: String) {
        val request: SimulationRequest = buildRequest(place) ?: return
        simulate { simulateAlert(request) }
    }

    fun onSchedule(place: String) {
        val request: SimulationRequest = buildRequest(place) ?: return
        val delay: Duration = Duration.ofMinutes(state.value.form.delayMinutesValue ?: return)
        simulatedAlertScheduler.schedule(request, delay)
        state.update { it.copy(message = DeveloperToolsMessage.SCHEDULED, scheduledFor = clock.now() + delay) }
    }

    fun onSimulateAgain() {
        simulate { simulateAlert.repeatLast() }
    }

    fun onRunCheckNow() {
        analyticsTracker.track(AnalyticsEvent.DeveloperCheckTriggered)
        alertCheckScheduler.runCheckNow()
        state.update { it.copy(message = DeveloperToolsMessage.CHECK_STARTED) }
    }

    private fun buildRequest(place: String): SimulationRequest? {
        val current: DeveloperToolsUiState = state.value
        if (!current.canSimulate) return null
        val magnitude: Double = current.form.magnitudeValue ?: return null
        return SimulationRequest(place, magnitude, current.form.distanceKmValue ?: 0.0)
    }

    private fun simulate(run: suspend () -> SimulatedAlert) {
        if (state.value.isSimulating) return
        state.update { it.copy(isSimulating = true) }
        viewModelScope.launch {
            val simulated: SimulatedAlert = run()
            state.update { it.copy(message = simulated.outcome.toMessage(), isSimulating = false) }
        }
    }

    private fun DeveloperToolsUiState.withSettings(settings: AlertSettings): DeveloperToolsUiState {
        val cityName: String? = (settings.area as? AlertArea.AroundCity)?.city?.name
        if (form.magnitude.isNotEmpty()) return copy(cityName = cityName)
        val magnitude: String = String.format(Locale.ROOT, MAGNITUDE_FORMAT, settings.magnitudeThreshold + MAGNITUDE_ABOVE_THRESHOLD)
        return copy(cityName = cityName, form = form.copy(magnitude = magnitude))
    }

    private fun SimulationOutcome.toMessage(): DeveloperToolsMessage =
        when (this) {
            SimulationOutcome.POSTED -> DeveloperToolsMessage.ALERT_POSTED
            SimulationOutcome.ALREADY_NOTIFIED -> DeveloperToolsMessage.ALREADY_NOTIFIED
            SimulationOutcome.NOT_MATCHED -> DeveloperToolsMessage.NOT_MATCHED
            SimulationOutcome.NOTIFICATIONS_OFF -> DeveloperToolsMessage.NOTIFICATIONS_OFF
            SimulationOutcome.ALERTS_OFF -> DeveloperToolsMessage.ALERTS_OFF
            SimulationOutcome.NOTHING_TO_REPEAT -> DeveloperToolsMessage.NOTHING_TO_REPEAT
        }

    private companion object {
        const val MAGNITUDE_FORMAT: String = "%.1f"
        const val MAGNITUDE_ABOVE_THRESHOLD: Double = 0.5
    }
}
