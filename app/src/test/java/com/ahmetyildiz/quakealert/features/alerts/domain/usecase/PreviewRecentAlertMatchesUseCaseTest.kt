package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class PreviewRecentAlertMatchesUseCaseTest {

    private val clock = FakeClock()
    private val now: Instant = clock.now()
    private val useCase = PreviewRecentAlertMatchesUseCase(FakeEarthquakeRepository(), AlertMatcher(), clock)
    private val center = City(name = "Center", adminArea = null, countryCode = "XX", location = GeoPoint(0.0, 0.0))
    private val worldAtM45 = AlertChoice(magnitudeThreshold = 4.5, area = AlertArea.WholeWorld)

    @Test
    fun `result carries the preview period`() {
        assertEquals(AlertPreview(matchCount = 0, period = AlertConfig.PREVIEW_PERIOD), useCase.preview(emptyList(), worldAtM45))
    }

    @Test
    fun `earthquakes at or above the threshold count, weaker and unknown ones do not`() {
        val earthquakes: List<Earthquake> = listOf(
            quake("equal", magnitude = 4.5),
            quake("stronger", magnitude = 6.1),
            quake("weaker", magnitude = 4.4),
            quake("unknown", magnitude = null),
        )
        assertEquals(2, useCase.preview(earthquakes, worldAtM45).matchCount)
    }

    @Test
    fun `simulated test earthquakes are not counted`() {
        val earthquakes: List<Earthquake> = listOf(
            quake("us7000abcd"),
            quake("${SimulateAlertUseCase.SIMULATED_ID_PREFIX}1790626998788"),
        )
        assertEquals(1, useCase.preview(earthquakes, worldAtM45).matchCount)
    }

    @Test
    fun `only earthquakes inside the radius count`() {
        val choice = AlertChoice(magnitudeThreshold = 4.5, area = AlertArea.AroundCity(center, radiusKm = 100))
        val earthquakes: List<Earthquake> = listOf(
            quake("inside", location = EarthquakeFixtures.pointOnEquatorAt(99.0)),
            quake("outside", location = EarthquakeFixtures.pointOnEquatorAt(101.0)),
        )
        assertEquals(1, useCase.preview(earthquakes, choice).matchCount)
    }

    @Test
    fun `only earthquakes within the preview period count, its start included`() {
        val periodStart: Instant = now - AlertConfig.PREVIEW_PERIOD
        val earthquakes: List<Earthquake> = listOf(
            quake("at start", time = periodStart),
            quake("just before", time = periodStart - Duration.ofSeconds(1)),
        )
        assertEquals(1, useCase.preview(earthquakes, worldAtM45).matchCount)
    }

    @Test
    fun `lower threshold and larger radius never count fewer earthquakes`() {
        val earthquakes: List<Earthquake> = (1..20).map {
            quake("q$it", magnitude = 2.5 + it * 0.25, location = EarthquakeFixtures.pointOnEquatorAt(it * 40.0))
        }
        val narrow = AlertChoice(magnitudeThreshold = 5.0, area = AlertArea.AroundCity(center, radiusKm = 250))
        val wide = AlertChoice(magnitudeThreshold = 3.0, area = AlertArea.AroundCity(center, radiusKm = 1000))
        assertTrue(useCase.preview(earthquakes, wide).matchCount >= useCase.preview(earthquakes, narrow).matchCount)
    }

    private fun quake(
        id: String,
        magnitude: Double? = 5.0,
        location: GeoPoint = GeoPoint(0.0, 0.0),
        time: Instant = now - Duration.ofHours(1),
    ): Earthquake = EarthquakeFixtures.earthquake(id = id, magnitude = magnitude, location = location, time = time)
}
