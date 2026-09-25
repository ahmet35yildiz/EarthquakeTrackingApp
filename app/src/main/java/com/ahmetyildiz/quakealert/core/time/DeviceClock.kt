package com.ahmetyildiz.quakealert.core.time

import java.time.Instant
import javax.inject.Inject

class DeviceClock @Inject constructor() : Clock {

    override fun now(): Instant = Instant.now()
}
