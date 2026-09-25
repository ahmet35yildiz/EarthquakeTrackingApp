package com.ahmetyildiz.quakealert.core.model

import com.ahmetyildiz.quakealert.core.location.distanceKmTo

/** Where earthquakes are relevant to the user: anywhere, or within a radius around a city. */
sealed interface AlertArea {

    /** True when an epicenter at [point] is inside this area. */
    fun contains(point: GeoPoint): Boolean

    data object WholeWorld : AlertArea {

        override fun contains(point: GeoPoint): Boolean = true
    }

    data class AroundCity(val city: City, val radiusKm: Int) : AlertArea {

        init {
            require(radiusKm > 0) { "Radius must be positive: $radiusKm" }
        }

        /** The edge counts as inside: a point exactly [radiusKm] away matches. */
        override fun contains(point: GeoPoint): Boolean = city.location.distanceKmTo(point) <= radiusKm
    }
}
