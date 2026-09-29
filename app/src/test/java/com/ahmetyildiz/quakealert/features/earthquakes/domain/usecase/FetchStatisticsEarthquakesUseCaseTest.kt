package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FetchStatisticsEarthquakesUseCaseTest {

    @Test
    fun `asks USGS for the whole period at the list's minimum magnitude`() = runTest {
        val repository = FakeEarthquakeRepository()
        val clock = FakeClock(Instant.parse("2026-09-29T12:00:00Z"))
        FetchStatisticsEarthquakesUseCase(repository, clock)(StatisticsPeriod.LAST_30_DAYS)
        val expected = EarthquakeQuery(
            startTime = Instant.parse("2026-08-31T00:00:00Z"),
            minMagnitude = EarthquakesConfig.RECENT_MIN_MAGNITUDE,
        )
        assertEquals(listOf(expected), repository.receivedQueries)
    }
}
