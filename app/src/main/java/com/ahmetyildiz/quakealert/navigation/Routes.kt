package com.ahmetyildiz.quakealert.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface TopLevelRoute

@Serializable
data object EarthquakesRoute : TopLevelRoute

@Serializable
data object AlertsRoute : TopLevelRoute

@Serializable
data object SettingsRoute : TopLevelRoute
