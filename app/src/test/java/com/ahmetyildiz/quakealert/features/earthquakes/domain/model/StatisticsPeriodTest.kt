package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatisticsPeriodTest {

    private val now: Instant = Instant.parse("2026-09-28T22:30:00Z")
    private val istanbul: ZoneId = ZoneId.of("Europe/Istanbul")

    @Test
    fun `days end today in the given time zone`() {
        val days: List<LocalDate> = StatisticsPeriod.LAST_7_DAYS.days(now, istanbul)
        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 9, 23), days.first())
        assertEquals(LocalDate.of(2026, 9, 29), days.last())
    }

    @Test
    fun `period starts at local midnight of its first day`() {
        assertEquals(Instant.parse("2026-08-30T21:00:00Z"), StatisticsPeriod.LAST_30_DAYS.startTime(now, istanbul))
    }
}
