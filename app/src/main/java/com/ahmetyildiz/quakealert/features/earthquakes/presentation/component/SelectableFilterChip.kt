package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.ahmetyildiz.quakealert.core.ui.component.quakeAlertFilterChipColors

@Composable
internal fun SelectableFilterChip(label: String, icon: Painter, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        },
        colors = quakeAlertFilterChipColors(),
        border = null,
    )
}
