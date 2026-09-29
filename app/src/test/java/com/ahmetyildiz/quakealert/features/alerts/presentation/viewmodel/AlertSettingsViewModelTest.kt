package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.ObserveAlertSettingsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SyncAlertScheduleUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.UpdateAlertSettingsUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AlertSettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var notificationAccess: NotificationAccess = NotificationAccess.ALLOWED
    private val viewModel: AlertSettingsViewModel by lazy {
        AlertSettingsViewModel(
            observeAlertSettings = ObserveAlertSettingsUseCase(preferencesRepository),
            updateAlertSettings = UpdateAlertSettingsUseCase(
                preferencesRepository,
                clock,
                SyncAlertScheduleUseCase(preferencesRepository, FakeAlertCheckScheduler()),
            ),
            notificationAccessChecker = { notificationAccess },
            analyticsTracker = analyticsTracker,
        )
    }

    private val preferences: UserPreferences
        get() = preferencesRepository.userPreferences.value

    @Test
    fun `state shows the saved settings`() = runTest {
        val state: AlertSettingsUiState = collectState()
        assertFalse(state.isLoading)
        assertEquals(AlertSettings.DEFAULT, state.settings)
        assertEquals(AreaSelection.from(AlertArea.WholeWorld), state.areaSelection)
    }

    @Test
    fun `toggling off saves, resets the baseline and is tracked`() = runTest {
        collectState()
        viewModel.onAlertsToggled(false)
        assertFalse(preferences.alertSettings.isEnabled)
        assertEquals(clock.now(), preferences.alertBaselineAt)
        assertEquals(listOf(AnalyticsEvent.AlertsToggled(isEnabled = false)), analyticsTracker.events)
        assertFalse(collectState().settings.isEnabled)
    }

    @Test
    fun `threshold change is saved and tracked`() = runTest {
        viewModel.onThresholdChanged(6.0)
        assertEquals(6.0, preferences.alertSettings.magnitudeThreshold)
        val expected = AnalyticsEvent.AlertThresholdChanged(from = 4.5, to = 6.0, context = SetupContext.SETTINGS)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `same threshold is neither saved nor tracked`() = runTest {
        viewModel.onThresholdChanged(AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD)
        assertNull(preferences.alertBaselineAt)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `near a city without a city is kept on screen but not saved`() = runTest {
        val pending = AreaSelection(AreaMode.NEAR_CITY, city = null, radiusKm = 250)
        viewModel.onAreaSelectionChanged(pending)
        assertEquals(pending, collectState().areaSelection)
        assertEquals(AlertArea.WholeWorld, preferences.alertSettings.area)
        assertNull(preferences.alertBaselineAt)
    }

    @Test
    fun `picking a city saves the area around it`() = runTest {
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.NEAR_CITY, city = izmir, radiusKm = 100))
        assertEquals(AlertArea.AroundCity(izmir, radiusKm = 100), preferences.alertSettings.area)
        val expected = AnalyticsEvent.AlertAreaSet(countryCode = "TR", radiusKm = 100, context = SetupContext.SETTINGS)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `switching to the whole world remembers the city for switching back`() = runTest {
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.NEAR_CITY, city = izmir, radiusKm = 100))
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.WHOLE_WORLD, city = izmir, radiusKm = 100))
        assertEquals(AlertArea.WholeWorld, preferences.alertSettings.area)
        assertEquals(izmir, collectState().areaSelection.city)
        assertEquals(AnalyticsEvent.AlertAreaCleared(SetupContext.SETTINGS), analyticsTracker.events.last())
    }

    @Test
    fun `saved city can be cleared to the whole world from the screen state alone`() = runTest {
        preferencesRepository.saveAlertSettings(
            AlertSettings.DEFAULT.copy(area = AlertArea.AroundCity(izmir, radiusKm = 100)),
            baselineAt = clock.now(),
        )
        val saved: AreaSelection = collectState().areaSelection
        viewModel.onAreaSelectionChanged(saved.copy(mode = AreaMode.WHOLE_WORLD))
        assertEquals(AlertArea.WholeWorld, preferences.alertSettings.area)
        assertEquals(AnalyticsEvent.AlertAreaCleared(SetupContext.SETTINGS), analyticsTracker.events.last())
        viewModel.onAreaSelectionChanged(collectState().areaSelection.copy(mode = AreaMode.NEAR_CITY))
        assertEquals(AlertArea.AroundCity(izmir, radiusKm = 100), preferences.alertSettings.area)
    }

    @Test
    fun `permission is checked again when the screen resumes`() = runTest {
        assertEquals(NotificationAccess.ALLOWED, collectState().notificationAccess)
        notificationAccess = NotificationAccess.APP_BLOCKED
        viewModel.onScreenResumed()
        assertEquals(NotificationAccess.APP_BLOCKED, collectState().notificationAccess)
    }

    @Test
    fun `alert channel turned off while notifications stay allowed is seen on resume`() = runTest {
        notificationAccess = NotificationAccess.ALERT_CHANNEL_BLOCKED
        viewModel.onScreenResumed()
        assertEquals(NotificationAccess.ALERT_CHANNEL_BLOCKED, collectState().notificationAccess)
    }

    private fun TestScope.collectState(): AlertSettingsUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState.value
    }
}
