package com.ahmetyildiz.quakealert.features.emergency.domain

import java.time.Duration

object EmergencyConfig {
    val STROBE_FLASH_DURATION: Duration = Duration.ofMillis(250)
    val STROBE_PAUSE_DURATION: Duration = Duration.ofMillis(250)
}
