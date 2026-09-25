package com.ahmetyildiz.quakealert.core.time

import java.time.Duration
import java.time.Instant

sealed interface RelativeTime {

    data object JustNow : RelativeTime

    data class MinutesAgo(val minutes: Long) : RelativeTime

    data class HoursAgo(val hours: Long) : RelativeTime

    data class DaysAgo(val days: Long) : RelativeTime

    companion object {
        fun between(time: Instant, now: Instant): RelativeTime {
            val elapsed: Duration = Duration.between(time, now)
            return when {
                elapsed < Duration.ofMinutes(1) -> JustNow
                elapsed < Duration.ofHours(1) -> MinutesAgo(elapsed.toMinutes())
                elapsed < Duration.ofDays(1) -> HoursAgo(elapsed.toHours())
                else -> DaysAgo(elapsed.toDays())
            }
        }
    }
}
