package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.ObserveAlertSettingsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.UpdateAlertSettingsUseCase
import kotlinx.coroutines.flow.toList
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
import java.time.Instant

class AlertSettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var areNotificationsAllowed: Boolean = true
    private val viewModel: AlertSettingsViewModel by lazy {
        AlertSettingsViewModel(
            observeAlertSettings = ObserveAlertSettingsUseCase(preferencesRepository),
            updateAlertSettings = UpdateAlertSettingsUseCase(preferencesRepository, clock),
            notificationPermissionChecker = { areNotificationsAllowed },
            analyticsTracker = analyticsTracker,
        )
    }

    private val preferences: UserPreferences
        get() = preferencesRepository.userPreferences.value

    @Test
    fun `state shows the saved settings and last check`() = runTest {
        val lastCheckedAt: Instant = clock.now()
        preferencesRepository.setLastCheckedAt(lastCheckedAt)
        val state: AlertSettingsUiState = collectState()
        assertFalse(state.isLoading)
        assertEquals(AlertSettings.DEFAULT, state.settings)
        assertEquals(AreaSelection.from(AlertArea.WholeWorld), state.areaSelection)
        assertEquals(lastCheckedAt, state.lastCheckedAt)
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
    fun `every saved change emits one saved event`() = runTest {
        val events: MutableList<AlertSettingsEvent> = mutableListOf()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(events) }
        viewModel.onThresholdChanged(5.0)
        viewModel.onThresholdChanged(5.0)
        assertEquals(listOf(AlertSettingsEvent.Saved), events)
    }

    @Test
    fun `permission is checked again when the screen resumes`() = runTest {
        assertTrue(collectState().areNotificationsAllowed)
        areNotificationsAllowed = false
        viewModel.onScreenResumed()
        assertFalse(collectState().areNotificationsAllowed)
    }

    private fun TestScope.collectState(): AlertSettingsUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState.value
    }
}
