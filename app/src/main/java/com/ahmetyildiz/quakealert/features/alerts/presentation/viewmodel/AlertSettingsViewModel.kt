package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.notification.NotificationPermissionChecker
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.ObserveAlertSettingsUseCase
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.UpdateAlertSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertSettingsViewModel @Inject constructor(
    observeAlertSettings: ObserveAlertSettingsUseCase,
    private val updateAlertSettings: UpdateAlertSettingsUseCase,
    private val notificationPermissionChecker: NotificationPermissionChecker,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val areaDraft = MutableStateFlow<AreaSelection?>(null)
    private val areNotificationsAllowed = MutableStateFlow(notificationPermissionChecker.areNotificationsAllowed())
    private val eventChannel = MutableSharedFlow<AlertSettingsEvent>(extraBufferCapacity = 1)

    val uiState: StateFlow<AlertSettingsUiState> = combine(
        observeAlertSettings(),
        areaDraft,
        areNotificationsAllowed,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AlertSettingsUiState())

    val events: SharedFlow<AlertSettingsEvent> = eventChannel.asSharedFlow()

    fun onScreenResumed() {
        areNotificationsAllowed.value = notificationPermissionChecker.areNotificationsAllowed()
    }

    fun onAlertsToggled(isEnabled: Boolean) {
        update { it.copy(isEnabled = isEnabled) }
    }

    fun onThresholdChanged(threshold: Double) {
        update { it.copy(magnitudeThreshold = threshold) }
    }

    fun onAreaSelectionChanged(selection: AreaSelection) {
        areaDraft.value = selection
        val area: AlertArea = selection.toAlertAreaOrNull() ?: return
        update { it.copy(area = area) }
    }

    private fun update(transform: (AlertSettings) -> AlertSettings) {
        viewModelScope.launch {
            val update: AlertSettingsUpdate = updateAlertSettings(transform)
            if (!update.isChanged) return@launch
            update.toAnalyticsEvents(SetupContext.SETTINGS).forEach(analyticsTracker::track)
            eventChannel.tryEmit(AlertSettingsEvent.Saved)
        }
    }

    private fun toUiState(
        settings: AlertSettings,
        draft: AreaSelection?,
        areNotificationsAllowed: Boolean,
    ): AlertSettingsUiState =
        AlertSettingsUiState(
            isLoading = false,
            settings = settings,
            areaSelection = draft ?: AreaSelection.from(settings.area),
            areNotificationsAllowed = areNotificationsAllowed,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
