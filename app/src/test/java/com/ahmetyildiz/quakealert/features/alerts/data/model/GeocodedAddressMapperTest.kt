package com.ahmetyildiz.quakealert.features.alerts.data.model

import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.data.model.GeocodedAddressFixtures.address
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeocodedAddressMapperTest {

    @Test
    fun `complete address maps to a city`() {
        val expected = City(
            name = "Izmir",
            adminArea = "Izmir Province",
            countryCode = "TR",
            location = GeoPoint(38.42, 27.14),
        )
        assertEquals(expected, address().toCityOrNull())
    }

    @Test
    fun `name falls back to sub admin area, then admin area, then feature name`() {
        assertEquals("Bornova", address(locality = null, subAdminArea = "Bornova").toCityOrNull()?.name)
        assertEquals("Tokyo", address(locality = " ", adminArea = "Tokyo").toCityOrNull()?.name)
        val featureOnly: GeocodedAddress = address(locality = null, adminArea = null, featureName = "Shibuya")
        assertEquals("Shibuya", featureOnly.toCityOrNull()?.name)
    }

    @Test
    fun `admin area equal to the name is dropped`() {
        assertNull(address(locality = null, adminArea = "Tokyo").toCityOrNull()?.adminArea)
    }

    @Test
    fun `country code is upper-cased`() {
        assertEquals("TR", address(countryCode = "tr").toCityOrNull()?.countryCode)
    }

    @Test
    fun `address is country level only without locality and admin areas`() {
        assertTrue(address(locality = null, adminArea = null, featureName = "Monaco").isCountryLevel)
        assertFalse(address(locality = null, adminArea = "Tokyo").isCountryLevel)
        assertFalse(address(adminArea = null).isCountryLevel)
    }

    @Test
    fun `address without any name is dropped`() {
        assertNull(address(locality = null, subAdminArea = null, adminArea = null, featureName = null).toCityOrNull())
    }

    @Test
    fun `address without country code is dropped`() {
        assertNull(address(countryCode = null).toCityOrNull())
    }

    @Test
    fun `address with invalid coordinates is dropped`() {
        assertNull(address(latitude = 91.0).toCityOrNull())
    }
}
