package com.ahmetyildiz.quakealert.core.model

enum class CompassDirection(val abbreviation: String) {
    NORTH("N"),
    NORTH_NORTHEAST("NNE"),
    NORTHEAST("NE"),
    EAST_NORTHEAST("ENE"),
    EAST("E"),
    EAST_SOUTHEAST("ESE"),
    SOUTHEAST("SE"),
    SOUTH_SOUTHEAST("SSE"),
    SOUTH("S"),
    SOUTH_SOUTHWEST("SSW"),
    SOUTHWEST("SW"),
    WEST_SOUTHWEST("WSW"),
    WEST("W"),
    WEST_NORTHWEST("WNW"),
    NORTHWEST("NW"),
    NORTH_NORTHWEST("NNW");

    companion object {
        fun fromAbbreviation(abbreviation: String): CompassDirection? =
            entries.firstOrNull { it.abbreviation == abbreviation }
    }
}
