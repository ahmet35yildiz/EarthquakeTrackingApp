package com.ahmetyildiz.quakealert.features.alerts.data.source

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.ahmetyildiz.quakealert.core.di.IoDispatcher
import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddress
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidCityGeocoder @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CityGeocoder {

    override fun isPresent(): Boolean = Geocoder.isPresent()

    override suspend fun findAddresses(locationName: String, locale: Locale): List<GeocodedAddress> {
        val geocoder = Geocoder(context, locale)
        val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            findAddressesAsync(geocoder, locationName)
        } else {
            findAddressesBlocking(geocoder, locationName)
        }
        return addresses.mapNotNull { it.toGeocodedAddressOrNull() }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun findAddressesAsync(geocoder: Geocoder, locationName: String): List<Address> =
        suspendCancellableCoroutine { continuation ->
            val listener = object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    continuation.resume(addresses.toList())
                }

                override fun onError(errorMessage: String?) {
                    continuation.resumeWithException(IOException(errorMessage))
                }
            }
            geocoder.getFromLocationName(locationName, AlertConfig.CITY_SEARCH_MAX_RESULTS, listener)
        }

    @Suppress("DEPRECATION")
    private suspend fun findAddressesBlocking(geocoder: Geocoder, locationName: String): List<Address> =
        withContext(ioDispatcher) {
            geocoder.getFromLocationName(locationName, AlertConfig.CITY_SEARCH_MAX_RESULTS).orEmpty()
        }

    private fun Address.toGeocodedAddressOrNull(): GeocodedAddress? {
        if (!hasLatitude() || !hasLongitude()) return null
        return GeocodedAddress(
            locality = locality,
            subAdminArea = subAdminArea,
            adminArea = adminArea,
            featureName = featureName,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
        )
    }
}
