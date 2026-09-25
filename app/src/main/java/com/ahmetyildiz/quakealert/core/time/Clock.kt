package com.ahmetyildiz.quakealert.core.time

import java.time.Instant

/** Source of the current time. Injected everywhere "now" matters so time-based rules can be tested. */
fun interface Clock {

    fun now(): Instant
}
