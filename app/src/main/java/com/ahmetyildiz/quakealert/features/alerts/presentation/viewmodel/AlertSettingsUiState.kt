package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import java.time.Instant

data class AlertSettingsUiState(
    val isLoading: Boolean = true,
    val settings: AlertSettings = AlertSettings.DEFAULT,
    val areaSelection: AreaSelection = AreaSelection.from(AlertArea.WholeWorld),
    val lastCheckedAt: Instant? = null,
    val areNotificationsAllowed: Boolean = true,
)

sealed interface AlertSettingsEvent {

    data object Saved : AlertSettingsEvent
}
