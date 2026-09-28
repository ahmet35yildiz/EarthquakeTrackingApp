package com.ahmetyildiz.quakealert.features.alerts.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.ahmetyildiz.quakealert.core.location.hasLocationPermission
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidDeviceLocationSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceLocationSource {

    private val locationManager: LocationManager? = context.getSystemService(LocationManager::class.java)

    override fun hasPermission(): Boolean = context.hasLocationPermission()

    override fun isLocationEnabled(): Boolean =
        locationManager?.let(LocationManagerCompat::isLocationEnabled) ?: false

    override suspend fun findCurrentLocation(): GeoPoint? {
        val manager: LocationManager = locationManager ?: return null
        if (!hasPermission()) return null
        return requestFromEveryProvider(manager, findUsableProviders(manager)).filterNotNull().firstOrNull()
    }

    private fun requestFromEveryProvider(manager: LocationManager, providers: List<String>): Flow<GeoPoint?> =
        channelFlow {
            providers.forEach { provider -> launch { send(requestCurrentLocation(manager, provider)) } }
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(manager: LocationManager, provider: String): GeoPoint? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(context),
            ) { location: Location? ->
                if (continuation.isActive) continuation.resume(location?.toGeoPointOrNull())
            }
        }

    @SuppressLint("MissingPermission")
    override fun findLastKnownLocation(): GeoPoint? {
        val manager: LocationManager = locationManager ?: return null
        if (!hasPermission()) return null
        return findUsableProviders(manager)
            .mapNotNull(manager::getLastKnownLocation)
            .maxByOrNull(Location::getTime)
            ?.toGeoPointOrNull()
    }

    private fun findUsableProviders(manager: LocationManager): List<String> {
        val enabledProviders: List<String> = manager.getProviders(true)
        return preferredProviders().filter { it in enabledProviders }
    }

    private fun preferredProviders(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        }

    private fun Location.toGeoPointOrNull(): GeoPoint? = GeoPoint.createOrNull(latitude, longitude)
}
