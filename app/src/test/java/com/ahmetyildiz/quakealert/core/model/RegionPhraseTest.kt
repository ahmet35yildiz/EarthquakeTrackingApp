package com.ahmetyildiz.quakealert.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RegionPhraseTest {

    @Test
    fun `side of a place drops the article`() {
        assertEquals(
            RegionPhrase.SideOf(RegionSide.SOUTH, "Fiji Islands"),
            RegionPhrase.parse("south of the Fiji Islands"),
        )
        assertEquals(
            RegionPhrase.SideOf(RegionSide.SOUTHEAST, "Loyalty Islands"),
            RegionPhrase.parse("southeast of the Loyalty Islands"),
        )
        assertEquals(RegionPhrase.SideOf(RegionSide.SOUTH, "Lombok"), RegionPhrase.parse("South of Lombok"))
    }

    @Test
    fun `lower case adjectives name a part of a place`() {
        assertEquals(
            RegionPhrase.SideOf(RegionSide.NORTH, "Mid-Atlantic Ridge"),
            RegionPhrase.parse("northern Mid-Atlantic Ridge"),
        )
        assertEquals(RegionPhrase.SideOf(RegionSide.CENTRAL, "Alaska"), RegionPhrase.parse("central Alaska"))
        assertEquals(RegionPhrase.SideOf(RegionSide.SOUTH, "Qinghai"), RegionPhrase.parse("Southern Qinghai"))
    }

    @Test
    fun `region, coast and island forms are read`() {
        assertEquals(RegionPhrase.Region("Iceland"), RegionPhrase.parse("Iceland region"))
        assertEquals(RegionPhrase.OffCoast("Oregon"), RegionPhrase.parse("off the coast of Oregon"))
        assertEquals(RegionPhrase.OffCoast("Aisen"), RegionPhrase.parse("Off the coast of Aisen"))
        assertEquals(
            RegionPhrase.OffCoast("Kamchatka Peninsula", CompassDirection.EAST),
            RegionPhrase.parse("off the east coast of the Kamchatka Peninsula"),
        )
        assertEquals(RegionPhrase.Islands("Kermadec"), RegionPhrase.parse("Kermadec Islands"))
        assertEquals(RegionPhrase.Island("Macquarie"), RegionPhrase.parse("Macquarie Island"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Northern Mariana Islands region",
            "Mid-Indian Ridge",
            "off the far coast of Honshu",
            "Preston",
        ],
    )
    fun `proper names and unknown forms stay plain unless a region suffix is present`(text: String) {
        val expected: RegionPhrase = if (text.endsWith(" region")) {
            RegionPhrase.Region(text.removeSuffix(" region"))
        } else {
            RegionPhrase.Plain(text)
        }
        assertEquals(expected, RegionPhrase.parse(text))
    }
}
