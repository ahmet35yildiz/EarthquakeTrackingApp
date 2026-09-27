package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing

internal data class ChoiceToggleOption<T>(
    val value: T,
    val label: String,
    val icon: Painter? = null,
)

@Composable
internal fun <T> ChoiceToggle(
    options: List<ChoiceToggleOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Spacing.extraSmall)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        options.forEach { option ->
            ChoiceToggleItem(
                option = option,
                isSelected = option.value == selected,
                onClick = { onSelected(option.value) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun <T> ChoiceToggleItem(
    option: ChoiceToggleOption<T>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor: Color =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .shadow(elevation = if (isSelected) SELECTED_ITEM_ELEVATION else 0.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceBright else Color.Transparent)
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .heightIn(min = ITEM_MIN_HEIGHT)
            .padding(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        option.icon?.let { icon ->
            Icon(painter = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(ICON_SIZE))
        }
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val ITEM_MIN_HEIGHT: Dp = 40.dp

private val ICON_SIZE: Dp = 18.dp

private val SELECTED_ITEM_ELEVATION: Dp = 1.dp

@PreviewLightDark
@Composable
private fun ChoiceTogglePreview() {
    QuakeAlertTheme {
        Surface {
            ChoiceToggle(
                options = listOf("Whole world", "Near a city").map { ChoiceToggleOption(value = it, label = it) },
                selected = "Near a city",
                onSelected = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
