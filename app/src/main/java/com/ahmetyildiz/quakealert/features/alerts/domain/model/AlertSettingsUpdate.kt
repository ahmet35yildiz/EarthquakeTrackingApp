package com.ahmetyildiz.quakealert.features.alerts.domain.model

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings

data class AlertSettingsUpdate(
    val previous: AlertSettings,
    val updated: AlertSettings,
) {

    val isChanged: Boolean
        get() = previous != updated
}
