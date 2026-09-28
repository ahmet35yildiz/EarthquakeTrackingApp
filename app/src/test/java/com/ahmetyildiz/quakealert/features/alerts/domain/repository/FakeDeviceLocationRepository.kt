package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import kotlinx.coroutines.CompletableDeferred

class FakeDeviceLocationRepository(
    var result: AppResult<GeoPoint> = AppResult.Success(GeoPoint(38.46, 27.21)),
) : DeviceLocationRepository {

    var pendingResult: CompletableDeferred<AppResult<GeoPoint>>? = null

    override suspend fun getCurrentLocation(): AppResult<GeoPoint> = pendingResult?.await() ?: result
}
