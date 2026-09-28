package com.ahmetyildiz.quakealert.features.alerts.data.repository

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.data.source.FakeDeviceLocationSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeviceLocationRepositoryImplTest {

    private val current = GeoPoint(38.46, 27.21)
    private val lastKnown = GeoPoint(39.93, 32.86)
    private val source = FakeDeviceLocationSource(currentLocation = current, lastKnownLocation = lastKnown)
    private val repository = DeviceLocationRepositoryImpl(source)

    @Test
    fun `missing permission fails without asking for a location`() = runTest {
        source.isPermissionGranted = false
        assertEquals(AppResult.Failure(AppError.LocationPermissionDenied), repository.getCurrentLocation())
        assertEquals(0, source.currentLocationRequestCount)
    }

    @Test
    fun `location turned off fails without asking for a location`() = runTest {
        source.isEnabled = false
        assertEquals(AppResult.Failure(AppError.LocationDisabled), repository.getCurrentLocation())
        assertEquals(0, source.currentLocationRequestCount)
    }

    @Test
    fun `current location is preferred`() = runTest {
        assertEquals(AppResult.Success(current), repository.getCurrentLocation())
    }

    @Test
    fun `last known location is used when there is no current location`() = runTest {
        source.currentLocation = null
        assertEquals(AppResult.Success(lastKnown), repository.getCurrentLocation())
    }

    @Test
    fun `last known location is used when the current location takes too long`() = runTest {
        source.isCurrentLocationHanging = true
        assertEquals(AppResult.Success(lastKnown), repository.getCurrentLocation())
    }

    @Test
    fun `no location at all is unavailable`() = runTest {
        source.currentLocation = null
        source.lastKnownLocation = null
        assertEquals(AppResult.Failure(AppError.LocationUnavailable), repository.getCurrentLocation())
    }
}
