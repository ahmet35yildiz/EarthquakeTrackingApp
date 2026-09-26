package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchResult
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState

data class CitySearchActions(
    val onCountrySelected: (String) -> Unit,
    val onSearch: (String) -> Unit,
    val onCitySelected: (City) -> Unit,
    val onCancel: (() -> Unit)?,
)

@Composable
fun CitySearchPanel(
    state: CitySearchUiState,
    actions: CitySearchActions,
    modifier: Modifier = Modifier,
) {
    var cityName: String by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val canSearch: Boolean = state.selectedCountry != null && cityName.isNotBlank()
    val search: () -> Unit = {
        focusManager.clearFocus()
        actions.onSearch(cityName)
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        CountryPickerField(
            selectedCountry = state.selectedCountry,
            countries = state.countries,
            onCountrySelected = actions.onCountrySelected,
        )
        CityNameField(cityName = cityName, onCityNameChange = { cityName = it }, canSearch = canSearch, onSearch = search)
        CitySearchResultContent(
            result = state.result,
            countryName = state.selectedCountry?.name.orEmpty(),
            onCitySelected = actions.onCitySelected,
            onRetry = search,
        )
        actions.onCancel?.let { TextButton(onClick = it) { Text(text = stringResource(R.string.action_cancel)) } }
    }
}

@Composable
private fun CityNameField(cityName: String, onCityNameChange: (String) -> Unit, canSearch: Boolean, onSearch: () -> Unit) {
    OutlinedTextField(
        value = cityName,
        onValueChange = onCityNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.city_search_label)) },
        supportingText = { Text(text = stringResource(R.string.city_search_prompt)) },
        trailingIcon = {
            IconButton(onClick = onSearch, enabled = canSearch) {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = stringResource(R.string.action_search))
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { if (canSearch) onSearch() }),
    )
}

@Composable
private fun CitySearchResultContent(
    result: CitySearchResult,
    countryName: String,
    onCitySelected: (City) -> Unit,
    onRetry: () -> Unit,
) {
    when (result) {
        CitySearchResult.Idle -> Unit
        CitySearchResult.Loading -> SearchingIndicator()
        is CitySearchResult.Found -> CityResults(cities = result.cities, onCitySelected = onCitySelected)
        is CitySearchResult.NoResults -> ResultMessage(
            text = stringResource(R.string.city_search_no_results, result.searchedName, countryName),
        )
        is CitySearchResult.Failed -> SearchFailure(error = result.error, onRetry = onRetry)
    }
}

@Composable
private fun SearchingIndicator() {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.medium), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        ResultMessage(text = stringResource(R.string.city_search_loading))
    }
}

@Composable
private fun CityResults(cities: List<City>, onCitySelected: (City) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
        Column {
            cities.forEach { city ->
                ListItem(
                    headlineContent = { Text(text = city.name) },
                    modifier = Modifier.clickable(role = Role.Button) { onCitySelected(city) },
                    supportingContent = city.adminArea?.let { { Text(text = it) } },
                    leadingContent = { Icon(imageVector = Icons.Rounded.Place, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun SearchFailure(error: AppError, onRetry: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ResultMessage(text = stringResource(error.toMessageRes()), modifier = Modifier.weight(1f))
        if (error != AppError.GeocoderUnavailable) {
            TextButton(onClick = onRetry) { Text(text = stringResource(R.string.action_retry)) }
        }
    }
}

@Composable
private fun ResultMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun AppError.toMessageRes(): Int =
    when (this) {
        AppError.Network -> R.string.error_message_network
        AppError.GeocoderUnavailable -> R.string.city_search_error_unavailable
        else -> R.string.error_message_generic
    }

@PreviewLightDark
@Composable
private fun CitySearchPanelPreview() {
    QuakeAlertTheme {
        Surface {
            CitySearchPanel(
                state = CitySearchUiState(
                    selectedCountry = Country("US", "United States"),
                    result = CitySearchResult.Found(
                        listOf(
                            City("Springfield", "Illinois", "US", GeoPoint(39.8, -89.6)),
                            City("Springfield", "Massachusetts", "US", GeoPoint(42.1, -72.6)),
                        ),
                    ),
                ),
                actions = CitySearchActions(onCountrySelected = {}, onSearch = {}, onCitySelected = {}, onCancel = {}),
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
