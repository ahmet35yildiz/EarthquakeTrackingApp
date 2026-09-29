package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeSimulatedAlertScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeNotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.DeliverAlertsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.NotifyAlertsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.ObserveAlertSettingsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SimulateAlertUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration

class DeveloperToolsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val checkScheduler = FakeAlertCheckScheduler()
    private val simulationScheduler = FakeSimulatedAlertScheduler()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val viewModel: DeveloperToolsViewModel by lazy {
        DeveloperToolsViewModel(
            observeAlertSettings = ObserveAlertSettingsUseCase(preferencesRepository),
            simulateAlert = SimulateAlertUseCase(
                preferencesRepository,
                FakeEarthquakeRepository(),
                DeliverAlertsUseCase(
                    FakeNotifiedEarthquakeRepository(),
                    AlertMatcher(),
                    NotifyAlertsUseCase(FakeAlertNotifier(), { NotificationAccess.ALLOWED }, FakeAnalyticsTracker()),
                ),
                analyticsTracker,
                clock,
            ),
            simulatedAlertScheduler = simulationScheduler,
            alertCheckScheduler = checkScheduler,
            analyticsTracker = analyticsTracker,
            clock = clock,
        )
    }

    private val state: DeveloperToolsUiState
        get() = viewModel.uiState.value

    @Test
    fun `form starts just above the threshold and knows the city`() = runTest {
        save(AlertSettings.DEFAULT.copy(magnitudeThreshold = 6.0, area = AlertArea.AroundCity(izmir, radiusKm = 100)))
        assertEquals("6.5", state.form.magnitude)
        assertEquals("İzmir", state.cityName)
    }

    @Test
    fun `whole world has no city`() = runTest {
        save(AlertSettings.DEFAULT)
        assertNull(state.cityName)
    }

    @Test
    fun `simulation now posts when the values match`() = runTest {
        save(AlertSettings.DEFAULT)
        viewModel.onSimulateNow(PLACE)
        assertEquals(DeveloperToolsMessage.ALERT_POSTED, state.message)
        assertFalse(state.isSimulating)
    }

    @Test
    fun `simulation now below the threshold reports no match`() = runTest {
        save(AlertSettings.DEFAULT)
        viewModel.onMagnitudeChanged("3,0")
        viewModel.onSimulateNow(PLACE)
        assertEquals(DeveloperToolsMessage.NOT_MATCHED, state.message)
    }

    @Test
    fun `invalid values block the simulation`() = runTest {
        save(AlertSettings.DEFAULT)
        viewModel.onMagnitudeChanged("abc")
        assertFalse(state.canSimulate)
        viewModel.onSimulateNow(PLACE)
        assertNull(state.message)
    }

    @Test
    fun `scheduling passes the values and the delay`() = runTest {
        save(AlertSettings.DEFAULT.copy(area = AlertArea.AroundCity(izmir, radiusKm = 100)))
        viewModel.onMagnitudeChanged("5.5")
        viewModel.onDistanceChanged("150")
        viewModel.onDelayChanged("2")
        viewModel.onSchedule(PLACE)
        val expected = SimulationRequest(PLACE, magnitude = 5.5, distanceFromCityKm = 150.0) to Duration.ofMinutes(2)
        assertEquals(listOf(expected), simulationScheduler.scheduled)
        assertEquals(DeveloperToolsMessage.SCHEDULED, state.message)
        assertEquals(clock.now() + Duration.ofMinutes(2), state.scheduledFor)
    }

    @Test
    fun `delay above an hour blocks scheduling`() = runTest {
        save(AlertSettings.DEFAULT)
        viewModel.onDelayChanged("61")
        assertFalse(state.canSchedule)
    }

    @Test
    fun `simulating again reports that nothing was duplicated`() = runTest {
        save(AlertSettings.DEFAULT)
        viewModel.onSimulateNow(PLACE)
        viewModel.onSimulateAgain()
        assertEquals(DeveloperToolsMessage.ALREADY_NOTIFIED, state.message)
    }

    @Test
    fun `run check now starts a check and is tracked`() {
        viewModel.onRunCheckNow()
        assertEquals(1, checkScheduler.runNowCount)
        assertEquals(DeveloperToolsMessage.CHECK_STARTED, state.message)
        assertEquals(listOf(AnalyticsEvent.DeveloperCheckTriggered), analyticsTracker.events)
    }

    private suspend fun save(settings: AlertSettings) {
        preferencesRepository.saveAlertSettings(settings, clock.now() - Duration.ofMinutes(5))
    }

    private companion object {
        const val PLACE: String = "Simulated place"
    }
}
