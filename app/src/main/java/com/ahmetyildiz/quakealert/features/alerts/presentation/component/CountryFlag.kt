package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import java.util.Locale

private const val REGIONAL_INDICATOR_A: Int = 0x1F1E6

internal fun countryFlag(countryCode: String): String =
    countryCode.uppercase(Locale.ROOT)
        .filter { it in 'A'..'Z' }
        .map { REGIONAL_INDICATOR_A + (it - 'A') }
        .joinToString(separator = "") { String(Character.toChars(it)) }
