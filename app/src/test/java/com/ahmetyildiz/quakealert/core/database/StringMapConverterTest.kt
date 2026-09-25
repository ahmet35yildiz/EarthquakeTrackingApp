package com.ahmetyildiz.quakealert.core.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringMapConverterTest {

    private val converter = StringMapConverter()

    @Test
    fun `map is stored as a JSON object`() {
        val json: String = converter.fromMap(mapOf("source" to "launcher", "count" to "3"))
        assertEquals("""{"source":"launcher","count":"3"}""", json)
    }

    @Test
    fun `map survives a round trip, including empty maps and special characters`() {
        val maps: List<Map<String, String>> = listOf(
            emptyMap(),
            mapOf("text" to "quotes \" and \\ backslash", "turkish" to "İzmir ığüşöç"),
        )
        val roundTripped: List<Map<String, String>> = maps.map { converter.toMap(converter.fromMap(it)) }
        assertEquals(maps, roundTripped)
    }
}
