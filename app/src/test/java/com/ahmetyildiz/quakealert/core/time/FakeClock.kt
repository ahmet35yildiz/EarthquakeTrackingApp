package com.ahmetyildiz.quakealert.core.time

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class FakeClock(var currentTime: Instant = Instant.parse("2026-09-25T12:00:00Z")) : Clock {

    override fun now(): Instant = currentTime

    override fun zone(): ZoneId = ZoneOffset.UTC

    fun advanceBy(duration: Duration) {
        currentTime += duration
    }
}
