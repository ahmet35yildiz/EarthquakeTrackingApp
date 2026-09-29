package com.ahmetyildiz.quakealert.features.emergency.data.source

import com.ahmetyildiz.quakealert.features.emergency.domain.model.WhistlePattern
import java.time.Duration
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WhistleWaveformTest {

    private val pattern = WhistlePattern(
        frequencyHz = 1000,
        blastCount = 3,
        blastDuration = Duration.ofMillis(100),
        gapDuration = Duration.ofMillis(50),
        pauseDuration = Duration.ofMillis(200),
    )
    private val waveform: ShortArray = WhistleWaveform.create(pattern, SAMPLE_RATE_HZ)

    @Test
    fun `one cycle is three blasts, two gaps and the pause`() {
        assertEquals(3 * BLAST_FRAMES + 2 * GAP_FRAMES + PAUSE_FRAMES, waveform.size)
    }

    @Test
    fun `blasts are loud and gaps and the pause are silent`() {
        val blastStarts: List<Int> = List(3) { it * (BLAST_FRAMES + GAP_FRAMES) }
        blastStarts.forEach { start ->
            assertTrue(peak(start, start + BLAST_FRAMES) > LOUD_PEAK)
        }
        assertEquals(0, peak(BLAST_FRAMES, BLAST_FRAMES + GAP_FRAMES))
        assertEquals(0, peak(waveform.size - PAUSE_FRAMES, waveform.size))
    }

    @Test
    fun `blasts fade in and out so the loop does not click`() {
        assertEquals(0, waveform.first().toInt())
        assertTrue(abs(waveform[BLAST_FRAMES - 1].toInt()) < QUIET_EDGE)
    }

    private fun peak(from: Int, to: Int): Int = (from until to).maxOf { abs(waveform[it].toInt()) }

    private companion object {
        const val SAMPLE_RATE_HZ: Int = 8000
        const val BLAST_FRAMES: Int = 800
        const val GAP_FRAMES: Int = 400
        const val PAUSE_FRAMES: Int = 1600
        const val LOUD_PEAK: Int = 30_000
        const val QUIET_EDGE: Int = 1000
    }
}
