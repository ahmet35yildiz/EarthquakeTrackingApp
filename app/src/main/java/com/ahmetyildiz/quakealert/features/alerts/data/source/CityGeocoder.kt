package com.ahmetyildiz.quakealert.features.alerts.data.source

import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddress
import java.util.Locale

interface CityGeocoder {

    fun isPresent(): Boolean

    suspend fun findAddresses(locationName: String, locale: Locale): List<GeocodedAddress>
}
