package com.ahmetyildiz.quakealert.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.ahmetyildiz.quakealert.core.analytics.DetailSource
import com.ahmetyildiz.quakealert.core.navigation.DeepLinkConfig
import com.ahmetyildiz.quakealert.features.alerts.presentation.screen.AlertSettingsEntry
import com.ahmetyildiz.quakealert.features.alerts.presentation.screen.OnboardingEntry
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen.EarthquakeDetailEntry
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen.EarthquakeListEntry
import com.ahmetyildiz.quakealert.features.settings.presentation.screen.SettingsScreen

@Composable
fun QuakeAlertNavHost(
    navController: NavHostController,
    startDestination: GraphRoute,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        navigation<OnboardingGraphRoute>(startDestination = OnboardingRoute) {
            composable<OnboardingRoute> { OnboardingEntry(onFinished = navController::navigateToMainGraph) }
        }
        navigation<MainGraphRoute>(startDestination = EarthquakesRoute) {
            composable<EarthquakesRoute> {
                EarthquakeListEntry(onEarthquakeClick = { id -> navController.navigate(EarthquakeDetailRoute(id)) })
            }
            composable<EarthquakeDetailRoute>(
                deepLinks = listOf(navDeepLink<EarthquakeDetailRoute>(DeepLinkConfig.EARTHQUAKE_BASE)),
            ) { backStackEntry ->
                val route: EarthquakeDetailRoute = backStackEntry.toRoute()
                EarthquakeDetailEntry(
                    earthquakeId = route.earthquakeId,
                    source = if (route.isFromNotification) DetailSource.NOTIFICATION else DetailSource.LIST,
                    onBack = navController::navigateUp,
                )
            }
            composable<AlertsRoute> { AlertSettingsEntry() }
            composable<SettingsRoute> { SettingsScreen() }
        }
    }
}

fun NavController.navigateToMainGraph() {
    navigate(MainGraphRoute) {
        popUpTo<OnboardingGraphRoute> { inclusive = true }
    }
}

fun NavController.navigateToTopLevelDestination(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo<MainGraphRoute> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
