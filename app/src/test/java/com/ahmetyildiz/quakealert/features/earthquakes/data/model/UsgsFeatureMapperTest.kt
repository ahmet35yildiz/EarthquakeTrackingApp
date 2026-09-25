package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Magnitude
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant

class UsgsFeatureMapperTest {

    private val features: List<UsgsFeatureDto> = UsgsFixtures.featureCollection().features

    @Test
    fun `complete feature maps every field`() {
        val earthquake: Earthquake? = features[0].toEarthquakeOrNull()
        val expected = Earthquake(
            id = "us7000abcd",
            magnitude = Magnitude(value = 5.3, type = "mww"),
            place = "15 km SSW of Hilvan, Turkey",
            time = Instant.ofEpochMilli(1790358888110),
            location = GeoPoint(latitude = 37.4, longitude = 38.9),
            depthKm = 10.0,
            detailUrl = "https://earthquake.usgs.gov/earthquakes/eventpage/us7000abcd",
            isReviewed = true,
            hasTsunamiFlag = true,
            feltReportCount = 12,
        )
        assertEquals(expected, earthquake)
    }

    @Test
    fun `missing magnitude, place and felt reports stay empty`() {
        val earthquake: Earthquake? = features[1].toEarthquakeOrNull()
        assertNull(earthquake?.magnitude)
        assertNull(earthquake?.place)
        assertNull(earthquake?.feltReportCount)
    }

    @Test
    fun `depth comes from the third coordinate and can be above sea level`() {
        val earthquake: Earthquake? = features[1].toEarthquakeOrNull()
        assertEquals(-1.07000005245209, earthquake?.depthKm)
    }

    @Test
    fun `automatic event without tsunami flag is mapped as such`() {
        val earthquake: Earthquake? = features[1].toEarthquakeOrNull()
        assertEquals(listOf(false, false), listOf(earthquake?.isReviewed, earthquake?.hasTsunamiFlag))
    }

    @Test
    fun `event that is not an earthquake is dropped`() {
        assertNull(features[2].toEarthquakeOrNull())
    }

    @Test
    fun `feature without geometry is dropped`() {
        assertNull(features[0].copy(geometry = null).toEarthquakeOrNull())
    }

    @Test
    fun `feature without depth is dropped`() {
        val feature: UsgsFeatureDto = features[0].copy(geometry = UsgsGeometryDto(coordinates = listOf(38.9, 37.4)))
        assertNull(feature.toEarthquakeOrNull())
    }

    @ParameterizedTest
    @ValueSource(doubles = [90.5, -91.0])
    fun `feature with an impossible latitude is dropped`(latitude: Double) {
        val geometry = UsgsGeometryDto(coordinates = listOf(38.9, latitude, 10.0))
        val feature: UsgsFeatureDto = features[0].copy(geometry = geometry)
        assertNull(feature.toEarthquakeOrNull())
    }

    @Test
    fun `single event response maps like a list item`() {
        val earthquake: Earthquake? = UsgsFixtures.feature().toEarthquakeOrNull()
        assertEquals("us7000efgh", earthquake?.id)
        assertEquals(Magnitude(value = 4.7, type = "mb"), earthquake?.magnitude)
    }
}
