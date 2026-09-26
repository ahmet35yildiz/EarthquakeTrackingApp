package com.ahmetyildiz.quakealert.features.alerts.data.source

import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddress
import java.util.Locale

class FakeCityGeocoder(
    var isGeocoderPresent: Boolean = true,
    var addresses: List<GeocodedAddress> = emptyList(),
    var failure: Exception? = null,
) : CityGeocoder {

    val requests: MutableList<Pair<String, Locale>> = mutableListOf()

    override fun isPresent(): Boolean = isGeocoderPresent

    override suspend fun findAddresses(locationName: String, locale: Locale): List<GeocodedAddress> {
        requests += locationName to locale
        failure?.let { throw it }
        return addresses
    }
}
