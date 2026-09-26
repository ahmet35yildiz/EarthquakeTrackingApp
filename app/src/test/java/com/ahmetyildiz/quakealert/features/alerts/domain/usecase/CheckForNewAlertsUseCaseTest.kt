package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.BackgroundCheckFailureReason
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertCheckResult
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeNotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class CheckForNewAlertsUseCaseTest {

    private val clock = FakeClock(Instant.parse("2026-09-26T12:00:00Z"))
    private val baselineAt: Instant = clock.now() - Duration.ofHours(1)
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val earthquakeRepository = FakeEarthquakeRepository()
    private val notifiedRepository = FakeNotifiedEarthquakeRepository()
    private val notifier = FakeAlertNotifier()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var areNotificationsAllowed: Boolean = true
    private val useCase = CheckForNewAlertsUseCase(
        userPreferencesRepository = preferencesRepository,
        earthquakeRepository = earthquakeRepository,
        notifiedEarthquakeRepository = notifiedRepository,
        deliverAlerts = DeliverAlertsUseCase(
            notifiedRepository,
            AlertMatcher(),
            NotifyAlertsUseCase(notifier, { areNotificationsAllowed }, analyticsTracker),
        ),
        analyticsTracker = analyticsTracker,
        clock = clock,
    )
    private val fresh: Earthquake = quake(id = "fresh", minutesAgo = 10)
    private val beforeBaseline: Earthquake = quake(id = "before-baseline", minutesAgo = 90)

    private val preferences: UserPreferences
        get() = preferencesRepository.userPreferences.value

    @Test
    fun `check is skipped before alert settings were ever saved`() = runTest {
        assertEquals(AlertCheckResult.Skipped, useCase())
        assertTrue(earthquakeRepository.receivedQueries.isEmpty())
    }

    @Test
    fun `check is skipped while alerts are disabled`() = runTest {
        saveSettings(AlertSettings.DEFAULT.copy(isEnabled = false))
        assertEquals(AlertCheckResult.Skipped, useCase())
        assertTrue(earthquakeRepository.receivedQueries.isEmpty())
    }

    @Test
    fun `first check asks for recent events updated since the baseline minus the overlap`() = runTest {
        val area = AlertArea.AroundCity(City("İzmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 100)
        saveSettings(AlertSettings.DEFAULT.copy(magnitudeThreshold = 5.0, area = area))
        useCase()
        val expected = EarthquakeQuery(
            startTime = clock.now() - Duration.ofHours(6),
            minMagnitude = 5.0,
            area = area,
            updatedAfter = baselineAt - Duration.ofMinutes(10),
        )
        assertEquals(listOf(expected), earthquakeRepository.receivedQueries)
    }

    @Test
    fun `later checks ask for events updated since the last check minus the overlap`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        preferencesRepository.setLastCheckedAt(clock.now() - Duration.ofMinutes(15))
        useCase()
        assertEquals(clock.now() - Duration.ofMinutes(25), earthquakeRepository.receivedQueries.single().updatedAfter)
    }

    @Test
    fun `matching events are notified, remembered and tracked`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        earthquakeRepository.remoteEarthquakes = listOf(fresh, beforeBaseline)
        assertEquals(AlertCheckResult.Completed(fetched = 2, matched = 1, notified = 1), useCase())
        assertEquals(listOf("fresh"), notifier.shownAlerts.single().map { it.earthquake.id })
        assertEquals(setOf("fresh"), notifiedRepository.notifiedAt.keys)
        assertEquals(clock.now(), preferences.lastCheckedAt)
        assertEquals(AnalyticsEvent.BackgroundCheckCompleted(2, 1, 1, 0), analyticsTracker.events.last())
    }

    @Test
    fun `an event is never notified twice`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        earthquakeRepository.remoteEarthquakes = listOf(fresh)
        useCase()
        assertEquals(AlertCheckResult.Completed(fetched = 1, matched = 0, notified = 0), useCase())
        assertEquals(1, notifier.shownAlerts.size)
    }

    @Test
    fun `suppressed events are not remembered so they can still alert once allowed`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        earthquakeRepository.remoteEarthquakes = listOf(fresh)
        areNotificationsAllowed = false
        assertEquals(AlertCheckResult.Completed(fetched = 1, matched = 1, notified = 0), useCase())
        assertTrue(notifiedRepository.notifiedAt.isEmpty())
        areNotificationsAllowed = true
        assertEquals(AlertCheckResult.Completed(fetched = 1, matched = 1, notified = 1), useCase())
    }

    @Test
    fun `remembered ids older than the retention are pruned`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        notifiedRepository.notifiedAt["old"] = clock.now() - Duration.ofDays(31)
        notifiedRepository.notifiedAt["recent"] = clock.now() - Duration.ofDays(29)
        useCase()
        assertEquals(setOf("recent"), notifiedRepository.notifiedAt.keys)
    }

    @Test
    fun `network failure asks for a retry and keeps the last check time`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        earthquakeRepository.failure = AppError.Network
        val result = useCase() as AlertCheckResult.Failed
        assertTrue(result.shouldRetry)
        assertNull(preferences.lastCheckedAt)
        assertEquals(listOf(AnalyticsEvent.BackgroundCheckFailed(BackgroundCheckFailureReason.NETWORK)), analyticsTracker.events)
    }

    @Test
    fun `server failure is retried, parsing failure is not`() = runTest {
        saveSettings(AlertSettings.DEFAULT)
        earthquakeRepository.failure = AppError.Server(code = 503)
        assertTrue((useCase() as AlertCheckResult.Failed).shouldRetry)
        earthquakeRepository.failure = AppError.Parsing
        assertFalse((useCase() as AlertCheckResult.Failed).shouldRetry)
        assertEquals(AnalyticsEvent.BackgroundCheckFailed(BackgroundCheckFailureReason.PARSING), analyticsTracker.events.last())
    }

    private suspend fun saveSettings(settings: AlertSettings) {
        preferencesRepository.saveAlertSettings(settings, baselineAt)
    }

    private fun quake(id: String, minutesAgo: Long): Earthquake =
        EarthquakeFixtures.earthquake(id = id, magnitude = 5.0, time = clock.now() - Duration.ofMinutes(minutesAgo))
}
