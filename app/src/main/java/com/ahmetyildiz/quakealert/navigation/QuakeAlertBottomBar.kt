package com.ahmetyildiz.quakealert.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun QuakeAlertBottomBar(
    currentDestination: NavDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    BoxWithConstraints {
        val labelStyle: TextStyle = rememberTabLabelStyle(labelWidth = itemWidth(maxWidth))
        NavigationBar {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected: Boolean = currentDestination.isInHierarchyOf(destination)
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = { TopLevelDestinationIcon(destination = destination, isSelected = isSelected) },
                    label = { TopLevelDestinationLabel(destination = destination, style = labelStyle) },
                )
            }
        }
    }
}

private val NAVIGATION_BAR_ITEM_SPACING: Dp = 8.dp

private fun itemWidth(barWidth: Dp): Dp {
    val itemCount: Int = TopLevelDestination.entries.size
    return (barWidth - NAVIGATION_BAR_ITEM_SPACING * (itemCount - 1)) / itemCount
}

@Composable
internal fun TopLevelDestinationIcon(destination: TopLevelDestination, isSelected: Boolean) {
    Icon(
        painter = painterResource(if (isSelected) destination.selectedIconRes else destination.unselectedIconRes),
        contentDescription = null,
    )
}

@Composable
internal fun TopLevelDestinationLabel(destination: TopLevelDestination, style: TextStyle = LocalTextStyle.current) {
    Text(
        text = stringResource(destination.labelRes),
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

internal fun NavDestination?.isInHierarchyOf(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
