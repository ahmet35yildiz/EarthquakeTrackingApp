package com.ahmetyildiz.quakealert.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ahmetyildiz.quakealert.R

/** Tabs of the bottom navigation bar, in display order. */
enum class TopLevelDestination(
    val route: TopLevelRoute,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    EARTHQUAKES(
        route = EarthquakesRoute,
        labelRes = R.string.tab_earthquakes,
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
    ),
    ALERTS(
        route = AlertsRoute,
        labelRes = R.string.tab_alerts,
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
    ),
    SETTINGS(
        route = SettingsRoute,
        labelRes = R.string.tab_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
}
