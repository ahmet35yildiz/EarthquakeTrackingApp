package com.ahmetyildiz.quakealert.features.earthquakes.domain.model

import com.ahmetyildiz.quakealert.core.model.AlertArea
import java.time.Instant
import java.time.ZoneId

data class StatisticsInput(
    val earthquakes: List<Earthquake>,
    val period: StatisticsPeriod,
    val region: RegionFilter,
    val area: AlertArea,
    val now: Instant,
    val zone: ZoneId,
)
