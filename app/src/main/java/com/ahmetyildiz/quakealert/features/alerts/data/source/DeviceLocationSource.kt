package com.ahmetyildiz.quakealert.features.alerts.data.source

import com.ahmetyildiz.quakealert.core.model.GeoPoint

interface DeviceLocationSource {

    fun hasPermission(): Boolean

    fun isLocationEnabled(): Boolean

    suspend fun findCurrentLocation(): GeoPoint?

    fun findLastKnownLocation(): GeoPoint?
}
