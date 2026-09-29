package com.ahmetyildiz.quakealert.features.alerts.domain

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertMatchCriteria
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Duration
import java.time.Instant

class AlertMatcherTest {

    private val matcher = AlertMatcher()

    private val criteria = AlertMatchCriteria(
        settings = AlertSettings(isEnabled = true, magnitudeThreshold = THRESHOLD, area = AlertArea.WholeWorld),
        alertBaselineAt = CHECKED_AT - Duration.ofDays(1),
        notifiedEarthquakeIds = emptySet(),
        checkedAt = CHECKED_AT,
    )

    @Test
    fun `earthquake that satisfies every rule matches`() {
        assertTrue(matcher.matches(matchingEarthquake(), criteria))
    }

    @ParameterizedTest(name = "M{0} → threshold and area rule equals the delivery rule")
    @CsvSource("4.49", "4.5", "6.0")
    fun `threshold and area rule gives the same answer as delivery for an eligible earthquake`(magnitude: Double) {
        val area = AlertArea.AroundCity(City("Center", null, "XX", GeoPoint(0.0, 0.0)), radiusKm = 100)
        val aroundCity: AlertMatchCriteria = criteria.copy(settings = criteria.settings.copy(area = area))
        val choice = AlertChoice(magnitudeThreshold = THRESHOLD, area = area)
        listOf(GeoPoint(0.5, 0.0), GeoPoint(2.0, 0.0)).forEach { location ->
            val earthquake: Earthquake = matchingEarthquake(magnitude = magnitude, location = location)
            assertEquals(matcher.matches(earthquake, aroundCity), matcher.matchesThresholdAndArea(earthquake, choice))
        }
    }

    @Nested
    inner class AlertsEnabled {

        @Test
        fun `nothing matches when alerts are disabled`() {
            val disabled: AlertMatchCriteria = criteria.copy(settings = criteria.settings.copy(isEnabled = false))
            assertFalse(matcher.matches(matchingEarthquake(), disabled))
        }
    }

    @Nested
    inner class MagnitudeThreshold {

        @ParameterizedTest(name = "M{0} with threshold M4.5 → {1}")
        @CsvSource("4.49, false", "4.5, true", "4.51, true", "8.0, true", "2.5, false")
        fun `magnitude must be at least the threshold`(magnitude: Double, expected: Boolean) {
            assertEquals(expected, matcher.matches(matchingEarthquake(magnitude = magnitude), criteria))
        }

        @Test
        fun `earthquake without magnitude never matches`() {
            assertFalse(matcher.matches(matchingEarthquake(magnitude = null), criteria))
        }
    }

    @Nested
    inner class Area {

        private val aroundCity: AlertMatchCriteria = criteria.copy(
            settings = criteria.settings.copy(area = AlertArea.AroundCity(city = CENTER_CITY, radiusKm = RADIUS_KM)),
        )

        @Test
        fun `whole world matches an epicenter anywhere`() {
            val farAway: Earthquake = matchingEarthquake(location = GeoPoint(-89.0, 179.0))
            assertTrue(matcher.matches(farAway, criteria))
        }

        @Test
        fun `around city matches an epicenter just inside the radius`() {
            val inside: Earthquake = matchingEarthquake(location = pointOnEquatorAt(RADIUS_KM - ONE_METER_IN_KM))
            assertTrue(matcher.matches(inside, aroundCity))
        }

        @Test
        fun `around city does not match an epicenter just outside the radius`() {
            val outside: Earthquake = matchingEarthquake(location = pointOnEquatorAt(RADIUS_KM + ONE_METER_IN_KM))
            assertFalse(matcher.matches(outside, aroundCity))
        }
    }

    @Nested
    inner class AlreadyNotified {

        @Test
        fun `earthquake notified before does not match`() {
            val notified: AlertMatchCriteria = criteria.copy(notifiedEarthquakeIds = setOf(EARTHQUAKE_ID))
            assertFalse(matcher.matches(matchingEarthquake(), notified))
        }

        @Test
        fun `other notified ids do not block the earthquake`() {
            val notified: AlertMatchCriteria = criteria.copy(notifiedEarthquakeIds = setOf("other-id"))
            assertTrue(matcher.matches(matchingEarthquake(), notified))
        }
    }

    @Nested
    inner class Baseline {

        private val baselineAt: Instant = CHECKED_AT - Duration.ofHours(1)
        private val withBaseline: AlertMatchCriteria = criteria.copy(alertBaselineAt = baselineAt)

        @Test
        fun `earthquake at the baseline matches`() {
            assertTrue(matcher.matches(matchingEarthquake(time = baselineAt), withBaseline))
        }

        @Test
        fun `earthquake one second before the baseline does not match`() {
            val beforeBaseline: Earthquake = matchingEarthquake(time = baselineAt - Duration.ofSeconds(1))
            assertFalse(matcher.matches(beforeBaseline, withBaseline))
        }

        @Test
        fun `nothing matches before alert settings were ever saved`() {
            assertFalse(matcher.matches(matchingEarthquake(), criteria.copy(alertBaselineAt = null)))
        }
    }

    @Nested
    inner class MaxEventAge {

        private val oldestAllowedTime: Instant = CHECKED_AT - AlertConfig.MAX_EVENT_AGE

        @Test
        fun `earthquake exactly at the max age matches`() {
            assertTrue(matcher.matches(matchingEarthquake(time = oldestAllowedTime), criteria))
        }

        @Test
        fun `earthquake one second older than the max age does not match`() {
            val tooOld: Earthquake = matchingEarthquake(time = oldestAllowedTime - Duration.ofSeconds(1))
            assertFalse(matcher.matches(tooOld, criteria))
        }
    }

    @Test
    fun `find matches keeps only matching earthquakes in their original order`() {
        val earthquakes: List<Earthquake> = listOf(
            matchingEarthquake(id = "first"),
            matchingEarthquake(id = "below-threshold", magnitude = 3.0),
            matchingEarthquake(id = "second"),
            matchingEarthquake(id = "notified"),
        )
        val notified: AlertMatchCriteria = criteria.copy(notifiedEarthquakeIds = setOf("notified"))
        val matchedIds: List<String> = matcher.findMatches(earthquakes, notified).map(Earthquake::id)
        assertEquals(listOf("first", "second"), matchedIds)
    }

    private fun matchingEarthquake(
        id: String = EARTHQUAKE_ID,
        magnitude: Double? = THRESHOLD,
        location: GeoPoint = GeoPoint(0.0, 0.0),
        time: Instant = CHECKED_AT - Duration.ofMinutes(5),
    ): Earthquake = EarthquakeFixtures.earthquake(id = id, magnitude = magnitude, location = location, time = time)

    private fun pointOnEquatorAt(distanceKm: Double): GeoPoint = EarthquakeFixtures.pointOnEquatorAt(distanceKm)

    private companion object {
        const val EARTHQUAKE_ID: String = "us7000abcd"
        const val THRESHOLD: Double = 4.5
        const val RADIUS_KM: Int = 250
        const val ONE_METER_IN_KM: Double = 0.001
        val CHECKED_AT: Instant = Instant.parse("2026-09-25T12:00:00Z")
        val CENTER_CITY: City = City(
            name = "Center",
            adminArea = null,
            countryCode = "XX",
            location = GeoPoint(0.0, 0.0),
        )
    }
}
