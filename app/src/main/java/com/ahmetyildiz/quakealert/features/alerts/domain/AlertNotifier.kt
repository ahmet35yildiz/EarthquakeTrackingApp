package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert

interface AlertNotifier {

    fun showAlerts(alerts: List<EarthquakeAlert>)

    fun showSummary(alerts: List<EarthquakeAlert>, magnitudeThreshold: Double)

    companion object {
        const val SUMMARY_EVENT_ID: String = "summary"
    }
}
