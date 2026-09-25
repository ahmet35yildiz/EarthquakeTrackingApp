package com.ahmetyildiz.quakealert.features.earthquakes.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeSortOrder

@Composable
fun EarthquakeSortMenu(
    selected: EarthquakeSortOrder,
    isNearestAvailable: Boolean,
    onSortOrderSelected: (EarthquakeSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    val selectedLabel: String = stringResource(selected.labelRes)
    val description: String = stringResource(R.string.sort_button_description, selectedLabel)
    Box(modifier = modifier) {
        TextButton(
            onClick = { isExpanded = true },
            modifier = Modifier.semantics { contentDescription = description },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sort),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(text = selectedLabel, modifier = Modifier.padding(start = Spacing.small))
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            availableSortOrders(isNearestAvailable).forEach { sortOrder ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(sortOrder.labelRes)) },
                    onClick = {
                        isExpanded = false
                        onSortOrderSelected(sortOrder)
                    },
                    trailingIcon = if (sortOrder == selected) {
                        { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

private fun availableSortOrders(isNearestAvailable: Boolean): List<EarthquakeSortOrder> =
    EarthquakeSortOrder.entries.filter { it != EarthquakeSortOrder.NEAREST_FIRST || isNearestAvailable }

@get:StringRes
private val EarthquakeSortOrder.labelRes: Int
    get() = when (this) {
        EarthquakeSortOrder.NEWEST_FIRST -> R.string.sort_newest_first
        EarthquakeSortOrder.LARGEST_FIRST -> R.string.sort_largest_first
        EarthquakeSortOrder.NEAREST_FIRST -> R.string.sort_nearest_first
    }

@PreviewLightDark
@Composable
private fun EarthquakeSortMenuPreview() {
    QuakeAlertTheme {
        Surface {
            EarthquakeSortMenu(
                selected = EarthquakeSortOrder.LARGEST_FIRST,
                isNearestAvailable = true,
                onSortOrderSelected = {},
            )
        }
    }
}
