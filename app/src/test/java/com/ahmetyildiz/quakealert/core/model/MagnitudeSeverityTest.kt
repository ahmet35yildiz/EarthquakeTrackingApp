package com.ahmetyildiz.quakealert.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MagnitudeSeverityTest {

    @ParameterizedTest
    @CsvSource(
        "-0.5, MINOR",
        "2.5, MINOR",
        "3.99, MINOR",
        "4.0, LIGHT",
        "4.99, LIGHT",
        "5.0, MODERATE",
        "6.0, STRONG",
        "6.99, STRONG",
        "7.0, MAJOR",
        "9.1, MAJOR",
    )
    fun `magnitude maps to its severity band, lower bound inclusive`(magnitude: Double, expected: MagnitudeSeverity) {
        val severity: MagnitudeSeverity = MagnitudeSeverity.fromMagnitude(magnitude)
        assertEquals(expected, severity)
    }
}
