package com.ahmetyildiz.quakealert.core.time

import java.time.Instant
import java.time.ZoneId

fun interface Clock {

    fun now(): Instant

    fun zone(): ZoneId = ZoneId.systemDefault()
}
