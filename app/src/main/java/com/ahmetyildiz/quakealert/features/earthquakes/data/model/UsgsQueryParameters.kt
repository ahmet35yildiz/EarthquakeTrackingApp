package com.ahmetyildiz.quakealert.features.earthquakes.data.model

import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeQuery
import java.util.Locale

private const val COORDINATE_FORMAT: String = "%.4f"
private const val MAGNITUDE_FORMAT: String = "%.1f"

fun EarthquakeQuery.toUsgsQueryParameters(): Map<String, String> =
    buildMap {
        put("starttime", startTime.toString())
        put("minmagnitude", MAGNITUDE_FORMAT.formatInvariant(minMagnitude))
        updatedAfter?.let { put("updatedafter", it.toString()) }
        putAll(area.toCircleParameters())
    }

private fun AlertArea.toCircleParameters(): Map<String, String> =
    when (this) {
        AlertArea.WholeWorld -> emptyMap()
        is AlertArea.AroundCity -> mapOf(
            "latitude" to COORDINATE_FORMAT.formatInvariant(city.location.latitude),
            "longitude" to COORDINATE_FORMAT.formatInvariant(city.location.longitude),
            "maxradiuskm" to radiusKm.toString(),
        )
    }

private fun String.formatInvariant(value: Double): String = String.format(Locale.ROOT, this, value)
