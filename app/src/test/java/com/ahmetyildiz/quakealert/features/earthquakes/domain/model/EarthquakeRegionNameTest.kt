package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EarthquakeRegionNameTest {

    @Test
    fun `region is the last part of the place name`() {
        mapOf(
            "12 km SW of Seferihisar, Turkey" to "Turkey",
            "Pangai, Tonga" to "Tonga",
            "Kermadec Islands, New Zealand" to "New Zealand",
            "Fiji region" to "Fiji region",
            "south of the Fiji Islands" to "south of the Fiji Islands",
        ).forEach { (place, region) -> assertEquals(region, EarthquakeRegionName.of(place)) }
    }

    @Test
    fun `USGS abbreviations are written out`() {
        assertEquals("California", EarthquakeRegionName.of("5 km N of Petrolia, CA"))
        assertEquals("Mexico", EarthquakeRegionName.of("10 km W of Ensenada, B.C., MX"))
    }

    @Test
    fun `missing place has no region`() {
        assertNull(EarthquakeRegionName.of(null))
        assertNull(EarthquakeRegionName.of(" "))
    }
}
