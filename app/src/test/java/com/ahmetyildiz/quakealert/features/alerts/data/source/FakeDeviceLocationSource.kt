package com.ahmetyildiz.quakealert.features.alerts.data.source

import com.ahmetyildiz.quakealert.core.model.GeoPoint
import kotlinx.coroutines.awaitCancellation

class FakeDeviceLocationSource(
    var isPermissionGranted: Boolean = true,
    var isEnabled: Boolean = true,
    var currentLocation: GeoPoint? = null,
    var lastKnownLocation: GeoPoint? = null,
    var isCurrentLocationHanging: Boolean = false,
) : DeviceLocationSource {

    var currentLocationRequestCount: Int = 0

    override fun hasPermission(): Boolean = isPermissionGranted

    override fun isLocationEnabled(): Boolean = isEnabled

    override suspend fun findCurrentLocation(): GeoPoint? {
        currentLocationRequestCount++
        if (isCurrentLocationHanging) awaitCancellation()
        return currentLocation
    }

    override fun findLastKnownLocation(): GeoPoint? = lastKnownLocation
}
