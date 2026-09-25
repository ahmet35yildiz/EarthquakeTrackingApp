package com.ahmetyildiz.quakealert.core.model

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GeoPointTest {

    @ParameterizedTest
    @CsvSource("90.0, 180.0", "-90.0, -180.0", "0.0, 0.0")
    fun `accepts coordinates within range, bounds included`(latitude: Double, longitude: Double) {
        assertDoesNotThrow { GeoPoint(latitude, longitude) }
    }

    @ParameterizedTest
    @CsvSource("90.1, 0.0", "-90.1, 0.0", "0.0, 180.1", "0.0, -180.1", "NaN, 0.0")
    fun `rejects coordinates out of range`(latitude: Double, longitude: Double) {
        assertThrows<IllegalArgumentException> { GeoPoint(latitude, longitude) }
    }
}
