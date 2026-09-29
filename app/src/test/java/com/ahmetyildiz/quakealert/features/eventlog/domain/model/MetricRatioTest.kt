package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MetricRatioTest {

    @Test
    fun `percent is rounded to a whole number`() {
        assertEquals(67, MetricRatio(count = 2, total = 3).percent)
        assertEquals(33, MetricRatio(count = 1, total = 3).percent)
    }

    @Test
    fun `nothing to compare with has no percent`() {
        assertNull(MetricRatio(count = 0, total = 0).percent)
    }
}
