package com.ahmetyildiz.quakealert.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun QuakeAlertApp(
    pendingDeepLink: Intent?,
    onDeepLinkHandled: () -> Unit,
    viewModel: StartDestinationViewModel = hiltViewModel(),
) {
    val startDestination: GraphRoute = viewModel.startDestination.collectAsStateWithLifecycle().value ?: return
    val navController: NavHostController = rememberNavController()
    val currentBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = currentBackStackEntry?.destination
    val isTopLevel: Boolean = currentDestination.isTopLevel()
    val windowWidth: Dp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val usesNavigationRail: Boolean = windowWidth >= NAVIGATION_RAIL_MIN_WIDTH
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevel && !usesNavigationRail) {
                QuakeAlertBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = navController::navigateToTopLevelDestination,
                )
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            if (isTopLevel && usesNavigationRail) {
                QuakeAlertNavigationRail(
                    currentDestination = currentDestination,
                    onDestinationSelected = navController::navigateToTopLevelDestination,
                )
            }
            QuakeAlertNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f),
            )
        }
    }
    PendingDeepLinkEffect(navController = navController, deepLink = pendingDeepLink, onHandled = onDeepLinkHandled)
}

private val NAVIGATION_RAIL_MIN_WIDTH: Dp = 600.dp

private fun NavDestination?.isTopLevel(): Boolean =
    this != null && TopLevelDestination.entries.any { hasRoute(it.route::class) }

@Composable
private fun PendingDeepLinkEffect(navController: NavHostController, deepLink: Intent?, onHandled: () -> Unit) {
    LaunchedEffect(deepLink) {
        if (deepLink == null) return@LaunchedEffect
        navController.handleDeepLink(deepLink)
        onHandled()
    }
}
