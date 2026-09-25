package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.CacheFreshness
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class CheckCacheFreshnessUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(): CacheFreshness {
        val lastRefreshedAt: Instant = userPreferencesRepository.userPreferences.first().lastRefreshedAt
            ?: return CacheFreshness.MISSING
        val age: Duration = Duration.between(lastRefreshedAt, clock.now())
        return if (age > EarthquakesConfig.CACHE_STALE_AFTER) CacheFreshness.STALE else CacheFreshness.FRESH
    }
}
