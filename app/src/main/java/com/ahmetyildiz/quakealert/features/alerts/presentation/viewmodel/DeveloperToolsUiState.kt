package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import java.time.Instant

enum class DeveloperToolsMessage {
    ALERT_POSTED,
    ALREADY_NOTIFIED,
    NOT_MATCHED,
    NOTIFICATIONS_OFF,
    ALERTS_OFF,
    NOTHING_TO_REPEAT,
    SCHEDULED,
    CHECK_STARTED,
}

data class SimulationForm(
    val magnitude: String = "",
    val distanceKm: String = DEFAULT_DISTANCE_KM,
    val delayMinutes: String = DEFAULT_DELAY_MINUTES,
) {

    val magnitudeValue: Double?
        get() = magnitude.toDecimalOrNull()?.takeIf { it in MAGNITUDE_RANGE }

    val distanceKmValue: Double?
        get() = distanceKm.toDecimalOrNull()?.takeIf { it in DISTANCE_RANGE_KM }

    val delayMinutesValue: Long?
        get() = delayMinutes.trim().toLongOrNull()?.takeIf { it in DELAY_RANGE_MINUTES }

    private fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

    private companion object {
        const val DEFAULT_DISTANCE_KM: String = "20"
        const val DEFAULT_DELAY_MINUTES: String = "1"
        val MAGNITUDE_RANGE: ClosedFloatingPointRange<Double> = 0.0..10.0
        val DISTANCE_RANGE_KM: ClosedFloatingPointRange<Double> = 0.0..20_000.0
        val DELAY_RANGE_MINUTES: LongRange = 0L..60L
    }
}

data class DeveloperToolsUiState(
    val form: SimulationForm = SimulationForm(),
    val cityName: String? = null,
    val message: DeveloperToolsMessage? = null,
    val scheduledFor: Instant? = null,
    val isSimulating: Boolean = false,
) {

    val canSimulate: Boolean
        get() = !isSimulating && form.magnitudeValue != null && (cityName == null || form.distanceKmValue != null)

    val canSchedule: Boolean
        get() = canSimulate && form.delayMinutesValue != null
}
