package com.ahmetyildiz.quakealert.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PlaceDescriptionTest {

    @Test
    fun `distance, direction and place are read from the relative form`() {
        assertEquals(
            PlaceDescription.NearPlace(28.0, CompassDirection.NORTHWEST, "Preston, Nevada"),
            PlaceDescription.parse("28 km NW of Preston, Nevada"),
        )
    }

    @Test
    fun `three letter directions and decimal distances are read`() {
        assertEquals(
            PlaceDescription.NearPlace(2.5, CompassDirection.WEST_SOUTHWEST, "Westley, CA"),
            PlaceDescription.parse("2.5 km WSW of Westley, CA"),
        )
    }

    @Test
    fun `surrounding spaces are ignored`() {
        assertEquals(
            PlaceDescription.NearPlace(5.0, CompassDirection.SOUTH, "Volcano, Hawaii"),
            PlaceDescription.parse(" 5 km S of Volcano, Hawaii "),
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["south of the Fiji Islands", "Kermadec Islands, New Zealand", "Mid-Indian Ridge", "12 km XYZ of Nowhere"],
    )
    fun `other texts are kept as a name`(text: String) {
        assertEquals(PlaceDescription.Named(text), PlaceDescription.parse(text))
    }

    @Test
    fun `every direction abbreviation maps back to its direction`() {
        CompassDirection.entries.forEach { direction ->
            assertEquals(direction, CompassDirection.fromAbbreviation(direction.abbreviation))
        }
    }
}
