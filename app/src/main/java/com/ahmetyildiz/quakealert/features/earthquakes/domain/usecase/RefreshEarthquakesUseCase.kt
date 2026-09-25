package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.EarthquakeRepository
import java.time.Instant
import javax.inject.Inject

class RefreshEarthquakesUseCase @Inject constructor(
    private val earthquakeRepository: EarthquakeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(): AppResult<Int> {
        val now: Instant = clock.now()
        val query = EarthquakeQuery(
            startTime = now.minus(EarthquakesConfig.RECENT_PERIOD),
            minMagnitude = EarthquakesConfig.RECENT_MIN_MAGNITUDE,
        )
        val result: AppResult<Int> = earthquakeRepository.refreshCache(query)
        if (result is AppResult.Success) userPreferencesRepository.setLastRefreshedAt(now)
        return result
    }
}
