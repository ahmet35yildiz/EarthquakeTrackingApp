package com.ahmetyildiz.quakealert.core.model

import com.ahmetyildiz.quakealert.core.location.distanceKmTo

sealed interface AlertArea {

    fun contains(point: GeoPoint): Boolean

    data object WholeWorld : AlertArea {

        override fun contains(point: GeoPoint): Boolean = true
    }

    data class AroundCity(val city: City, val radiusKm: Int) : AlertArea {

        init {
            require(radiusKm > 0) { "Radius must be positive: $radiusKm" }
        }

        override fun contains(point: GeoPoint): Boolean = city.location.distanceKmTo(point) <= radiusKm
    }
}
