package com.ahmetyildiz.quakealert.core.location

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Mean Earth radius; the spherical model is accurate to about 0.5 %, plenty for "how far away was it". */
private const val EARTH_RADIUS_KM: Double = 6371.0

/** Great-circle distance to [other] in kilometres (haversine formula). */
fun GeoPoint.distanceKmTo(other: GeoPoint): Double {
    val latitudeDelta: Double = Math.toRadians(other.latitude - latitude)
    val longitudeDelta: Double = Math.toRadians(other.longitude - longitude)
    val haversine: Double = sin(latitudeDelta / 2).pow(2) +
        cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(longitudeDelta / 2).pow(2)
    // Rounding can push the value a hair above 1 for antipodal points, which would make asin return NaN.
    return 2 * EARTH_RADIUS_KM * asin(sqrt(haversine.coerceAtMost(1.0)))
}
