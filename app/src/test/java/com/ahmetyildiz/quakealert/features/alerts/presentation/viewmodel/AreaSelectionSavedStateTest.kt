package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AreaSelectionSavedStateTest {

    private val handle = SavedStateHandle()
    private val bornova = City(name = "Bornova", adminArea = "İzmir", countryCode = "TR", location = GeoPoint(38.46, 27.21))

    @Test
    fun `nothing saved restores nothing`() {
        assertNull(handle.restoreAreaSelectionOrNull())
    }

    @Test
    fun `city selection round trips with every city field`() {
        val selection = AreaSelection(AreaMode.NEAR_CITY, city = bornova, radiusKm = 50)
        handle.saveAreaSelection(selection)
        assertEquals(selection, handle.restoreAreaSelectionOrNull())
    }

    @Test
    fun `city without an admin area round trips`() {
        val selection = AreaSelection(AreaMode.NEAR_CITY, city = bornova.copy(adminArea = null), radiusKm = 250)
        handle.saveAreaSelection(selection)
        assertEquals(selection, handle.restoreAreaSelectionOrNull())
    }

    @Test
    fun `switching to the whole world clears the saved city`() {
        handle.saveAreaSelection(AreaSelection(AreaMode.NEAR_CITY, city = bornova, radiusKm = 50))
        handle.saveAreaSelection(AreaSelection(AreaMode.WHOLE_WORLD, city = null, radiusKm = 50))
        assertEquals(AreaSelection(AreaMode.WHOLE_WORLD, city = null, radiusKm = 50), handle.restoreAreaSelectionOrNull())
    }

    @Test
    fun `unknown mode restores nothing`() {
        handle.saveAreaSelection(AreaSelection(AreaMode.NEAR_CITY, city = bornova, radiusKm = 50))
        handle["area_selection_mode"] = "SOMEWHERE"
        assertNull(handle.restoreAreaSelectionOrNull())
    }
}
