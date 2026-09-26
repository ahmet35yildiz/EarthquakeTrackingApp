package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.OnboardingStep
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.CompleteOnboardingUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.SyncAlertScheduleUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OnboardingViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val scheduler = FakeAlertCheckScheduler()
    private val analyticsTracker = FakeAnalyticsTracker()
    private val savedStateHandle = SavedStateHandle()
    private var areNotificationsAllowed: Boolean = false
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val viewModel: OnboardingViewModel by lazy { createViewModel() }

    private val state: OnboardingUiState
        get() = viewModel.uiState.value

    private val preferences: UserPreferences
        get() = preferencesRepository.userPreferences.value

    @Test
    fun `first start tracks the start and the welcome step`() {
        assertEquals(OnboardingPage.WELCOME, state.page)
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.OnboardingStarted,
            AnalyticsEvent.OnboardingStepViewed(OnboardingStep.WELCOME),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    @Test
    fun `restored onboarding keeps its page and threshold and tracks nothing`() {
        viewModel.onNext()
        viewModel.onThresholdChanged(6.0)
        analyticsTracker.events.clear()
        val restored: OnboardingViewModel = createViewModel()
        assertEquals(OnboardingPage.ALERT_SETUP, restored.uiState.value.page)
        assertEquals(6.0, restored.uiState.value.threshold)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `next and back move between the pages and track each view`() {
        viewModel.onNext()
        viewModel.onNext()
        assertEquals(OnboardingPage.NOTIFICATIONS, state.page)
        viewModel.onBack()
        assertEquals(OnboardingPage.ALERT_SETUP, state.page)
        assertEquals(AnalyticsEvent.OnboardingStepViewed(OnboardingStep.ALERT_SETUP), analyticsTracker.events.last())
    }

    @Test
    fun `back on the welcome page does nothing`() {
        viewModel.onBack()
        assertEquals(OnboardingPage.WELCOME, state.page)
        assertEquals(2, analyticsTracker.events.size)
    }

    @Test
    fun `near a city without a city blocks the setup page`() {
        viewModel.onNext()
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.NEAR_CITY, city = null, radiusKm = 250))
        viewModel.onNext()
        assertFalse(state.canLeaveAlertSetup)
        assertEquals(OnboardingPage.ALERT_SETUP, state.page)
    }

    @Test
    fun `permission request and result are tracked`() {
        viewModel.onPermissionRequested()
        viewModel.onPermissionResult(isGranted = false)
        assertTrue(state.isPermissionDenied)
        assertFalse(state.areNotificationsAllowed)
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.NotificationPermissionRequested(SetupContext.ONBOARDING),
            AnalyticsEvent.NotificationPermissionResult(isGranted = false),
        )
        assertEquals(expected, analyticsTracker.events.takeLast(2))
    }

    @Test
    fun `permission granted in the system settings is seen on resume`() {
        areNotificationsAllowed = true
        viewModel.onScreenResumed()
        assertTrue(state.areNotificationsAllowed)
    }

    @Test
    fun `finishing saves the chosen settings, schedules the check and completes`() {
        viewModel.onThresholdChanged(5.0)
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.NEAR_CITY, city = izmir, radiusKm = 100))
        viewModel.onFinish()
        assertEquals(AlertArea.AroundCity(izmir, radiusKm = 100), preferences.alertSettings.area)
        assertEquals(5.0, preferences.alertSettings.magnitudeThreshold)
        assertTrue(preferences.isOnboardingCompleted)
        assertTrue(scheduler.isScheduled)
        assertTrue(viewModel.isCompleted.value)
    }

    @Test
    fun `finishing tracks the choice with the onboarding context`() {
        viewModel.onAreaSelectionChanged(AreaSelection(AreaMode.NEAR_CITY, city = izmir, radiusKm = 100))
        areNotificationsAllowed = true
        viewModel.onScreenResumed()
        viewModel.onFinish()
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.AlertAreaSet(countryCode = "TR", radiusKm = 100, context = SetupContext.ONBOARDING),
            AnalyticsEvent.OnboardingCompleted(threshold = 4.5, radiusKm = 100, isNotificationsGranted = true),
        )
        assertEquals(expected, analyticsTracker.events.takeLast(2))
    }

    @Test
    fun `finishing twice completes once`() {
        viewModel.onFinish()
        viewModel.onFinish()
        assertEquals(1, analyticsTracker.events.count { it is AnalyticsEvent.OnboardingCompleted })
    }

    private fun createViewModel(): OnboardingViewModel =
        OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            completeOnboarding = CompleteOnboardingUseCase(
                preferencesRepository,
                clock,
                SyncAlertScheduleUseCase(preferencesRepository, scheduler),
            ),
            notificationPermissionChecker = { areNotificationsAllowed },
            analyticsTracker = analyticsTracker,
        )
}
