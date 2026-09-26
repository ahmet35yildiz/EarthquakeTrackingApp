package com.ahmetyildiz.quakealert.features.alerts.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AlertConfigTest {

    @Test
    fun `default threshold lies on a threshold step inside the range`() {
        val stepsFromStart: Double = (AlertConfig.DEFAULT_THRESHOLD - AlertConfig.THRESHOLD_RANGE.start) /
            AlertConfig.THRESHOLD_STEP
        assertTrue(AlertConfig.DEFAULT_THRESHOLD in AlertConfig.THRESHOLD_RANGE)
        assertEquals(0.0, stepsFromStart % 1.0)
    }

    @Test
    fun `threshold range is divided into whole steps`() {
        val rangeInSteps: Double = (AlertConfig.THRESHOLD_RANGE.endInclusive - AlertConfig.THRESHOLD_RANGE.start) /
            AlertConfig.THRESHOLD_STEP
        assertEquals(0.0, rangeInSteps % 1.0)
    }

    @Test
    fun `default radius is one of the radius options`() {
        assertTrue(AlertConfig.DEFAULT_RADIUS_KM in AlertConfig.RADIUS_OPTIONS_KM)
    }

    @Test
    fun `radius options are positive and ascending`() {
        assertTrue(AlertConfig.RADIUS_OPTIONS_KM.all { it > 0 })
        assertEquals(AlertConfig.RADIUS_OPTIONS_KM.sorted(), AlertConfig.RADIUS_OPTIONS_KM)
    }
}
