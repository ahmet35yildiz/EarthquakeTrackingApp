package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ThresholdStepsTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource("2.5, 2.5", "4.49, 4.5", "4.74, 4.5", "4.76, 5.0", "8.0, 8.0", "1.0, 2.5", "9.3, 8.0")
    fun `value snaps to the nearest step inside the range`(value: Float, expected: Double) {
        assertEquals(expected, snapToThresholdStep(value))
    }

    @Test
    fun `slider has one stop between every pair of steps`() {
        val stops: Int = countThresholdSliderSteps() + 2
        val expectedStops: Int = AlertConfig.THRESHOLD_RANGE.let {
            ((it.endInclusive - it.start) / AlertConfig.THRESHOLD_STEP).toInt() + 1
        }
        assertEquals(expectedStops, stops)
        assertEquals(10, countThresholdSliderSteps())
    }
}
