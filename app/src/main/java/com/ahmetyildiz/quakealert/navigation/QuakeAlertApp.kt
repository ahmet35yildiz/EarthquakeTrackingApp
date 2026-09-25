package com.ahmetyildiz.quakealert.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun QuakeAlertApp() {
    val navController: NavHostController = rememberNavController()
    val currentBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            QuakeAlertBottomBar(
                currentDestination = currentBackStackEntry?.destination,
                onDestinationSelected = navController::navigateToTopLevelDestination,
            )
        },
    ) { innerPadding ->
        QuakeAlertNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}
