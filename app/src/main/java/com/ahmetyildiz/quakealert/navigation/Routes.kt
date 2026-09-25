package com.ahmetyildiz.quakealert.navigation

import kotlinx.serialization.Serializable

/** Routes of the bottom navigation tabs. */
@Serializable
sealed interface TopLevelRoute

@Serializable
data object EarthquakesRoute : TopLevelRoute

@Serializable
data object AlertsRoute : TopLevelRoute

@Serializable
data object SettingsRoute : TopLevelRoute
