package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class PreviewRecentAlertMatchesUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val alertMatcher: AlertMatcher,
    private val clock: Clock,
) {

    fun observeRecentEarthquakes(): Flow<List<Earthquake>> = earthquakeRepository.observeCachedEarthquakes()

    fun preview(recentEarthquakes: List<Earthquake>, choice: AlertChoice): AlertPreview {
        val periodStart: Instant = clock.now() - AlertConfig.PREVIEW_PERIOD
        val matchCount: Int = recentEarthquakes.count { earthquake ->
            isRecorded(earthquake) &&
                !earthquake.time.isBefore(periodStart) &&
                alertMatcher.matchesThresholdAndArea(earthquake, choice)
        }
        return AlertPreview(matchCount = matchCount, period = AlertConfig.PREVIEW_PERIOD)
    }

    private fun isRecorded(earthquake: Earthquake): Boolean =
        !earthquake.id.startsWith(SimulateAlertUseCase.SIMULATED_ID_PREFIX)
}
