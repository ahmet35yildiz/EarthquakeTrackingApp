package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.SimulationOutcomeValue
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertDelivery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulatedAlert
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationOutcome
import com.ahmetyildiz.quakealert.features.alerts.domain.model.SimulationRequest
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class SimulateAlertUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val earthquakeRepository: EarthquakeRepository,
    private val deliverAlerts: DeliverAlertsUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val clock: Clock,
) {

    suspend operator fun invoke(request: SimulationRequest, isScheduled: Boolean = false): SimulatedAlert {
        val simulated: SimulatedAlert = deliver(noMatchOutcome = SimulationOutcome.NOT_MATCHED) { area, now ->
            createEarthquake(request, area, now).also { earthquakeRepository.addToCache(it) }
        }
        return simulated.also { track(it.outcome, isScheduled) }
    }

    suspend fun repeatLast(): SimulatedAlert {
        val last: Earthquake? = findLastSimulated()
        val simulated: SimulatedAlert = if (last == null) {
            SimulatedAlert(null, SimulationOutcome.NOTHING_TO_REPEAT)
        } else {
            deliver(noMatchOutcome = SimulationOutcome.ALREADY_NOTIFIED) { _, _ -> last }
        }
        return simulated.also { track(it.outcome, isScheduled = false) }
    }

    private suspend fun deliver(
        noMatchOutcome: SimulationOutcome,
        provideEarthquake: suspend (AlertArea, Instant) -> Earthquake,
    ): SimulatedAlert {
        val now: Instant = clock.now()
        val preferences: UserPreferences = userPreferencesRepository.userPreferences.first()
        val baselineAt: Instant? = preferences.alertBaselineAt
        if (baselineAt == null || !preferences.alertSettings.isEnabled) return SimulatedAlert(null, SimulationOutcome.ALERTS_OFF)
        val earthquake: Earthquake = provideEarthquake(preferences.alertSettings.area, now)
        val criteria = AlertMatchCriteria(preferences.alertSettings, baselineAt, emptySet(), now)
        return SimulatedAlert(earthquake, deliverAlerts(listOf(earthquake), criteria).toOutcome(noMatchOutcome))
    }

    private suspend fun findLastSimulated(): Earthquake? =
        earthquakeRepository.observeCachedEarthquakes().first()
            .filter { it.id.startsWith(SIMULATED_ID_PREFIX) }
            .maxByOrNull(Earthquake::time)

    private fun createEarthquake(request: SimulationRequest, area: AlertArea, now: Instant): Earthquake =
        Earthquake(
            id = "$SIMULATED_ID_PREFIX${now.toEpochMilli()}",
            magnitude = Magnitude(value = request.magnitude, type = null),
            place = request.place,
            time = now,
            location = locationFor(area, request.distanceFromCityKm),
            depthKm = DEPTH_KM,
            detailUrl = "",
            isReviewed = false,
            hasTsunamiFlag = false,
            feltReportCount = null,
        )

    private fun locationFor(area: AlertArea, distanceFromCityKm: Double): GeoPoint {
        val center: GeoPoint = (area as? AlertArea.AroundCity)?.city?.location ?: return GeoPoint(0.0, 0.0)
        val latitudeDelta: Double = distanceFromCityKm / KM_PER_LATITUDE_DEGREE
        return GeoPoint.createOrNull(center.latitude + latitudeDelta, center.longitude)
            ?: GeoPoint.createOrNull(center.latitude - latitudeDelta, center.longitude)
            ?: center
    }

    private fun AlertDelivery.toOutcome(noMatchOutcome: SimulationOutcome): SimulationOutcome =
        when (result) {
            AlertNotificationResult.SUPPRESSED -> SimulationOutcome.NOTIFICATIONS_OFF
            AlertNotificationResult.POSTED -> SimulationOutcome.POSTED
            AlertNotificationResult.NOTHING_TO_NOTIFY -> noMatchOutcome
        }

    private fun track(outcome: SimulationOutcome, isScheduled: Boolean) {
        analyticsTracker.track(AnalyticsEvent.DeveloperSimulatedAlert(outcome.toAnalyticsValue(), isScheduled))
    }

    private fun SimulationOutcome.toAnalyticsValue(): SimulationOutcomeValue = SimulationOutcomeValue.valueOf(name)

    companion object {
        const val SIMULATED_ID_PREFIX: String = "simulated-"
        private const val DEPTH_KM: Double = 10.0
        private const val KM_PER_LATITUDE_DEGREE: Double = 111.2
    }
}
