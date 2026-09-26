package com.ahmetyildiz.quakealert.navigation

import android.content.Intent
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentBackStackEntry?.destination.isTopLevel()) {
                QuakeAlertBottomBar(
                    currentDestination = currentBackStackEntry?.destination,
                    onDestinationSelected = navController::navigateToTopLevelDestination,
                )
            }
        },
    ) { innerPadding ->
        QuakeAlertNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
    PendingDeepLinkEffect(navController = navController, deepLink = pendingDeepLink, onHandled = onDeepLinkHandled)
}

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
