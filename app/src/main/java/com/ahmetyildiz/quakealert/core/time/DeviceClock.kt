package com.ahmetyildiz.quakealert.core.time

import java.time.Instant
import javax.inject.Inject

/** [Clock] that reads the device's wall-clock time. */
class DeviceClock @Inject constructor() : Clock {

    override fun now(): Instant = Instant.now()
}
