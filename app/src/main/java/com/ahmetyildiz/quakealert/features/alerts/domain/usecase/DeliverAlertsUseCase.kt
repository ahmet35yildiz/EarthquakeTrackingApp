package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertDelivery
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertNotificationResult
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.NotifiedEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import javax.inject.Inject

class DeliverAlertsUseCase @Inject constructor(
    private val notifiedEarthquakeRepository: NotifiedEarthquakeRepository,
    private val alertMatcher: AlertMatcher,
    private val notifyAlerts: NotifyAlertsUseCase,
) {

    suspend operator fun invoke(candidates: List<Earthquake>, criteria: AlertMatchCriteria): AlertDelivery {
        val notifiedIds: Set<String> = notifiedEarthquakeRepository.findNotifiedIds(candidates.map(Earthquake::id))
        val matches: List<Earthquake> = alertMatcher.findMatches(candidates, criteria.copy(notifiedEarthquakeIds = notifiedIds))
        val result: AlertNotificationResult = notifyAlerts(matches, criteria.settings)
        if (result == AlertNotificationResult.POSTED) {
            notifiedEarthquakeRepository.markNotified(matches.map(Earthquake::id), criteria.checkedAt)
        }
        return AlertDelivery(matched = matches.size, result = result)
    }
}
