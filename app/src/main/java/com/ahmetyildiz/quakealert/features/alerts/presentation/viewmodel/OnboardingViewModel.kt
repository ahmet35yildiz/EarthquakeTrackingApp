package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.OnboardingStep
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.notification.NotificationAccessChecker
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val notificationAccessChecker: NotificationAccessChecker,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val state = MutableStateFlow(restoreState())
    private val mutableIsCompleted = MutableStateFlow(false)

    val uiState: StateFlow<OnboardingUiState> = state.asStateFlow()
    val isCompleted: StateFlow<Boolean> = mutableIsCompleted.asStateFlow()

    init {
        if (!savedStateHandle.contains(KEY_PAGE)) {
            analyticsTracker.track(AnalyticsEvent.OnboardingStarted)
            savedStateHandle.saveAreaSelection(state.value.areaSelection)
            showPage(OnboardingPage.WELCOME)
        }
    }

    fun onNext() {
        when (state.value.page) {
            OnboardingPage.WELCOME -> showPage(OnboardingPage.ALERT_SETUP)
            OnboardingPage.ALERT_SETUP -> if (state.value.canLeaveAlertSetup) showPage(OnboardingPage.NOTIFICATIONS)
            OnboardingPage.NOTIFICATIONS -> onFinish()
        }
    }

    fun onBack() {
        val previous: OnboardingPage = OnboardingPage.entries.getOrNull(state.value.page.ordinal - 1) ?: return
        showPage(previous)
    }

    fun onThresholdChanged(threshold: Double) {
        savedStateHandle[KEY_THRESHOLD] = threshold
        state.update { it.copy(threshold = threshold) }
    }

    fun onAreaSelectionChanged(selection: AreaSelection) {
        savedStateHandle.saveAreaSelection(selection)
        state.update { it.copy(areaSelection = selection) }
    }

    fun onScreenResumed() {
        state.update { it.copy(notificationAccess = notificationAccessChecker.getAlertNotificationAccess()) }
    }

    fun onPermissionRequested() {
        analyticsTracker.track(AnalyticsEvent.NotificationPermissionRequested(SetupContext.ONBOARDING))
    }

    fun onPermissionResult(isGranted: Boolean) {
        analyticsTracker.track(AnalyticsEvent.NotificationPermissionResult(isGranted))
        state.update {
            it.copy(notificationAccess = notificationAccessChecker.getAlertNotificationAccess(), isPermissionDenied = !isGranted)
        }
    }

    fun onFinish() {
        val current: OnboardingUiState = state.value
        val area: AlertArea = current.areaSelection.toAlertAreaOrNull() ?: return
        if (current.isFinishing) return
        state.update { it.copy(isFinishing = true) }
        viewModelScope.launch {
            val settings = AlertSettings(isEnabled = true, magnitudeThreshold = current.threshold, area = area)
            trackCompletion(completeOnboarding(settings), current.notificationAccess.isAllowed)
            mutableIsCompleted.value = true
        }
    }

    private fun restoreState(): OnboardingUiState {
        val areaSelection: AreaSelection? = savedStateHandle.restoreAreaSelectionOrNull()
        return OnboardingUiState(
            page = restorePage(hasRestoredArea = areaSelection?.toAlertAreaOrNull() != null),
            threshold = savedStateHandle.get<Double>(KEY_THRESHOLD) ?: AlertConfig.DEFAULT_THRESHOLD,
            areaSelection = areaSelection ?: AreaSelection.from(AlertArea.WholeWorld),
            notificationAccess = notificationAccessChecker.getAlertNotificationAccess(),
        )
    }

    private fun restorePage(hasRestoredArea: Boolean): OnboardingPage {
        val savedPage: OnboardingPage =
            savedStateHandle.get<String>(KEY_PAGE)?.let(OnboardingPage::valueOf) ?: OnboardingPage.WELCOME
        if (savedPage != OnboardingPage.NOTIFICATIONS || hasRestoredArea) return savedPage
        savedStateHandle[KEY_PAGE] = OnboardingPage.ALERT_SETUP.name
        return OnboardingPage.ALERT_SETUP
    }

    private fun showPage(page: OnboardingPage) {
        savedStateHandle[KEY_PAGE] = page.name
        state.update { it.copy(page = page) }
        analyticsTracker.track(AnalyticsEvent.OnboardingStepViewed(page.toAnalyticsStep()))
    }

    private fun trackCompletion(update: AlertSettingsUpdate, areNotificationsAllowed: Boolean) {
        update.toAnalyticsEvents(SetupContext.ONBOARDING).forEach(analyticsTracker::track)
        val radiusKm: Int? = (update.updated.area as? AlertArea.AroundCity)?.radiusKm
        analyticsTracker.track(
            AnalyticsEvent.OnboardingCompleted(update.updated.magnitudeThreshold, radiusKm, areNotificationsAllowed),
        )
    }

    private fun OnboardingPage.toAnalyticsStep(): OnboardingStep =
        when (this) {
            OnboardingPage.WELCOME -> OnboardingStep.WELCOME
            OnboardingPage.ALERT_SETUP -> OnboardingStep.ALERT_SETUP
            OnboardingPage.NOTIFICATIONS -> OnboardingStep.NOTIFICATIONS
        }

    private companion object {
        const val KEY_PAGE: String = "onboarding_page"
        const val KEY_THRESHOLD: String = "onboarding_threshold"
    }
}
