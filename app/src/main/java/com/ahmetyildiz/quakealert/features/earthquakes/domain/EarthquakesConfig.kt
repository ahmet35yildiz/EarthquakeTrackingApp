package com.ahmetyildiz.quakealert.features.earthquakes.domain

import java.time.Duration

object EarthquakesConfig {
    val RECENT_PERIOD: Duration = Duration.ofDays(7)
    const val RECENT_MIN_MAGNITUDE: Double = 2.5
    val CACHE_STALE_AFTER: Duration = Duration.ofMinutes(5)
    const val STATISTICS_TOP_REGION_COUNT: Int = 5
}
