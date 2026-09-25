package com.ahmetyildiz.quakealert.core.model

enum class MagnitudeSeverity(val lowerBound: Double) {
    MINOR(lowerBound = Double.NEGATIVE_INFINITY),
    LIGHT(lowerBound = 4.0),
    MODERATE(lowerBound = 5.0),
    STRONG(lowerBound = 6.0),
    MAJOR(lowerBound = 7.0);

    companion object {
        fun fromMagnitude(magnitude: Double): MagnitudeSeverity =
            entries.last { magnitude >= it.lowerBound }
    }
}
