package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.LocationLookup

data class AreaSelectorActions(
    val onModeSelected: (AreaMode) -> Unit,
    val onRadiusSelected: (Int) -> Unit,
    val onCitySelected: (City) -> Unit,
    val onCountrySelected: (String) -> Unit,
    val onSearch: (String) -> Unit,
    val onSearchDismissed: () -> Unit,
    val location: CurrentLocationActions,
)

@Composable
fun AreaSelector(
    selection: AreaSelection,
    citySearch: CitySearchUiState,
    actions: AreaSelectorActions,
    modifier: Modifier = Modifier,
) {
    var isChoosingCity: Boolean by rememberSaveable { mutableStateOf(false) }
    val locatedCity: City? = (citySearch.location as? LocationLookup.Found)?.city
    LaunchedEffect(locatedCity) {
        if (locatedCity == null) return@LaunchedEffect
        isChoosingCity = false
        actions.onCitySelected(locatedCity)
    }
    val openCitySearch: () -> Unit = {
        isChoosingCity = true
        selection.city?.let { actions.onCountrySelected(it.countryCode) }
    }
    SectionCard(
        title = stringResource(R.string.alert_area_title),
        icon = rememberVectorPainter(Icons.Rounded.Place),
        modifier = modifier,
    ) {
        if (canChooseMode(selection, citySearch)) {
            AreaModeSelector(mode = selection.mode) { mode ->
                actions.onModeSelected(mode)
                if (mode == AreaMode.NEAR_CITY && selection.city == null && citySearch.isAvailable) isChoosingCity = true
            }
        }
        when (selection.mode) {
            AreaMode.WHOLE_WORLD -> WholeWorldWarningCard(isCitySearchAvailable = citySearch.isAvailable)
            AreaMode.NEAR_CITY -> NearCityContent(
                selection = selection,
                citySearch = citySearch,
                onChooseCity = openCitySearch,
                onRadiusSelected = actions.onRadiusSelected,
            )
        }
    }
    if (isChoosingCity) {
        CitySearchDialog(
            state = citySearch,
            actions = CitySearchActions(
                onCountrySelected = actions.onCountrySelected,
                onSearch = actions.onSearch,
                onCitySelected = {
                    isChoosingCity = false
                    actions.onCitySelected(it)
                },
                location = actions.location,
            ),
            onDismiss = {
                isChoosingCity = false
                actions.onSearchDismissed()
            },
        )
    }
}

private fun canChooseMode(selection: AreaSelection, citySearch: CitySearchUiState): Boolean =
    citySearch.isAvailable || selection.mode == AreaMode.NEAR_CITY || selection.city != null

@Composable
private fun AreaModeSelector(mode: AreaMode, onModeSelected: (AreaMode) -> Unit) {
    ChoiceToggle(
        options = AreaMode.entries.map { option ->
            ChoiceToggleOption(value = option, label = stringResource(option.labelRes), icon = option.iconPainter())
        },
        selected = mode,
        onSelected = onModeSelected,
    )
}

@Composable
private fun AreaMode.iconPainter(): Painter = when (this) {
    AreaMode.WHOLE_WORLD -> painterResource(R.drawable.ic_language)
    AreaMode.NEAR_CITY -> rememberVectorPainter(Icons.Rounded.Place)
}

@Composable
private fun NearCityContent(
    selection: AreaSelection,
    citySearch: CitySearchUiState,
    onChooseCity: () -> Unit,
    onRadiusSelected: (Int) -> Unit,
) {
    val city: City? = selection.city
    if (city != null) {
        SelectedCityCard(
            city = city,
            countryName = citySearch.countries.firstOrNull { it.code == city.countryCode }?.name,
            onChange = if (citySearch.isAvailable) onChooseCity else null,
        )
        if (!citySearch.isAvailable) {
            Text(
                text = stringResource(R.string.alert_area_city_change_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else if (citySearch.isAvailable) {
        OutlinedButton(onClick = onChooseCity, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(text = stringResource(R.string.city_search_title), modifier = Modifier.padding(start = Spacing.small))
        }
    }
    RadiusSelector(radiusKm = selection.radiusKm, onRadiusSelected = onRadiusSelected)
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
    location = CurrentLocationActions(onUseMyLocation = {}, onOpenAppSettings = {}, onOpenLocationSettings = {}),
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
