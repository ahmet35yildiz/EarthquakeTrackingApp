package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import app.cash.turbine.test
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeListOptions
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder.LARGEST_FIRST
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder.NEAREST_FIRST
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.pointOnEquatorAt
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.MagnitudeFilter.ABOVE_THRESHOLD
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RecentEarthquakes
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter.NEAR_CITY
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private val testDispatcher = UnconfinedTestDispatcher()
    private val useCase = ObserveRecentEarthquakesUseCase(
        earthquakeRepository,
        preferencesRepository,
        computeDispatcher = testDispatcher,
    )

    @Test
    fun `world and all filters keep every cached earthquake in cache order`() = runTest(testDispatcher) {
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions()).first()
        assertEquals(cached.map { it.id }, recent.ids())
        assertEquals(cached.size, recent.cachedCount)
    }

    @Test
    fun `distance to the user's city is attached when an area is set`() = runTest(testDispatcher) {
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions()).first()
        assertEquals(111.195, requireNotNull(recent.earthquakes.first().distanceKm), 0.001)
    }

    @Test
    fun `distance is empty without an area`() = runTest(testDispatcher) {
        preferencesRepository.update { preferencesWith(AlertArea.WholeWorld) }
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions()).first()
        assertEquals(List(cached.size) { null }, recent.earthquakes.map { it.distanceKm })
    }

    @Test
    fun `near city keeps earthquakes within the radius, edge included`() = runTest(testDispatcher) {
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(region = NEAR_CITY)).first()
        assertEquals(listOf("near", "edge", "no-mag"), recent.ids())
    }

    @Test
    fun `above threshold keeps equal and larger magnitudes, drops unknown ones`() = runTest(testDispatcher) {
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(magnitude = ABOVE_THRESHOLD)).first()
        assertEquals(listOf("near", "edge", "outside"), recent.ids())
    }

    @Test
    fun `region and magnitude filters combine`() = runTest(testDispatcher) {
        val filters = EarthquakeListOptions(region = NEAR_CITY, magnitude = ABOVE_THRESHOLD)
        val recent: RecentEarthquakes = useCase(filters).first()
        assertEquals(listOf("near", "edge"), recent.ids())
        assertEquals(filters, recent.appliedOptions)
    }

    @Test
    fun `near city without an area falls back to the whole world`() = runTest(testDispatcher) {
        preferencesRepository.update { preferencesWith(AlertArea.WholeWorld) }
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(region = NEAR_CITY)).first()
        assertEquals(cached.map { it.id }, recent.ids())
        assertEquals(RegionFilter.WORLD, recent.appliedOptions.region)
    }

    @Test
    fun `context for the screen comes from the preferences`() = runTest(testDispatcher) {
        val refreshedAt: Instant = Instant.parse("2026-09-25T12:00:00Z")
        preferencesRepository.update { it.copy(lastRefreshedAt = refreshedAt) }
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions()).first()
        assertEquals(area, recent.area)
        assertEquals(4.5, recent.magnitudeThreshold)
        assertEquals(refreshedAt, recent.lastRefreshedAt)
    }

    @Test
    fun `result follows changes of the cache and of the alert settings`() = runTest(testDispatcher) {
        useCase(EarthquakeListOptions(region = NEAR_CITY, magnitude = ABOVE_THRESHOLD)).test {
            assertEquals(listOf("near", "edge"), awaitItem().ids())
            preferencesRepository.update { preferencesWith(area, threshold = 5.0) }
            assertEquals(listOf("near"), awaitItem().ids())
            earthquakeRepository.cachedEarthquakes.value = listOf(far)
            assertEquals(emptyList<String>(), awaitItem().ids())
        }
    }

    @Test
    fun `newest first orders by origin time`() = runTest(testDispatcher) {
        givenCache(sortFixtures)
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions()).first()
        assertEquals(listOf("new-small", "mid-large", "old-large", "old-unknown", "far-new"), recent.ids())
    }

    @Test
    fun `largest first puts unknown magnitudes last, newer first on equal magnitude`() = runTest(testDispatcher) {
        givenCache(sortFixtures)
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(sortOrder = LARGEST_FIRST)).first()
        assertEquals(listOf("mid-large", "old-large", "far-new", "new-small", "old-unknown"), recent.ids())
    }

    @Test
    fun `nearest first orders by distance to the city, newer first on equal distance`() = runTest(testDispatcher) {
        givenCache(sortFixtures)
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(sortOrder = NEAREST_FIRST)).first()
        assertEquals(listOf("new-small", "old-large", "old-unknown", "mid-large", "far-new"), recent.ids())
    }

    @Test
    fun `nearest first without an area falls back to newest first`() = runTest(testDispatcher) {
        givenCache(sortFixtures)
        preferencesRepository.update { preferencesWith(AlertArea.WholeWorld) }
        val recent: RecentEarthquakes = useCase(EarthquakeListOptions(sortOrder = NEAREST_FIRST)).first()
        assertEquals(listOf("new-small", "mid-large", "old-large", "old-unknown", "far-new"), recent.ids())
        assertEquals(EarthquakeSortOrder.NEWEST_FIRST, recent.appliedOptions.sortOrder)
    }

    @Test
    fun `sorting applies to the filtered list`() = runTest(testDispatcher) {
        givenCache(sortFixtures)
        val options = EarthquakeListOptions(region = NEAR_CITY, sortOrder = LARGEST_FIRST)
        val recent: RecentEarthquakes = useCase(options).first()
        assertEquals(listOf("mid-large", "old-large", "new-small", "old-unknown"), recent.ids())
    }

    private val sortFixtures: List<Earthquake> = listOf(
        sortFixture(id = "old-unknown", magnitude = null, longitude = 1.0, secondsAgo = 4_000),
        sortFixture(id = "far-new", magnitude = 5.5, longitude = 90.0, secondsAgo = 5_000),
        sortFixture(id = "old-large", magnitude = 6.0, longitude = 1.0, secondsAgo = 3_000),
        sortFixture(id = "mid-large", magnitude = 6.0, longitude = 2.0, secondsAgo = 2_000),
        sortFixture(id = "new-small", magnitude = 3.0, longitude = 0.5, secondsAgo = 1_000),
    )

    private fun sortFixture(id: String, magnitude: Double?, longitude: Double, secondsAgo: Long): Earthquake =
        earthquake(
            id = id,
            magnitude = magnitude,
            location = GeoPoint(0.0, longitude),
            time = Instant.parse("2026-09-25T12:00:00Z").minusSeconds(secondsAgo),
        )

    private fun givenCache(earthquakes: List<Earthquake>) {
        earthquakeRepository.cachedEarthquakes.value = earthquakes
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
