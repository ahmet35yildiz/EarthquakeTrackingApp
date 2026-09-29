package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings

data class AlertSettingsUiState(
    val isLoading: Boolean = true,
    val settings: AlertSettings = AlertSettings.DEFAULT,
    val areaSelection: AreaSelection = AreaSelection.from(AlertArea.WholeWorld),
    val notificationAccess: NotificationAccess = NotificationAccess.ALLOWED,
)
