package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import java.time.LocalDate

data class EarthquakeStatistics(
    val totalCount: Int,
    val largest: Earthquake?,
    val averageMagnitude: Double?,
    val severityCounts: List<SeverityCount>,
    val dailyCounts: List<DailyCount>,
    val topRegions: List<RegionCount>,
)

data class SeverityCount(val severity: MagnitudeSeverity, val count: Int)

data class DailyCount(val date: LocalDate, val count: Int)

data class RegionCount(val region: String, val count: Int)
