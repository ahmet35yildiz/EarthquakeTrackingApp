package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert

class FakeAlertNotifier : AlertNotifier {

    val shownAlerts: MutableList<List<EarthquakeAlert>> = mutableListOf()
    val shownSummaries: MutableList<Pair<List<EarthquakeAlert>, Double>> = mutableListOf()

    override fun showAlerts(alerts: List<EarthquakeAlert>) {
        shownAlerts += alerts
    }

    override fun showSummary(alerts: List<EarthquakeAlert>, magnitudeThreshold: Double) {
        shownSummaries += alerts to magnitudeThreshold
    }
}
