package com.ahmetyildiz.quakealert.features.emergency.domain

import com.ahmetyildiz.quakealert.features.emergency.domain.model.WhistlePattern
import java.time.Duration

object EmergencyConfig {
    val STROBE_FLASH_DURATION: Duration = Duration.ofMillis(250)
    val STROBE_PAUSE_DURATION: Duration = Duration.ofMillis(250)
    val WHISTLE_PATTERN: WhistlePattern = WhistlePattern(
        frequencyHz = 3000,
        blastCount = 3,
        blastDuration = Duration.ofMillis(500),
        gapDuration = Duration.ofMillis(250),
        pauseDuration = Duration.ofMillis(1500),
    )
}
