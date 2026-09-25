package com.ahmetyildiz.quakealert.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ahmetyildiz.quakealert.R

/** Tabs of the bottom navigation bar, in display order. Icons: Material Symbols Rounded (outlined / filled). */
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
    ALERTS(
        route = AlertsRoute,
        labelRes = R.string.tab_alerts,
        selectedIconRes = R.drawable.ic_notifications_filled,
        unselectedIconRes = R.drawable.ic_notifications,
    ),
    SETTINGS(
        route = SettingsRoute,
        labelRes = R.string.tab_settings,
        selectedIconRes = R.drawable.ic_settings_filled,
        unselectedIconRes = R.drawable.ic_settings,
    ),
}
