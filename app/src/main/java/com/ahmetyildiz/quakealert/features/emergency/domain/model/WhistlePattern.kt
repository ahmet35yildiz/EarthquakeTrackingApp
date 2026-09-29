package com.ahmetyildiz.quakealert.features.emergency.domain.model

import java.time.Duration

data class WhistlePattern(
    val frequencyHz: Int,
    val blastCount: Int,
    val blastDuration: Duration,
    val gapDuration: Duration,
    val pauseDuration: Duration,
)
