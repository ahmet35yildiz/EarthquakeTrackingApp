package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class StatisticsPeriod(val dayCount: Int) {
    LAST_7_DAYS(dayCount = 7),
    LAST_30_DAYS(dayCount = 30);

    fun days(now: Instant, zone: ZoneId): List<LocalDate> {
        val today: LocalDate = now.atZone(zone).toLocalDate()
        return (dayCount - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }

    fun startTime(now: Instant, zone: ZoneId): Instant = days(now, zone).first().atStartOfDay(zone).toInstant()
}
