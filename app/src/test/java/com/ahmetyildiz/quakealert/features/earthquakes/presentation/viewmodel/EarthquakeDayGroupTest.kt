package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EarthquakeDayGroupTest {

    private val istanbul: ZoneId = ZoneId.of("Europe/Istanbul")

    private fun item(id: String, time: String): EarthquakeWithDistance =
        EarthquakeWithDistance(earthquake(id = id, time = Instant.parse(time)), distanceKm = null)

    @Test
    fun `groups keep the list order and split at local midnight`() {
        val items: List<EarthquakeWithDistance> = listOf(
            item(id = "a", time = "2026-09-27T10:00:00Z"),
            item(id = "b", time = "2026-09-26T21:30:00Z"),
            item(id = "c", time = "2026-09-26T20:30:00Z"),
            item(id = "d", time = "2026-09-25T08:00:00Z"),
        )
        val groups: List<EarthquakeDayGroup> = items.groupByDay(istanbul)
        assertEquals(
            listOf(LocalDate.of(2026, 9, 27), LocalDate.of(2026, 9, 26), LocalDate.of(2026, 9, 25)),
            groups.map { it.date },
        )
        assertEquals(
            listOf(listOf("a", "b"), listOf("c"), listOf("d")),
            groups.map { group -> group.earthquakes.map { it.earthquake.id } },
        )
    }

    @Test
    fun `empty list has no groups`() {
        assertEquals(emptyList<EarthquakeDayGroup>(), emptyList<EarthquakeWithDistance>().groupByDay(istanbul))
    }
}
