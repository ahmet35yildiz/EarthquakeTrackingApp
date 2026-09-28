package com.ahmetyildiz.quakealert.features.alerts.data.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.data.source.DeviceLocationSource
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.repository.DeviceLocationRepository
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class DeviceLocationRepositoryImpl @Inject constructor(
    private val deviceLocationSource: DeviceLocationSource,
) : DeviceLocationRepository {

    override suspend fun getCurrentLocation(): AppResult<GeoPoint> {
        if (!deviceLocationSource.hasPermission()) return AppResult.Failure(AppError.LocationPermissionDenied)
        if (!deviceLocationSource.isLocationEnabled()) return AppResult.Failure(AppError.LocationDisabled)
        val location: GeoPoint? = findCurrentLocationInTime() ?: deviceLocationSource.findLastKnownLocation()
        return location?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.LocationUnavailable)
    }

    private suspend fun findCurrentLocationInTime(): GeoPoint? =
        withTimeoutOrNull(AlertConfig.CURRENT_LOCATION_TIMEOUT.toMillis()) {
            deviceLocationSource.findCurrentLocation()
        }
}
