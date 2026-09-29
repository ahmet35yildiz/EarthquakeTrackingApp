package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import javax.inject.Inject

class FetchStatisticsEarthquakesUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(period: StatisticsPeriod): AppResult<List<Earthquake>> {
        val query = EarthquakeQuery(
            startTime = period.startTime(clock.now(), clock.zone()),
            minMagnitude = EarthquakesConfig.RECENT_MIN_MAGNITUDE,
        )
        return earthquakeRepository.fetchEarthquakes(query)
    }
}
