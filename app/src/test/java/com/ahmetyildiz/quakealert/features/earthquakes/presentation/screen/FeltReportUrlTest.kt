package com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FeltReportUrlTest {

    @Test
    fun `form address is the USGS event page with the felt report path`() {
        val url: String = feltReportUrl("https://earthquake.usgs.gov/earthquakes/eventpage/us7000abcd")
        assertEquals("https://earthquake.usgs.gov/earthquakes/eventpage/us7000abcd/tellus", url)
    }

    @Test
    fun `trailing slash of the event page is not doubled`() {
        val url: String = feltReportUrl("https://earthquake.usgs.gov/earthquakes/eventpage/us7000abcd/")
        assertEquals("https://earthquake.usgs.gov/earthquakes/eventpage/us7000abcd/tellus", url)
    }
}
