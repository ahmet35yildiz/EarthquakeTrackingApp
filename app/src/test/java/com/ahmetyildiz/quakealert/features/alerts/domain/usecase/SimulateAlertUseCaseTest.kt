package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SimulationOutcomeValue
import com.ahmetyildiz.quakealert.core.location.distanceKmTo
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertNotifier
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulatedAlert
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationOutcome
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.FakeNotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import java.time.Duration
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimulateAlertUseCaseTest {

    private val clock = FakeClock()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val earthquakeRepository = FakeEarthquakeRepository()
    private val notifier = FakeAlertNotifier()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var notificationAccess: NotificationAccess = NotificationAccess.ALLOWED
    private val useCase = SimulateAlertUseCase(
        userPreferencesRepository = preferencesRepository,
        earthquakeRepository = earthquakeRepository,
        deliverAlerts = DeliverAlertsUseCase(
            FakeNotifiedEarthquakeRepository(),
            AlertMatcher(),
            NotifyAlertsUseCase(notifier, { notificationAccess }, FakeAnalyticsTracker()),
        ),
        analyticsTracker = analyticsTracker,
        clock = clock,
    )
    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))
    private val izmirSettings: AlertSettings =
        AlertSettings.DEFAULT.copy(magnitudeThreshold = 5.0, area = AlertArea.AroundCity(izmir, radiusKm = 100))

    @Test
    fun `nothing happens before alerts are set up`() = runTest {
        assertEquals(SimulatedAlert(earthquake = null, outcome = SimulationOutcome.ALERTS_OFF), useCase(request()))
        assertTrue(earthquakeRepository.cachedEarthquakes.value.isEmpty())
    }

    @Test
    fun `nothing happens while alerts are disabled`() = runTest {
        save(AlertSettings.DEFAULT.copy(isEnabled = false))
        assertEquals(SimulationOutcome.ALERTS_OFF, useCase(request()).outcome)
    }

    @Test
    fun `matching earthquake uses the given values, is cached and notified`() = runTest {
        save(izmirSettings)
        val simulated: SimulatedAlert = useCase(request(magnitude = 5.5, distanceKm = 60.0))
        val earthquake: Earthquake = simulated.earthquake!!
        assertEquals(SimulationOutcome.POSTED, simulated.outcome)
        assertEquals(5.5, earthquake.magnitude?.value)
        assertEquals(PLACE, earthquake.place)
        assertEquals(clock.now(), earthquake.time)
        assertEquals(60.0, izmir.location.distanceKmTo(earthquake.location), DISTANCE_TOLERANCE_KM)
        assertEquals(listOf(earthquake), earthquakeRepository.cachedEarthquakes.value)
        assertEquals(earthquake.id, notifier.shownAlerts.single().single().earthquake.id)
    }

    @Test
    fun `earthquake below the threshold is not notified`() = runTest {
        save(izmirSettings)
        assertEquals(SimulationOutcome.NOT_MATCHED, useCase(request(magnitude = 4.9)).outcome)
        assertTrue(notifier.shownAlerts.isEmpty())
    }

    @Test
    fun `earthquake outside the radius is not notified`() = runTest {
        save(izmirSettings)
        assertEquals(SimulationOutcome.NOT_MATCHED, useCase(request(distanceKm = 101.0)).outcome)
        assertTrue(notifier.shownAlerts.isEmpty())
    }

    @Test
    fun `whole world ignores the distance`() = runTest {
        save(AlertSettings.DEFAULT)
        assertEquals(SimulationOutcome.POSTED, useCase(request(distanceKm = 15_000.0)).outcome)
    }

    @Test
    fun `outcome and scheduling are tracked`() = runTest {
        save(izmirSettings)
        useCase(request(magnitude = 3.0), isScheduled = true)
        val expected = AnalyticsEvent.DeveloperSimulatedAlert(SimulationOutcomeValue.NOT_MATCHED, isScheduled = true)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `repeating the same earthquake posts no duplicate`() = runTest {
        save(AlertSettings.DEFAULT)
        useCase(request())
        clock.advanceBy(Duration.ofMinutes(1))
        val repeated: SimulatedAlert = useCase.repeatLast()
        assertEquals(SimulationOutcome.ALREADY_NOTIFIED, repeated.outcome)
        assertEquals(1, notifier.shownAlerts.size)
        assertEquals(1, earthquakeRepository.cachedEarthquakes.value.size)
    }

    @Test
    fun `repeat without a simulated earthquake in the cache does nothing`() = runTest {
        save(AlertSettings.DEFAULT)
        assertEquals(SimulationOutcome.NOTHING_TO_REPEAT, useCase.repeatLast().outcome)
        assertTrue(notifier.shownAlerts.isEmpty())
    }

    @Test
    fun `missing permission is reported`() = runTest {
        save(AlertSettings.DEFAULT)
        notificationAccess = NotificationAccess.APP_BLOCKED
        assertEquals(SimulationOutcome.NOTIFICATIONS_OFF, useCase(request()).outcome)
    }

    private suspend fun save(settings: AlertSettings) {
        preferencesRepository.saveAlertSettings(settings, clock.now() - Duration.ofMinutes(5))
    }

    private fun request(magnitude: Double = 5.0, distanceKm: Double = 20.0): SimulationRequest =
        SimulationRequest(place = PLACE, magnitude = magnitude, distanceFromCityKm = distanceKm)

    private companion object {
        const val PLACE: String = "Simulated place"
        const val DISTANCE_TOLERANCE_KM: Double = 0.5
    }
}
