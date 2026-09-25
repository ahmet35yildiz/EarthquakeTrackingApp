package com.ahmetyildiz.quakealert.core.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.Instant

class RelativeTimeTest {

    private val now: Instant = Instant.parse("2026-09-25T12:00:00Z")

    @ParameterizedTest
    @MethodSource("cases")
    fun `elapsed time is shown in the largest whole unit`(elapsed: Duration, expected: RelativeTime) {
        assertEquals(expected, RelativeTime.between(now.minus(elapsed), now))
    }

    companion object {

        @JvmStatic
        fun cases(): List<Arguments> = listOf(
            argumentSet("future time", Duration.ofSeconds(-30), RelativeTime.JustNow),
            argumentSet("59 s", Duration.ofSeconds(59), RelativeTime.JustNow),
            argumentSet("60 s", Duration.ofSeconds(60), RelativeTime.MinutesAgo(1)),
            argumentSet("59 min 59 s", Duration.ofSeconds(3599), RelativeTime.MinutesAgo(59)),
            argumentSet("1 h", Duration.ofHours(1), RelativeTime.HoursAgo(1)),
            argumentSet("23 h 59 min", Duration.ofMinutes(1439), RelativeTime.HoursAgo(23)),
            argumentSet("1 day", Duration.ofDays(1), RelativeTime.DaysAgo(1)),
            argumentSet("6 days 23 h", Duration.ofHours(167), RelativeTime.DaysAgo(6)),
        )
    }
}
