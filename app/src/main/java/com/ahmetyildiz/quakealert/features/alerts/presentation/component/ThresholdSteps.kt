package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import java.math.BigDecimal
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal fun snapToThresholdStep(value: Float): Double {
    val range: ClosedFloatingPointRange<Double> = AlertConfig.THRESHOLD_RANGE
    val stepIndex: Long = ((value - range.start) / AlertConfig.THRESHOLD_STEP).roundToLong()
    val snapped: BigDecimal = BigDecimal.valueOf(range.start)
        .add(BigDecimal.valueOf(AlertConfig.THRESHOLD_STEP).multiply(BigDecimal.valueOf(stepIndex)))
    return snapped.toDouble().coerceIn(range)
}

internal fun countThresholdSliderSteps(): Int {
    val range: ClosedFloatingPointRange<Double> = AlertConfig.THRESHOLD_RANGE
    return ((range.endInclusive - range.start) / AlertConfig.THRESHOLD_STEP).roundToInt() - 1
}
