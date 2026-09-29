package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig

enum class OnboardingPage { WELCOME, ALERT_SETUP, NOTIFICATIONS }

data class OnboardingUiState(
    val page: OnboardingPage = OnboardingPage.WELCOME,
    val threshold: Double = AlertConfig.DEFAULT_THRESHOLD,
    val areaSelection: AreaSelection = AreaSelection.from(AlertArea.WholeWorld),
    val notificationAccess: NotificationAccess = NotificationAccess.APP_BLOCKED,
    val isPermissionDenied: Boolean = false,
    val isFinishing: Boolean = false,
) {

    val canLeaveAlertSetup: Boolean
        get() = areaSelection.toAlertAreaOrNull() != null
}
