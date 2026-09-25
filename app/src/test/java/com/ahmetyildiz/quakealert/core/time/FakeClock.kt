package com.ahmetyildiz.quakealert.core.time

import java.time.Duration
import java.time.Instant

class FakeClock(var currentTime: Instant = Instant.parse("2026-09-25T12:00:00Z")) : Clock {

    override fun now(): Instant = currentTime

    fun advanceBy(duration: Duration) {
        currentTime += duration
    }
}
