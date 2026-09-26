package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AreaSelectionTest {

    private val izmir = City(name = "İzmir", adminArea = null, countryCode = "TR", location = GeoPoint(38.42, 27.14))

    @Test
    fun `whole world starts with the default radius and no city`() {
        val expected = AreaSelection(AreaMode.WHOLE_WORLD, city = null, radiusKm = AlertConfig.DEFAULT_RADIUS_KM)
        assertEquals(expected, AreaSelection.from(AlertArea.WholeWorld))
    }

    @Test
    fun `area around a city round-trips`() {
        val area = AlertArea.AroundCity(city = izmir, radiusKm = 100)
        assertEquals(area, AreaSelection.from(area).toAlertAreaOrNull())
    }

    @Test
    fun `whole world mode ignores a remembered city`() {
        val selection = AreaSelection(AreaMode.WHOLE_WORLD, city = izmir, radiusKm = 100)
        assertEquals(AlertArea.WholeWorld, selection.toAlertAreaOrNull())
    }

    @Test
    fun `near city without a city is not a complete area`() {
        assertNull(AreaSelection(AreaMode.NEAR_CITY, city = null, radiusKm = 100).toAlertAreaOrNull())
    }
}
