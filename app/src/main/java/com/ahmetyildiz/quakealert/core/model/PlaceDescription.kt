package com.ahmetyildiz.quakealert.core.model

sealed interface PlaceDescription {

    data class NearPlace(
        val distanceKm: Double,
        val direction: CompassDirection,
        val placeName: String,
    ) : PlaceDescription

    data class Named(val name: String) : PlaceDescription

    companion object {
        private val NEAR_PLACE_PATTERN: Regex = Regex("""^(\d+(?:\.\d+)?) km ([NSEW]{1,3}) of (.+)$""")

        fun parse(text: String): PlaceDescription {
            val trimmedText: String = text.trim()
            val match: MatchResult = NEAR_PLACE_PATTERN.matchEntire(trimmedText) ?: return Named(trimmedText)
            val (distance: String, abbreviation: String, placeName: String) = match.destructured
            val direction: CompassDirection = CompassDirection.fromAbbreviation(abbreviation)
                ?: return Named(trimmedText)
            return NearPlace(distanceKm = distance.toDouble(), direction = direction, placeName = placeName)
        }
    }
}
