package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SuppressionReason
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.model.EarthquakeAlert
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NotifyAlertsUseCaseTest {

    private val notifier = FakeAlertNotifier()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var notificationAccess: NotificationAccess = NotificationAccess.ALLOWED
    private val useCase = NotifyAlertsUseCase(notifier, { notificationAccess }, analyticsTracker)
    private val center = City(name = "Center", adminArea = null, countryCode = "XX", location = GeoPoint(0.0, 0.0))
    private val older: Earthquake = earthquake(id = "older", magnitude = 5.0, time = "2026-09-25T10:00:00Z")
    private val newer: Earthquake = earthquake(id = "newer", magnitude = 6.0, time = "2026-09-25T11:00:00Z")

    @Test
    fun `nothing is shown without earthquakes`() {
        assertEquals(AlertNotificationResult.NOTHING_TO_NOTIFY, useCase(emptyList(), AlertSettings.DEFAULT))
        assertTrue(notifier.shownAlerts.isEmpty() && notifier.shownSummaries.isEmpty())
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `missing permission suppresses and is tracked once`() {
        notificationAccess = NotificationAccess.APP_BLOCKED
        assertEquals(AlertNotificationResult.SUPPRESSED, useCase(listOf(older, newer), AlertSettings.DEFAULT))
        assertTrue(notifier.shownAlerts.isEmpty() && notifier.shownSummaries.isEmpty())
        val expected = AnalyticsEvent.AlertNotificationSuppressed(SuppressionReason.PERMISSION_DENIED)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `blocked alert channel suppresses with its own reason even when notifications are allowed`() {
        notificationAccess = NotificationAccess.ALERT_CHANNEL_BLOCKED
        assertEquals(AlertNotificationResult.SUPPRESSED, useCase(listOf(older, newer), AlertSettings.DEFAULT))
        assertTrue(notifier.shownAlerts.isEmpty() && notifier.shownSummaries.isEmpty())
        val expected = AnalyticsEvent.AlertNotificationSuppressed(SuppressionReason.ALERT_CHANNEL_BLOCKED)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `up to three alerts are shown one by one, oldest first`() {
        assertEquals(AlertNotificationResult.POSTED, useCase(listOf(newer, older), AlertSettings.DEFAULT))
        assertEquals(listOf("older", "newer"), notifier.shownAlerts.single().map { it.earthquake.id })
        assertTrue(notifier.shownSummaries.isEmpty())
    }

    @Test
    fun `more than three alerts become one summary with the threshold`() {
        val many: List<Earthquake> = (1..4).map { earthquake(id = "id$it", magnitude = 4.0 + it) }
        assertEquals(AlertNotificationResult.POSTED, useCase(many, AlertSettings.DEFAULT))
        val (alerts: List<EarthquakeAlert>, threshold: Double) = notifier.shownSummaries.single()
        assertEquals(4, alerts.size)
        assertEquals(AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD, threshold)
        assertTrue(notifier.shownAlerts.isEmpty())
    }

    @Test
    fun `summary is tracked once with the largest magnitude`() {
        useCase((1..4).map { earthquake(id = "id$it", magnitude = 4.0 + it) }, AlertSettings.DEFAULT)
        val expected = AnalyticsEvent.AlertNotificationPosted(eventId = "summary", magnitude = 8.0, batchSize = 4)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `whole world alerts have no distance`() {
        useCase(listOf(older), AlertSettings.DEFAULT)
        assertNull(notifier.shownAlerts.single().single().distanceFromCity)
    }

    @Test
    fun `area alerts carry the distance to the city`() {
        val settings: AlertSettings = AlertSettings.DEFAULT.copy(area = AlertArea.AroundCity(center, radiusKm = 250))
        val nearby: Earthquake = earthquake(id = "nearby", location = EarthquakeFixtures.pointOnEquatorAt(100.0))
        useCase(listOf(nearby), settings)
        val alert: EarthquakeAlert = notifier.shownAlerts.single().single()
        assertEquals("Center", alert.distanceFromCity?.cityName)
        assertEquals(100.0, alert.distanceFromCity?.distanceKm ?: 0.0, DISTANCE_TOLERANCE_KM)
    }

    @Test
    fun `every individual notification is tracked with the batch size`() {
        useCase(listOf(newer, older), AlertSettings.DEFAULT)
        val expected: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.AlertNotificationPosted(eventId = "older", magnitude = 5.0, batchSize = 2),
            AnalyticsEvent.AlertNotificationPosted(eventId = "newer", magnitude = 6.0, batchSize = 2),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    private fun earthquake(
        id: String,
        magnitude: Double = 5.0,
        time: String = "2026-09-25T10:00:00Z",
        location: GeoPoint = GeoPoint(0.0, 0.0),
    ): Earthquake = EarthquakeFixtures.earthquake(id = id, magnitude = magnitude, location = location, time = Instant.parse(time))

    private companion object {
        const val DISTANCE_TOLERANCE_KM: Double = 0.01
    }
}
