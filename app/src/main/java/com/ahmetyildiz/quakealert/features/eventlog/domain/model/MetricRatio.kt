package com.ahmetyildiz.quakealert.features.eventlog.domain.model

import kotlin.math.roundToInt

data class MetricRatio(
    val count: Int,
    val total: Int,
) {

    val percent: Int?
        get() = if (total == 0) null else (count * PERCENT_SCALE / total).roundToInt()

    private companion object {
        const val PERCENT_SCALE: Double = 100.0
    }
}
