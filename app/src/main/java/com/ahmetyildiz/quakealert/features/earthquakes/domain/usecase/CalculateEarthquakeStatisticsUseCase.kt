package com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase

import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.DailyCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeRegionName
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeStatistics
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.RegionFilter
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.SeverityCount
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.StatisticsInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class CalculateEarthquakeStatisticsUseCase @Inject constructor() {

    operator fun invoke(input: StatisticsInput): EarthquakeStatistics {
        val startTime: Instant = input.period.startTime(input.now, input.zone)
        val earthquakes: List<Earthquake> = input.earthquakes.filter { earthquake ->
            earthquake.time in startTime..input.now && earthquake.isInRegion(input)
        }
        val magnitudes: List<Double> = earthquakes.mapNotNull { it.magnitude?.value }
        return EarthquakeStatistics(
            totalCount = earthquakes.size,
            largest = largestOf(earthquakes),
            averageMagnitude = magnitudes.takeIf { it.isNotEmpty() }?.average(),
            severityCounts = severityCountsOf(magnitudes),
            dailyCounts = dailyCountsOf(earthquakes, input.period.days(input.now, input.zone), input.zone),
            topRegions = topRegionsOf(earthquakes),
        )
    }

    private fun Earthquake.isInRegion(input: StatisticsInput): Boolean =
        when (input.region) {
            RegionFilter.WORLD -> true
            RegionFilter.NEAR_CITY -> input.area.contains(location)
        }

    private fun largestOf(earthquakes: List<Earthquake>): Earthquake? =
        earthquakes
            .filter { it.magnitude != null }
            .maxWithOrNull(compareBy<Earthquake> { it.magnitude?.value }.thenBy { it.time })

    private fun severityCountsOf(magnitudes: List<Double>): List<SeverityCount> {
        val counts: Map<MagnitudeSeverity, Int> = magnitudes.groupingBy(MagnitudeSeverity::fromMagnitude).eachCount()
        return MagnitudeSeverity.entries.map { SeverityCount(severity = it, count = counts[it] ?: 0) }
    }

    private fun dailyCountsOf(earthquakes: List<Earthquake>, days: List<LocalDate>, zone: ZoneId): List<DailyCount> {
        val counts: Map<LocalDate, Int> = earthquakes.groupingBy { it.time.atZone(zone).toLocalDate() }.eachCount()
        return days.map { DailyCount(date = it, count = counts[it] ?: 0) }
    }

    private fun topRegionsOf(earthquakes: List<Earthquake>): List<RegionCount> =
        earthquakes
            .mapNotNull { EarthquakeRegionName.of(it.place) }
            .groupingBy { it }
            .eachCount()
            .map { (region, count) -> RegionCount(region = region, count = count) }
            .sortedWith(compareByDescending<RegionCount> { it.count }.thenBy { it.region })
            .take(EarthquakesConfig.STATISTICS_TOP_REGION_COUNT)
}
