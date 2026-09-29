package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel.CitySearchUiState

@Composable
fun CitySearchDialog(
    state: CitySearchUiState,
    actions: CitySearchActions,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.safeDrawingPadding().imePadding()) {
                CitySearchHeader(onDismiss = onDismiss)
                CitySearchPanel(
                    state = state,
                    actions = actions,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.large, vertical = Spacing.small),
                )
            }
        }
    }
}

@Composable
private fun CitySearchHeader(onDismiss: () -> Unit) {
    Row(modifier = Modifier.padding(Spacing.small), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = stringResource(R.string.action_close))
        }
        Text(
            text = stringResource(R.string.city_search_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
    }
}
