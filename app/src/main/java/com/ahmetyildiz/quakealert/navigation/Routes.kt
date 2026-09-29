package com.ahmetyildiz.quakealert.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface GraphRoute

@Serializable
data object OnboardingGraphRoute : GraphRoute

@Serializable
data object MainGraphRoute : GraphRoute

@Serializable
data object OnboardingRoute

@Serializable
sealed interface TopLevelRoute

@Serializable
data object EarthquakesRoute : TopLevelRoute

@Serializable
data object StatisticsRoute : TopLevelRoute

@Serializable
data object AlertsRoute : TopLevelRoute

@Serializable
data object EmergencyRoute : TopLevelRoute

@Serializable
data object SettingsRoute : TopLevelRoute

@Serializable
data object DeveloperToolsRoute

@Serializable
data object EventLogRoute

@Serializable
data object SafetyGuideRoute

@Serializable
data class EarthquakeDetailRoute(
    val earthquakeId: String,
    val isFromNotification: Boolean = false,
)
