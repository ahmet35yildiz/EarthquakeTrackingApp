package com.ahmetyildiz.quakealert.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahmetyildiz.quakealert.features.alerts.presentation.screen.AlertSettingsScreen
import com.ahmetyildiz.quakealert.features.earthquakes.presentation.screen.EarthquakeListRoute
import com.ahmetyildiz.quakealert.features.settings.presentation.screen.SettingsScreen

@Composable
fun QuakeAlertNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = EarthquakesRoute,
        modifier = modifier,
    ) {
        composable<EarthquakesRoute> { EarthquakeListRoute(onEarthquakeClick = {}) }
        composable<AlertsRoute> { AlertSettingsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
    }
}

fun NavController.navigateToTopLevelDestination(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
