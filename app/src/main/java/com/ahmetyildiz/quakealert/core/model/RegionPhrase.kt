package com.ahmetyildiz.quakealert.core.model

enum class RegionSide(val ofWord: String?, val adjective: String?) {
    NORTH(ofWord = "north", adjective = "northern"),
    SOUTH(ofWord = "south", adjective = "southern"),
    EAST(ofWord = "east", adjective = "eastern"),
    WEST(ofWord = "west", adjective = "western"),
    NORTHEAST(ofWord = "northeast", adjective = null),
    NORTHWEST(ofWord = "northwest", adjective = null),
    SOUTHEAST(ofWord = "southeast", adjective = null),
    SOUTHWEST(ofWord = "southwest", adjective = null),
    CENTRAL(ofWord = null, adjective = "central"),
}

sealed interface RegionPhrase {

    data class SideOf(val side: RegionSide, val name: String) : RegionPhrase

    data class Region(val name: String) : RegionPhrase

    data class OffCoast(val name: String, val coast: CompassDirection? = null) : RegionPhrase

    data class Islands(val name: String) : RegionPhrase

    data class Island(val name: String) : RegionPhrase

    data class Plain(val name: String) : RegionPhrase

    companion object {
        private val OFF_COAST_PATTERN: Regex =
            Regex("""^off the (?:([a-z]+) )?coast of (?:the )?(.+)$""", RegexOption.IGNORE_CASE)
        private val REGION_PATTERN: Regex = Regex("""^(.+) region$""")
        private val SIDE_OF_PATTERN: Regex = Regex("""^([a-z]+) of (?:the )?(.+)$""", RegexOption.IGNORE_CASE)
        private val ADJECTIVE_PATTERN: Regex = Regex("""^([a-z]+) (.+)$""", RegexOption.IGNORE_CASE)
        private val COASTS: Map<String, CompassDirection> = mapOf(
            "north" to CompassDirection.NORTH,
            "south" to CompassDirection.SOUTH,
            "east" to CompassDirection.EAST,
            "west" to CompassDirection.WEST,
        )
        private val ISLANDS_PATTERN: Regex = Regex("""^(.+) Islands$""")
        private val ISLAND_PATTERN: Regex = Regex("""^(.+) Island$""")

        fun parse(text: String): RegionPhrase {
            val phrase: String = text.trim()
            return parseOffCoast(phrase)
                ?: parseRegion(phrase)
                ?: parseSideOf(phrase)
                ?: parseAdjective(phrase)
                ?: ISLANDS_PATTERN.matchEntire(phrase)?.let { Islands(it.groupValues[1]) }
                ?: ISLAND_PATTERN.matchEntire(phrase)?.let { Island(it.groupValues[1]) }
                ?: Plain(phrase)
        }

        private fun parseOffCoast(phrase: String): RegionPhrase? {
            val match: MatchResult = OFF_COAST_PATTERN.matchEntire(phrase) ?: return null
            val coastWord: String = match.groupValues[1].lowercase()
            if (coastWord.isEmpty()) return OffCoast(name = match.groupValues[2])
            val coast: CompassDirection = COASTS[coastWord] ?: return null
            return OffCoast(name = match.groupValues[2], coast = coast)
        }

        private fun parseRegion(phrase: String): RegionPhrase? =
            REGION_PATTERN.matchEntire(phrase)?.let { Region(it.groupValues[1]) }

        private fun parseSideOf(phrase: String): RegionPhrase? {
            val match: MatchResult = SIDE_OF_PATTERN.matchEntire(phrase) ?: return null
            val word: String = match.groupValues[1].lowercase()
            val side: RegionSide = RegionSide.entries.firstOrNull { it.ofWord == word } ?: return null
            return SideOf(side = side, name = match.groupValues[2])
        }

        private fun parseAdjective(phrase: String): RegionPhrase? {
            val match: MatchResult = ADJECTIVE_PATTERN.matchEntire(phrase) ?: return null
            val word: String = match.groupValues[1].lowercase()
            val side: RegionSide = RegionSide.entries.firstOrNull { it.adjective == word } ?: return null
            return SideOf(side = side, name = match.groupValues[2])
        }
    }
}
