package com.ahmetyildiz.quakealert.core.preferences

import com.ahmetyildiz.quakealert.core.model.AlertArea

data class AlertSettings(
    val isEnabled: Boolean,
    val magnitudeThreshold: Double,
    val area: AlertArea,
) {

    companion object {
        const val DEFAULT_MAGNITUDE_THRESHOLD: Double = 4.5

        val DEFAULT: AlertSettings = AlertSettings(
            isEnabled = true,
            magnitudeThreshold = DEFAULT_MAGNITUDE_THRESHOLD,
            area = AlertArea.WholeWorld,
        )
    }
}
