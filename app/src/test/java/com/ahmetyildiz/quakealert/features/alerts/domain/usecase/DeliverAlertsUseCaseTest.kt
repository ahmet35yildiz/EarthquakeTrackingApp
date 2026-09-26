package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertDelivery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeNotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class DeliverAlertsUseCaseTest {

    private val now: Instant = Instant.parse("2026-09-26T12:00:00Z")
    private val notifiedRepository = FakeNotifiedEarthquakeRepository()
    private val notifier = FakeAlertNotifier()
    private var areNotificationsAllowed: Boolean = true
    private val useCase = DeliverAlertsUseCase(
        notifiedRepository,
        AlertMatcher(),
        NotifyAlertsUseCase(notifier, { areNotificationsAllowed }, FakeAnalyticsTracker()),
    )
    private val criteria = AlertMatchCriteria(AlertSettings.DEFAULT, now - Duration.ofHours(1), emptySet(), now)
    private val quake: Earthquake = EarthquakeFixtures.earthquake(id = "q", magnitude = 5.0, time = now)

    @Test
    fun `shown matches are remembered`() = runTest {
        assertEquals(AlertDelivery(matched = 1, result = AlertNotificationResult.POSTED), useCase(listOf(quake), criteria))
        assertEquals(now, notifiedRepository.notifiedAt["q"])
    }

    @Test
    fun `remembered earthquakes do not match again`() = runTest {
        useCase(listOf(quake), criteria)
        val second: AlertDelivery = useCase(listOf(quake), criteria)
        assertEquals(0, second.matched)
        assertEquals(1, notifier.shownAlerts.size)
    }

    @Test
    fun `suppressed matches are not remembered`() = runTest {
        areNotificationsAllowed = false
        val delivery: AlertDelivery = useCase(listOf(quake), criteria)
        assertEquals(0, delivery.notified)
        assertTrue(notifiedRepository.notifiedAt.isEmpty())
    }
}
