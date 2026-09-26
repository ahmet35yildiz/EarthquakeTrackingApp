package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CountryFlagTest {

    @Test
    fun `country code becomes regional indicator symbols`() {
        assertEquals("🇹🇷", countryFlag("TR"))
        assertEquals("🇯🇵", countryFlag("jp"))
    }
}
