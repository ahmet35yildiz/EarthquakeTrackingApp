package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DailyCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.SeverityCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsInput
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CalculateEarthquakeStatisticsUseCaseTest {

    private val calculate = CalculateEarthquakeStatisticsUseCase()
    private val now: Instant = Instant.parse("2026-09-29T12:00:00Z")
    private val izmirArea = AlertArea.AroundCity(City("Izmir", null, "TR", GeoPoint(38.42, 27.14)), radiusKm = 250)

    @Test
    fun `last 7 days are today and the six days before it`() {
        val earthquakes: List<Earthquake> = listOf(
            earthquake(id = "first-moment", time = Instant.parse("2026-09-23T00:00:00Z")),
            earthquake(id = "just-before", time = Instant.parse("2026-09-22T23:59:59Z")),
            earthquake(id = "future", time = now.plusSeconds(1)),
            earthquake(id = "now", time = now),
        )
        assertEquals(2, statistics(earthquakes).totalCount)
    }

    @Test
    fun `last 30 days start 29 days before today`() {
        val earthquakes: List<Earthquake> = listOf(
            earthquake(id = "inside", time = Instant.parse("2026-08-31T00:00:00Z")),
            earthquake(id = "outside", time = Instant.parse("2026-08-30T23:59:59Z")),
        )
        val result: EarthquakeStatistics = statistics(earthquakes, period = StatisticsPeriod.LAST_30_DAYS)
        assertEquals(1, result.totalCount)
        assertEquals(30, result.dailyCounts.size)
    }

    @Test
    fun `largest is the highest magnitude and the newest one on a tie`() {
        val earthquakes: List<Earthquake> = listOf(
            earthquake(id = "older-six", magnitude = 6.0, time = now.minusSeconds(7200)),
            earthquake(id = "newer-six", magnitude = 6.0, time = now.minusSeconds(3600)),
            earthquake(id = "five", magnitude = 5.0, time = now),
            earthquake(id = "unknown", magnitude = null, time = now),
        )
        assertEquals("newer-six", statistics(earthquakes).largest?.id)
    }

    @Test
    fun `average uses known magnitudes and every earthquake is counted`() {
        val earthquakes: List<Earthquake> = listOf(
            earthquake(id = "a", magnitude = 3.0, time = now),
            earthquake(id = "b", magnitude = 5.0, time = now),
            earthquake(id = "c", magnitude = null, time = now),
        )
        val result: EarthquakeStatistics = statistics(earthquakes)
        assertEquals(4.0, result.averageMagnitude)
        assertEquals(3, result.totalCount)
    }

    @Test
    fun `earthquakes without a magnitude give no largest and no average`() {
        val result: EarthquakeStatistics = statistics(listOf(earthquake(id = "a", magnitude = null, time = now)))
        assertNull(result.largest)
        assertNull(result.averageMagnitude)
    }

    @Test
    fun `every magnitude class is listed in order with its boundaries`() {
        val magnitudes: List<Double?> = listOf(2.5, 3.9, 4.0, 4.9, 5.0, 7.0, 8.2, null)
        val earthquakes: List<Earthquake> = magnitudes.mapIndexed { index, magnitude ->
            earthquake(id = "e$index", magnitude = magnitude, time = now)
        }
        val expected: List<SeverityCount> = listOf(
            SeverityCount(MagnitudeSeverity.MINOR, 2),
            SeverityCount(MagnitudeSeverity.LIGHT, 2),
            SeverityCount(MagnitudeSeverity.MODERATE, 1),
            SeverityCount(MagnitudeSeverity.STRONG, 0),
            SeverityCount(MagnitudeSeverity.MAJOR, 2),
        )
        assertEquals(expected, statistics(earthquakes).severityCounts)
    }

    @Test
    fun `daily counts run from the oldest day to today, include empty days and add up to the total`() {
        val earthquakes: List<Earthquake> = listOf(
            earthquake(id = "today-1", time = now),
            earthquake(id = "today-2", time = now.minusSeconds(3600)),
            earthquake(id = "first-day", time = Instant.parse("2026-09-23T08:00:00Z")),
        )
        val result: EarthquakeStatistics = statistics(earthquakes)
        val expected: List<DailyCount> = (6 downTo 0).map { daysAgo ->
            val date: LocalDate = LocalDate.of(2026, 9, 29).minusDays(daysAgo.toLong())
            DailyCount(date, count = mapOf(6 to 1, 0 to 2)[daysAgo] ?: 0)
        }
        assertEquals(expected, result.dailyCounts)
        assertEquals(result.totalCount, result.dailyCounts.sumOf { it.count })
    }

    @Test
    fun `days follow the device time zone`() {
        val lateEvening = earthquake(id = "late", time = Instant.parse("2026-09-28T22:30:00Z"))
        val result: EarthquakeStatistics = statistics(listOf(lateEvening), zone = ZoneId.of("Europe/Istanbul"))
        assertEquals(DailyCount(LocalDate.of(2026, 9, 29), 1), result.dailyCounts.last())
    }

    @Test
    fun `top regions are counted by region name, busiest first, then by name`() {
        val places: List<String?> = listOf(
            "12 km SW of Seferihisar, Turkey", "Izmir, Turkey", "5 km N of Petrolia, CA", "Fiji region",
            "8 km E of Ridgecrest, CA", "10 km S of Tokyo, Japan", "Pangai, Tonga", "Alaska Peninsula, Alaska", null,
        )
        val earthquakes: List<Earthquake> = places.mapIndexed { index, place ->
            earthquake(id = "e$index", time = now, place = place)
        }
        val expected: List<RegionCount> = listOf(
            RegionCount("California", 2),
            RegionCount("Turkey", 2),
            RegionCount("Alaska", 1),
            RegionCount("Fiji region", 1),
            RegionCount("Japan", 1),
        )
        assertEquals(expected, statistics(earthquakes).topRegions)
        assertEquals(EarthquakesConfig.STATISTICS_TOP_REGION_COUNT, expected.size)
    }

    @Test
    fun `my area keeps only earthquakes inside the alert circle`() {
        val near = earthquake(id = "near", time = now, location = GeoPoint(38.3, 26.9))
        val far = earthquake(id = "far", time = now, location = GeoPoint(35.7, 139.7))
        assertEquals(1, statistics(listOf(near, far), region = RegionFilter.NEAR_CITY).totalCount)
        assertEquals(2, statistics(listOf(near, far), region = RegionFilter.WORLD).totalCount)
    }

    private fun statistics(
        earthquakes: List<Earthquake>,
        period: StatisticsPeriod = StatisticsPeriod.LAST_7_DAYS,
        region: RegionFilter = RegionFilter.WORLD,
        zone: ZoneId = ZoneOffset.UTC,
    ): EarthquakeStatistics = calculate(StatisticsInput(earthquakes, period, region, izmirArea, now, zone))
}
