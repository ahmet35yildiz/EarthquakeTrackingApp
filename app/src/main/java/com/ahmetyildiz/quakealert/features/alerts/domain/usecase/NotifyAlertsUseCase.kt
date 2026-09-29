package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SuppressionReason
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.notification.NotificationAccessChecker
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.distanceFromCityOrNull
import javax.inject.Inject

class NotifyAlertsUseCase @Inject constructor(
    private val alertNotifier: AlertNotifier,
    private val notificationAccessChecker: NotificationAccessChecker,
    private val analyticsTracker: AnalyticsTracker,
) {

    operator fun invoke(earthquakes: List<Earthquake>, settings: AlertSettings): AlertNotificationResult {
        if (earthquakes.isEmpty()) return AlertNotificationResult.NOTHING_TO_NOTIFY
        val access: NotificationAccess = notificationAccessChecker.getAlertNotificationAccess()
        if (!access.isAllowed) {
            analyticsTracker.track(AnalyticsEvent.AlertNotificationSuppressed(access.toSuppressionReason()))
            return AlertNotificationResult.SUPPRESSED
        }
        val alerts: List<EarthquakeAlert> = earthquakes
            .sortedBy(Earthquake::time)
            .map { EarthquakeAlert(earthquake = it, distanceFromCity = settings.area.distanceFromCityOrNull(it)) }
        if (alerts.size > AlertConfig.MAX_INDIVIDUAL_NOTIFICATIONS) {
            alertNotifier.showSummary(alerts, settings.magnitudeThreshold)
            trackPosted(AlertNotifier.SUMMARY_EVENT_ID, alerts.maxOf(::magnitudeOf), alerts.size)
        } else {
            alertNotifier.showAlerts(alerts)
            alerts.forEach { trackPosted(it.earthquake.id, magnitudeOf(it), alerts.size) }
        }
        return AlertNotificationResult.POSTED
    }

    private fun NotificationAccess.toSuppressionReason(): SuppressionReason =
        if (this == NotificationAccess.ALERT_CHANNEL_BLOCKED) {
            SuppressionReason.ALERT_CHANNEL_BLOCKED
        } else {
            SuppressionReason.PERMISSION_DENIED
        }

    private fun magnitudeOf(alert: EarthquakeAlert): Double = alert.earthquake.magnitude?.value ?: 0.0

    private fun trackPosted(eventId: String, magnitude: Double, batchSize: Int) {
        analyticsTracker.track(AnalyticsEvent.AlertNotificationPosted(eventId, magnitude, batchSize))
    }
}
