package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import app.cash.turbine.test
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFilters
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.pointOnEquatorAt
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ObserveRecentEarthquakesUseCaseTest {

    private val area = AlertArea.AroundCity(
        city = City(name = "Center", adminArea = null, countryCode = "XX", location = GeoPoint(0.0, 0.0)),
        radiusKm = 250,
    )
    private val near: Earthquake = earthquake(id = "near", magnitude = 5.0, location = GeoPoint(0.0, 1.0))
    private val atThresholdInside: Earthquake =
        earthquake(id = "edge", magnitude = 4.5, location = pointOnEquatorAt(distanceKm = 250 - ONE_METER_IN_KM))
    private val justOutside: Earthquake =
        earthquake(id = "outside", magnitude = 6.0, location = pointOnEquatorAt(distanceKm = 250 + ONE_METER_IN_KM))
    private val far: Earthquake = earthquake(id = "far", magnitude = 3.0, location = GeoPoint(0.0, 90.0))
    private val withoutMagnitude: Earthquake =
        earthquake(id = "no-mag", magnitude = null, location = GeoPoint(0.0, 1.0))
    private val cached: List<Earthquake> = listOf(near, atThresholdInside, justOutside, far, withoutMagnitude)

    private val earthquakeRepository = FakeEarthquakeRepository(cached = cached)
    private val preferencesRepository = FakeUserPreferencesRepository(preferencesWith(area))
    private val useCase = ObserveRecentEarthquakesUseCase(earthquakeRepository, preferencesRepository)

    @Test
    fun `world and all filters keep every cached earthquake in cache order`() = runTest {
        val recent: RecentEarthquakes = useCase(EarthquakeFilters()).first()
        assertEquals(cached.map { it.id }, recent.ids())
        assertEquals(cached.size, recent.cachedCount)
    }

    @Test
    fun `distance to the user's city is attached when an area is set`() = runTest {
        val recent: RecentEarthquakes = useCase(EarthquakeFilters()).first()
        assertEquals(111.195, requireNotNull(recent.earthquakes.first().distanceKm), 0.001)
    }

    @Test
    fun `distance is empty without an area`() = runTest {
        preferencesRepository.update { preferencesWith(AlertArea.WholeWorld) }
        val recent: RecentEarthquakes = useCase(EarthquakeFilters()).first()
        assertEquals(List(cached.size) { null }, recent.earthquakes.map { it.distanceKm })
    }

    @Test
    fun `near city keeps earthquakes within the radius, edge included`() = runTest {
        val recent: RecentEarthquakes = useCase(EarthquakeFilters(region = RegionFilter.NEAR_CITY)).first()
        assertEquals(listOf("near", "edge", "no-mag"), recent.ids())
    }

    @Test
    fun `above threshold keeps magnitudes equal to or above it and drops unknown magnitudes`() = runTest {
        val recent: RecentEarthquakes = useCase(EarthquakeFilters(magnitude = MagnitudeFilter.ABOVE_THRESHOLD)).first()
        assertEquals(listOf("near", "edge", "outside"), recent.ids())
    }

    @Test
    fun `region and magnitude filters combine`() = runTest {
        val filters = EarthquakeFilters(region = RegionFilter.NEAR_CITY, magnitude = MagnitudeFilter.ABOVE_THRESHOLD)
        val recent: RecentEarthquakes = useCase(filters).first()
        assertEquals(listOf("near", "edge"), recent.ids())
        assertEquals(filters, recent.appliedFilters)
    }

    @Test
    fun `near city without an area falls back to the whole world`() = runTest {
        preferencesRepository.update { preferencesWith(AlertArea.WholeWorld) }
        val recent: RecentEarthquakes = useCase(EarthquakeFilters(region = RegionFilter.NEAR_CITY)).first()
        assertEquals(cached.map { it.id }, recent.ids())
        assertEquals(RegionFilter.WORLD, recent.appliedFilters.region)
    }

    @Test
    fun `context for the screen comes from the preferences`() = runTest {
        val refreshedAt: Instant = Instant.parse("2026-09-25T12:00:00Z")
        preferencesRepository.update { it.copy(lastRefreshedAt = refreshedAt) }
        val recent: RecentEarthquakes = useCase(EarthquakeFilters()).first()
        assertEquals(area, recent.area)
        assertEquals(4.5, recent.magnitudeThreshold)
        assertEquals(refreshedAt, recent.lastRefreshedAt)
    }

    @Test
    fun `result follows changes of the cache and of the alert settings`() = runTest {
        useCase(EarthquakeFilters(region = RegionFilter.NEAR_CITY, magnitude = MagnitudeFilter.ABOVE_THRESHOLD)).test {
            assertEquals(listOf("near", "edge"), awaitItem().ids())
            preferencesRepository.update { preferencesWith(area, threshold = 5.0) }
            assertEquals(listOf("near"), awaitItem().ids())
            earthquakeRepository.cachedEarthquakes.value = listOf(far)
            assertEquals(emptyList<String>(), awaitItem().ids())
        }
    }

    private fun preferencesWith(area: AlertArea, threshold: Double = 4.5): UserPreferences =
        UserPreferences.DEFAULT.copy(
            alertSettings = AlertSettings(isEnabled = true, magnitudeThreshold = threshold, area = area),
        )

    private fun RecentEarthquakes.ids(): List<String> = earthquakes.map { it.earthquake.id }

    private companion object {
        const val ONE_METER_IN_KM: Double = 0.001
    }
}
