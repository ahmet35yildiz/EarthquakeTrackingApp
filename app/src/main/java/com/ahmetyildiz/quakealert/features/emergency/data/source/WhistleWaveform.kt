package com.ahmetyildiz.quakealert.features.emergency.data.source

import com.ahmetyildiz.quakealert.features.emergency.domain.model.WhistlePattern
import java.time.Duration
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

object WhistleWaveform {

    private val RAMP_DURATION: Duration = Duration.ofMillis(10)

    fun create(pattern: WhistlePattern, sampleRateHz: Int): ShortArray {
        val blast: ShortArray = tone(pattern.frequencyHz, frameCount(pattern.blastDuration, sampleRateHz), sampleRateHz)
        val gap = ShortArray(frameCount(pattern.gapDuration, sampleRateHz))
        val pause = ShortArray(frameCount(pattern.pauseDuration, sampleRateHz))
        return (1..pattern.blastCount)
            .flatMap { blastNumber -> listOf(blast, if (blastNumber < pattern.blastCount) gap else pause) }
            .reduce(ShortArray::plus)
    }

    private fun tone(frequencyHz: Int, frames: Int, sampleRateHz: Int): ShortArray {
        val rampFrames: Int = frameCount(RAMP_DURATION, sampleRateHz)
        return ShortArray(frames) { frame ->
            val envelope: Double = min(1.0, min(frame, frames - 1 - frame).toDouble() / rampFrames)
            val wave: Double = sin(2 * PI * frequencyHz * frame / sampleRateHz)
            (Short.MAX_VALUE * envelope * wave).toInt().toShort()
        }
    }

    private fun frameCount(duration: Duration, sampleRateHz: Int): Int =
        (duration.toMillis() * sampleRateHz / MILLIS_PER_SECOND).toInt()

    private const val MILLIS_PER_SECOND: Long = 1000
}
