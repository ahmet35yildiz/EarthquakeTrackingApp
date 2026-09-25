package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.CacheFreshness
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration

class CheckCacheFreshnessUseCaseTest {

    private val clock = FakeClock()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val useCase = CheckCacheFreshnessUseCase(preferencesRepository, clock)

    @Test
    fun `never refreshed cache is missing`() = runTest {
        assertEquals(CacheFreshness.MISSING, useCase())
    }

    @ParameterizedTest(name = "refreshed {0} s ago → {1}")
    @CsvSource("0, FRESH", "299, FRESH", "300, FRESH", "301, STALE", "86400, STALE")
    fun `cache becomes stale only after five minutes`(secondsAgo: Long, expected: CacheFreshness) = runTest {
        preferencesRepository.setLastRefreshedAt(clock.now())
        clock.advanceBy(Duration.ofSeconds(secondsAgo))
        assertEquals(expected, useCase())
    }
}
