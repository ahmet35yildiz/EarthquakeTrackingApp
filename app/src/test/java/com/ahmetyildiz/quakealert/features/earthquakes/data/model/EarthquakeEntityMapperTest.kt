package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.database.EarthquakeEntity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EarthquakeEntityMapperTest {

    @Test
    fun `earthquakes survive a round trip through the cache entity`() {
        val earthquakes: List<Earthquake> =
            UsgsFixtures.featureCollection().features.mapNotNull { it.toEarthquakeOrNull() }
        val roundTripped: List<Earthquake> = earthquakes.map { it.toEntity().toEarthquake() }
        assertEquals(earthquakes, roundTripped)
    }

    @Test
    fun `entity stores magnitude value and type in separate columns`() {
        val entity: EarthquakeEntity = requireNotNull(UsgsFixtures.feature().toEarthquakeOrNull()).toEntity()
        assertEquals(listOf<Any?>(4.7, "mb"), listOf(entity.magnitude, entity.magnitudeType))
    }
}
