package com.ahmetyildiz.quakealert.features.alerts.domain.model

import java.util.Locale

data class CitySearchQuery(
    val name: String,
    val country: Country,
    val locale: Locale,
)
