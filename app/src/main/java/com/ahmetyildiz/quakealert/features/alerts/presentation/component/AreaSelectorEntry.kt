package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.core.location.LOCATION_PERMISSION
import com.ahmetyildiz.quakealert.core.location.hasLocationPermission
import com.ahmetyildiz.quakealert.core.navigation.appSettingsIntent
import com.ahmetyildiz.quakealert.core.navigation.locationSettingsIntent
import com.ahmetyildiz.quakealert.core.navigation.tryStartActivity
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
            location = currentLocationActions(viewModel),
        ),
        modifier = modifier,
    )
}

@Composable
private fun currentLocationActions(viewModel: CitySearchViewModel): CurrentLocationActions {
    val context: Context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) viewModel.onUseMyLocation() else viewModel.onLocationPermissionDenied()
    }
    return CurrentLocationActions(
        onUseMyLocation = {
            if (context.hasLocationPermission()) {
                viewModel.onUseMyLocation()
            } else {
                permissionLauncher.launch(LOCATION_PERMISSION)
            }
        },
        onOpenAppSettings = { context.tryStartActivity(appSettingsIntent(context)) },
        onOpenLocationSettings = { context.tryStartActivity(locationSettingsIntent()) },
    )
}
