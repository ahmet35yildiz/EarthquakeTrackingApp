package com.ahmetyildiz.quakealert.core.preferences

import com.ahmetyildiz.quakealert.core.model.AlertArea

/** What the user wants to be alerted about. */
data class AlertSettings(
    val isEnabled: Boolean,
    /** Minimum magnitude that triggers an alert (inclusive). */
    val magnitudeThreshold: Double,
    val area: AlertArea,
) {

    companion object {
        /** Used until the user saves their own choice (onboarding or alert settings). */
        const val DEFAULT_MAGNITUDE_THRESHOLD: Double = 4.5

        val DEFAULT: AlertSettings = AlertSettings(
            isEnabled = true,
            magnitudeThreshold = DEFAULT_MAGNITUDE_THRESHOLD,
            area = AlertArea.WholeWorld,
        )
    }
}
