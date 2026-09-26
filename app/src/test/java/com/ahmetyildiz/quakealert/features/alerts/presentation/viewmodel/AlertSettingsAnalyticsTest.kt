package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.SetupContext
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AlertSettingsAnalyticsTest {

    private val izmirArea = AlertArea.AroundCity(City("İzmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 100)
    private val defaults: AlertSettings = AlertSettings.DEFAULT

    @Test
    fun `no change produces no events`() {
        assertEquals(emptyList<AnalyticsEvent>(), AlertSettingsUpdate(defaults, defaults).toAnalyticsEvents(CONTEXT))
    }

    @Test
    fun `toggle is tracked with the new state`() {
        val update = AlertSettingsUpdate(defaults, defaults.copy(isEnabled = false))
        assertEquals(listOf(AnalyticsEvent.AlertsToggled(isEnabled = false)), update.toAnalyticsEvents(CONTEXT))
    }

    @Test
    fun `threshold change is tracked with both values`() {
        val update = AlertSettingsUpdate(defaults, defaults.copy(magnitudeThreshold = 6.0))
        val expected = AnalyticsEvent.AlertThresholdChanged(from = 4.5, to = 6.0, context = CONTEXT)
        assertEquals(listOf(expected), update.toAnalyticsEvents(CONTEXT))
    }

    @Test
    fun `area set is tracked with country and radius only`() {
        val update = AlertSettingsUpdate(defaults, defaults.copy(area = izmirArea))
        val expected = AnalyticsEvent.AlertAreaSet(countryCode = "TR", radiusKm = 100, context = CONTEXT)
        assertEquals(listOf(expected), update.toAnalyticsEvents(CONTEXT))
    }

    @Test
    fun `switching back to the whole world is tracked as cleared`() {
        val update = AlertSettingsUpdate(defaults.copy(area = izmirArea), defaults)
        assertEquals(listOf(AnalyticsEvent.AlertAreaCleared(context = CONTEXT)), update.toAnalyticsEvents(CONTEXT))
    }

    private companion object {
        val CONTEXT: SetupContext = SetupContext.SETTINGS
    }
}
