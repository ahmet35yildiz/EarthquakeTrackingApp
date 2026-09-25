package com.ahmetyildiz.quakealert.core.location

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HaversineTest {

    @ParameterizedTest(name = "({0}, {1}) → ({2}, {3}) = {4} km")
    @CsvSource(
        "0.0, 0.0, 0.0, 1.0, 111.195",
        "0.0, 0.0, 90.0, 0.0, 10007.543",
        "0.0, 0.0, 0.0, 180.0, 20015.087",
        "0.0, 179.0, 0.0, -179.0, 222.390",
        "51.5074, -0.1278, 48.8566, 2.3522, 343.556",
    )
    fun `distance matches the great-circle distance`(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
        expectedKm: Double,
    ) {
        val from = GeoPoint(fromLatitude, fromLongitude)
        val to = GeoPoint(toLatitude, toLongitude)
        val distanceKm: Double = from.distanceKmTo(to)
        assertEquals(expectedKm, distanceKm, TOLERANCE_KM)
    }

    @Test
    fun `distance to the same point is zero`() {
        val izmir = GeoPoint(38.4237, 27.1428)
        assertEquals(0.0, izmir.distanceKmTo(izmir))
    }

    @Test
    fun `distance is symmetric`() {
        val istanbul = GeoPoint(41.0082, 28.9784)
        val izmir = GeoPoint(38.4237, 27.1428)
        assertEquals(istanbul.distanceKmTo(izmir), izmir.distanceKmTo(istanbul), TOLERANCE_KM)
    }

    private companion object {
        const val TOLERANCE_KM: Double = 0.001
    }
}
