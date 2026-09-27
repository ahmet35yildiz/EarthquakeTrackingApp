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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.component.SectionCard
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaMode
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState

data class AreaSelectorActions(
    val onModeSelected: (AreaMode) -> Unit,
    val onRadiusSelected: (Int) -> Unit,
    val onCitySelected: (City) -> Unit,
    val onCountrySelected: (String) -> Unit,
    val onSearch: (String) -> Unit,
    val onSearchDismissed: () -> Unit,
)

@Composable
fun AreaSelector(
    selection: AreaSelection,
    citySearch: CitySearchUiState,
    actions: AreaSelectorActions,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.alert_area_title),
        icon = rememberVectorPainter(Icons.Rounded.Place),
        modifier = modifier,
    ) {
        if (citySearch.isAvailable) {
            AreaModeSelector(mode = selection.mode, onModeSelected = actions.onModeSelected)
        }
        when (selection.mode) {
            AreaMode.WHOLE_WORLD -> WholeWorldWarningCard(isCitySearchAvailable = citySearch.isAvailable)
            AreaMode.NEAR_CITY -> NearCityContent(selection = selection, citySearch = citySearch, actions = actions)
        }
    }
}

@Composable
private fun AreaModeSelector(mode: AreaMode, onModeSelected: (AreaMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Spacing.extraSmall)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
    ) {
        AreaMode.entries.forEach { option ->
            AreaModeOption(
                mode = option,
                isSelected = option == mode,
                onClick = { onModeSelected(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AreaModeOption(mode: AreaMode, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor: Color =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .shadow(elevation = if (isSelected) SELECTED_OPTION_ELEVATION else 0.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceBright else Color.Transparent)
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .heightIn(min = OPTION_MIN_HEIGHT)
            .padding(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = mode.iconPainter(),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(OPTION_ICON_SIZE),
        )
        Text(
            text = stringResource(mode.labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AreaMode.iconPainter(): Painter = when (this) {
    AreaMode.WHOLE_WORLD -> painterResource(R.drawable.ic_language)
    AreaMode.NEAR_CITY -> rememberVectorPainter(Icons.Rounded.Place)
}

private val OPTION_MIN_HEIGHT: Dp = 40.dp

private val OPTION_ICON_SIZE: Dp = 18.dp

private val SELECTED_OPTION_ELEVATION: Dp = 1.dp

@Composable
private fun NearCityContent(selection: AreaSelection, citySearch: CitySearchUiState, actions: AreaSelectorActions) {
    var isChangingCity: Boolean by rememberSaveable { mutableStateOf(false) }
    val city: City? = selection.city
    if (city != null && !isChangingCity) {
        SelectedCityCard(
            city = city,
            countryName = citySearch.countries.firstOrNull { it.code == city.countryCode }?.name,
            onChange = if (citySearch.isAvailable) {
                {
                    isChangingCity = true
                    actions.onCountrySelected(city.countryCode)
                }
            } else {
                null
            },
        )
    } else {
        CitySearchPanel(
            state = citySearch,
            actions = CitySearchActions(
                onCountrySelected = actions.onCountrySelected,
                onSearch = actions.onSearch,
                onCitySelected = {
                    isChangingCity = false
                    actions.onCitySelected(it)
                },
                onCancel = if (city != null) {
                    {
                        isChangingCity = false
                        actions.onSearchDismissed()
                    }
                } else {
                    null
                },
            ),
        )
    }
    RadiusSelector(radiusKm = selection.radiusKm, onRadiusSelected = actions.onRadiusSelected)
}

private val AreaMode.labelRes: Int
    get() = when (this) {
        AreaMode.WHOLE_WORLD -> R.string.alert_area_whole_world
        AreaMode.NEAR_CITY -> R.string.alert_area_near_city
    }

private val previewActions: AreaSelectorActions = AreaSelectorActions(
    onModeSelected = {},
    onRadiusSelected = {},
    onCitySelected = {},
    onCountrySelected = {},
    onSearch = {},
    onSearchDismissed = {},
)

@PreviewLightDark
@Composable
private fun AreaSelectorWholeWorldPreview() {
    QuakeAlertTheme {
        Surface {
            AreaSelector(
                selection = AreaSelection(mode = AreaMode.WHOLE_WORLD, city = null, radiusKm = 250),
                citySearch = CitySearchUiState(),
                actions = previewActions,
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AreaSelectorNearCityPreview() {
    QuakeAlertTheme {
        Surface {
            AreaSelector(
                selection = AreaSelection(
                    mode = AreaMode.NEAR_CITY,
                    city = City("İzmir", null, "TR", GeoPoint(38.42, 27.14)),
                    radiusKm = 250,
                ),
                citySearch = CitySearchUiState(countries = listOf(Country("TR", "Türkiye"))),
                actions = previewActions,
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
