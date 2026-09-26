package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.filterByName

@Composable
fun CountryPickerField(
    selectedCountry: Country?,
    countries: List<Country>,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPickerOpen: Boolean by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(
            text = stringResource(R.string.country_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { isPickerOpen = true },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.medium,
        ) {
            SelectedCountryRow(selectedCountry)
        }
    }
    if (isPickerOpen) {
        CountryPickerDialog(
            countries = countries,
            selectedCountryCode = selectedCountry?.code,
            onCountrySelected = {
                isPickerOpen = false
                onCountrySelected(it)
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@Composable
private fun SelectedCountryRow(selectedCountry: Country?) {
    Row(
        modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        selectedCountry?.let { Text(text = countryFlag(it.code), style = MaterialTheme.typography.titleLarge) }
        Text(
            text = selectedCountry?.name ?: stringResource(R.string.country_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null)
    }
}

@Composable
fun CountryPickerDialog(
    countries: List<Country>,
    selectedCountryCode: String?,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query: String by rememberSaveable { mutableStateOf("") }
    val matchingCountries: List<Country> = remember(countries, query) { countries.filterByName(query) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.safeDrawingPadding().imePadding()) {
                CountryPickerHeader(query = query, onQueryChange = { query = it }, onDismiss = onDismiss)
                if (matchingCountries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.country_no_match, query),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Spacing.large),
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = matchingCountries, key = Country::code) { country ->
                        CountryRow(
                            country = country,
                            isSelected = country.code == selectedCountryCode,
                            onClick = { onCountrySelected(country.code) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryPickerHeader(query: String, onQueryChange: (String) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = stringResource(R.string.action_close))
            }
            Text(text = stringResource(R.string.country_placeholder), style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.country_search_hint)) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
        )
    }
}

@Composable
private fun CountryRow(country: Country, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = country.name) },
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Text(text = countryFlag(country.code), style = MaterialTheme.typography.titleLarge) },
        trailingContent = if (isSelected) {
            { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) }
        } else {
            null
        },
    )
}

@PreviewLightDark
@Composable
private fun CountryPickerFieldPreview() {
    QuakeAlertTheme {
        Surface {
            CountryPickerField(
                selectedCountry = Country("TR", "Türkiye"),
                countries = emptyList(),
                onCountrySelected = {},
                modifier = Modifier.padding(Spacing.large),
            )
        }
    }
}
