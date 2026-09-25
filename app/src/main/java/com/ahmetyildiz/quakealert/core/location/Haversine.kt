package com.ahmetyildiz.quakealert.core.location

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM: Double = 6371.0

fun GeoPoint.distanceKmTo(other: GeoPoint): Double {
    val latitudeDelta: Double = Math.toRadians(other.latitude - latitude)
    val longitudeDelta: Double = Math.toRadians(other.longitude - longitude)
    val haversine: Double = sin(latitudeDelta / 2).pow(2) +
        cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(longitudeDelta / 2).pow(2)
    return 2 * EARTH_RADIUS_KM * asin(sqrt(haversine.coerceAtMost(1.0)))
}
