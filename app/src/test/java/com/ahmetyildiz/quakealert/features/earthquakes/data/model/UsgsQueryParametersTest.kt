package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Locale

class UsgsQueryParametersTest {

    private val defaultLocale: Locale = Locale.getDefault()

    private val startTime: Instant = Instant.parse("2026-09-18T12:00:00Z")

    @AfterEach
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `whole world query sends only time and magnitude`() {
        val query = EarthquakeQuery(startTime = startTime, minMagnitude = 2.5)
        val parameters: Map<String, String> = query.toUsgsQueryParameters()
        assertEquals(mapOf("starttime" to "2026-09-18T12:00:00Z", "minmagnitude" to "2.5"), parameters)
    }

    @Test
    fun `area and updated-after are sent as native USGS parameters`() {
        val area = AlertArea.AroundCity(
            city = City(name = "Izmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42371, 27.14286)),
            radiusKm = 250,
        )
        val query = EarthquakeQuery(
            startTime = startTime,
            minMagnitude = 4.5,
            area = area,
            updatedAfter = Instant.parse("2026-09-25T11:50:00.500Z"),
        )
        val expected: Map<String, String> = mapOf(
            "starttime" to "2026-09-18T12:00:00Z",
            "minmagnitude" to "4.5",
            "updatedafter" to "2026-09-25T11:50:00.500Z",
            "latitude" to "38.4237",
            "longitude" to "27.1429",
            "maxradiuskm" to "250",
        )
        assertEquals(expected, query.toUsgsQueryParameters())
    }

    @Test
    fun `numbers use a dot even when the device language uses a decimal comma`() {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        val area = AlertArea.AroundCity(
            city = City(name = "Izmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.4237, 27.1428)),
            radiusKm = 100,
        )
        val parameters: Map<String, String> =
            EarthquakeQuery(startTime = startTime, minMagnitude = 4.5, area = area).toUsgsQueryParameters()
        val numbers: List<String?> = listOf("minmagnitude", "latitude", "longitude").map(parameters::get)
        assertEquals(listOf("4.5", "38.4237", "27.1428"), numbers)
    }
}
