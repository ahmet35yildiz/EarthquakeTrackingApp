package com.ahmetyildiz.quakealert.testing

import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert

class FakeAlertNotifier : AlertNotifier {

    val shownAlerts: MutableList<EarthquakeAlert> = mutableListOf()

    var summaryCount: Int = 0
        private set

    override fun showAlerts(alerts: List<EarthquakeAlert>) {
        shownAlerts += alerts
    }

    override fun showSummary(alerts: List<EarthquakeAlert>, magnitudeThreshold: Double) {
        summaryCount++
    }
}
