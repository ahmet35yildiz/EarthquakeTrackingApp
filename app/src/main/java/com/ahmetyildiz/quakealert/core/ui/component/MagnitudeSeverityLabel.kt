package com.ahmetyildiz.quakealert.core.ui.component

import androidx.annotation.StringRes
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.MagnitudeSeverity

@get:StringRes
val MagnitudeSeverity.labelRes: Int
    get() = when (this) {
        MagnitudeSeverity.MINOR -> R.string.severity_minor
        MagnitudeSeverity.LIGHT -> R.string.severity_light
        MagnitudeSeverity.MODERATE -> R.string.severity_moderate
        MagnitudeSeverity.STRONG -> R.string.severity_strong
        MagnitudeSeverity.MAJOR -> R.string.severity_major
    }

@get:StringRes
val MagnitudeSeverity.shortLabelRes: Int
    get() = when (this) {
        MagnitudeSeverity.MODERATE -> R.string.severity_moderate_short
        else -> labelRes
    }
