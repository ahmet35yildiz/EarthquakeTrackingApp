package com.ahmetyildiz.quakealert.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

@Composable
fun QuakeAlertNavigationRail(
    currentDestination: NavDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    NavigationRail {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected: Boolean = currentDestination.isInHierarchyOf(destination)
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = { TopLevelDestinationIcon(destination = destination, isSelected = isSelected) },
                    label = { TopLevelDestinationLabel(destination = destination) },
                )
            }
        }
    }
}
