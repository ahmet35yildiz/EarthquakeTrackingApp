package com.ahmetyildiz.quakealert.features.alerts.domain.repository

import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.GeoPoint

interface DeviceLocationRepository {

    suspend fun getCurrentLocation(): AppResult<GeoPoint>
}
