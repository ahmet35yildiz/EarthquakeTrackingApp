package com.ahmetyildiz.quakealert.core.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AlertAreaTest {

    // Center on the equator so a point's distance depends only on its longitude.
    private val area = AlertArea.AroundCity(
        city = City(name = "Center", adminArea = null, countryCode = "XX", location = GeoPoint(0.0, 0.0)),
        radiusKm = RADIUS_KM,
    )

    @Test
    fun `whole world contains every point`() {
        val points: List<GeoPoint> = listOf(GeoPoint(0.0, 0.0), GeoPoint(90.0, 180.0), GeoPoint(-90.0, -180.0))
        assertTrue(points.all(AlertArea.WholeWorld::contains))
    }

    @Test
    fun `around city contains its center`() {
        assertTrue(area.contains(GeoPoint(0.0, 0.0)))
    }

    @Test
    fun `around city contains a point 1 m inside the radius`() {
        assertTrue(area.contains(pointOnEquatorAt(distanceKm = RADIUS_KM - ONE_METER_IN_KM)))
    }

    @Test
    fun `around city does not contain a point 1 m outside the radius`() {
        assertFalse(area.contains(pointOnEquatorAt(distanceKm = RADIUS_KM + ONE_METER_IN_KM)))
    }

    @Test
    fun `around city does not contain a far away point`() {
        assertFalse(area.contains(GeoPoint(0.0, 90.0)))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -50])
    fun `around city rejects a radius that is not positive`(radiusKm: Int) {
        assertThrows<IllegalArgumentException> { area.copy(radiusKm = radiusKm) }
    }

    private fun pointOnEquatorAt(distanceKm: Double): GeoPoint = GeoPoint(0.0, distanceKm / KM_PER_DEGREE_ON_EQUATOR)

    private companion object {
        const val RADIUS_KM: Int = 250
        const val ONE_METER_IN_KM: Double = 0.001
        const val KM_PER_DEGREE_ON_EQUATOR: Double = 2 * Math.PI * 6371.0 / 360
    }
}
