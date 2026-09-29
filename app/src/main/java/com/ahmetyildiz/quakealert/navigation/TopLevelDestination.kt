package com.ahmetyildiz.quakealert.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ahmetyildiz.quakealert.R

enum class TopLevelDestination(
    val route: TopLevelRoute,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int,
) {
    EARTHQUAKES(
        route = EarthquakesRoute,
        labelRes = R.string.tab_earthquakes,
        selectedIconRes = R.drawable.ic_waves,
        unselectedIconRes = R.drawable.ic_waves,
    ),
    STATISTICS(
        route = StatisticsRoute,
        labelRes = R.string.tab_statistics,
        selectedIconRes = R.drawable.ic_bar_chart,
        unselectedIconRes = R.drawable.ic_bar_chart,
    ),
    ALERTS(
        route = AlertsRoute,
        labelRes = R.string.tab_alerts,
        selectedIconRes = R.drawable.ic_notifications_filled,
        unselectedIconRes = R.drawable.ic_notifications,
    ),
    EMERGENCY(
        route = EmergencyRoute,
        labelRes = R.string.tab_emergency,
        selectedIconRes = R.drawable.ic_emergency_filled,
        unselectedIconRes = R.drawable.ic_emergency,
    ),
    SETTINGS(
        route = SettingsRoute,
        labelRes = R.string.tab_settings,
        selectedIconRes = R.drawable.ic_settings_filled,
        unselectedIconRes = R.drawable.ic_settings,
    ),
}
