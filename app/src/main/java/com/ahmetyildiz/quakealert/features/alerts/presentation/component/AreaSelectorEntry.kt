package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.core.ui.format.currentLocale
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.AreaSelection
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchViewModel
import java.util.Locale

@Composable
fun AreaSelectorEntry(
    selection: AreaSelection,
    onSelectionChange: (AreaSelection) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitySearchViewModel = hiltViewModel(),
) {
    val citySearch: CitySearchUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locale: Locale = currentLocale()
    LaunchedEffect(locale) { viewModel.onLocaleChanged(locale) }
    AreaSelector(
        selection = selection,
        citySearch = citySearch,
        actions = AreaSelectorActions(
            onModeSelected = { onSelectionChange(selection.copy(mode = it)) },
            onRadiusSelected = { onSelectionChange(selection.copy(radiusKm = it)) },
            onCitySelected = {
                viewModel.onSearchDismissed()
                onSelectionChange(selection.copy(city = it))
            },
            onCountrySelected = viewModel::onCountrySelected,
            onSearch = viewModel::onSearch,
            onSearchDismissed = viewModel::onSearchDismissed,
        ),
        modifier = modifier,
    )
}
