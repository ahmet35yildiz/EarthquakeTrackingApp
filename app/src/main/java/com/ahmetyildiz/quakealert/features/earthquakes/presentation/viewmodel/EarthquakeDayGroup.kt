package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeWithDistance
import java.time.LocalDate
import java.time.ZoneId

data class EarthquakeDayGroup(
    val date: LocalDate,
    val earthquakes: List<EarthquakeWithDistance>,
)

fun List<EarthquakeWithDistance>.groupByDay(zone: ZoneId): List<EarthquakeDayGroup> =
    groupBy { it.earthquake.time.atZone(zone).toLocalDate() }
        .map { (date, earthquakes) -> EarthquakeDayGroup(date = date, earthquakes = earthquakes) }
