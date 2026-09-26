package com.ahmetyildiz.quakealert.features.alerts.data.model

object GeocodedAddressFixtures {

    fun address(
        locality: String? = "Izmir",
        subAdminArea: String? = null,
        adminArea: String? = "Izmir Province",
        featureName: String? = null,
        countryCode: String? = "TR",
        latitude: Double = 38.42,
        longitude: Double = 27.14,
    ): GeocodedAddress =
        GeocodedAddress(
            locality = locality,
            subAdminArea = subAdminArea,
            adminArea = adminArea,
            featureName = featureName,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
        )
}
