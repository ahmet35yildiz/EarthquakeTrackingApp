package com.ahmetyildiz.quakealert.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val CURRENT_TIME_REFRESH_INTERVAL: Duration = 1.minutes

@Composable
fun rememberCurrentTime(): Instant {
    val currentTime: Instant by produceState(initialValue = Instant.now()) {
        while (true) {
            delay(CURRENT_TIME_REFRESH_INTERVAL)
            value = Instant.now()
        }
    }
    return currentTime
}
