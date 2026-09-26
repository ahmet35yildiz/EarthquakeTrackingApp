package com.ahmetyildiz.quakealert.features.eventlog.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.navigation.shareIntent
import com.ahmetyildiz.quakealert.core.navigation.tryStartActivity
import com.ahmetyildiz.quakealert.core.ui.component.EmptyState
import com.ahmetyildiz.quakealert.core.ui.component.LoadingState
import com.ahmetyildiz.quakealert.core.ui.component.ScreenTitle
import com.ahmetyildiz.quakealert.core.ui.theme.Spacing
import com.ahmetyildiz.quakealert.features.eventlog.presentation.component.EventLogItem
import com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel.EventLogUiState
import com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel.EventLogViewModel
import kotlinx.coroutines.launch
import java.time.ZoneId

data class EventLogActions(
    val onBack: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onShare: () -> Unit,
    val onClearConfirmed: () -> Unit,
)

@Composable
fun EventLogEntry(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventLogViewModel = hiltViewModel(),
) {
    val uiState: EventLogUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query: String by rememberSaveable { mutableStateOf("") }
    val context: Context = LocalContext.current
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val shareTitle: String = stringResource(R.string.event_log_share_title)
    val noAppMessage: String = stringResource(R.string.error_no_app_for_action)
    val actions = EventLogActions(
        onBack = onBack,
        onQueryChange = { newQuery ->
            query = newQuery
            viewModel.onQueryChanged(newQuery)
        },
        onShare = {
            val text: String = viewModel.createShareText(ZoneId.systemDefault())
            if (!context.tryStartActivity(shareIntent(text, shareTitle))) {
                scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
            }
        },
        onClearConfirmed = viewModel::onClearConfirmed,
    )
    EventLogScreen(
        uiState = uiState,
        query = query,
        actions = actions,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun EventLogScreen(
    uiState: EventLogUiState,
    query: String,
    actions: EventLogActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var isClearDialogOpen: Boolean by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            EventLogTopBar(
                uiState = uiState,
                onBack = actions.onBack,
                onShare = actions.onShare,
                onClear = { isClearDialogOpen = true },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            FilterField(query = query, onQueryChange = actions.onQueryChange)
            EventLogContent(uiState = uiState)
        }
    }
    if (isClearDialogOpen) {
        ClearEventLogDialog(
            eventCount = uiState.totalCount,
            onConfirm = {
                isClearDialogOpen = false
                actions.onClearConfirmed()
            },
            onDismiss = { isClearDialogOpen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventLogTopBar(uiState: EventLogUiState, onBack: () -> Unit, onShare: () -> Unit, onClear: () -> Unit) {
    TopAppBar(
        title = { ScreenTitle(text = stringResource(R.string.event_log_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onShare, enabled = uiState.events.isNotEmpty()) {
                Icon(imageVector = Icons.Rounded.Share, contentDescription = stringResource(R.string.action_share))
            }
            IconButton(onClick = onClear, enabled = uiState.totalCount > 0) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.action_clear_event_log),
                )
            }
        },
    )
}

@Composable
private fun FilterField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenMargin, vertical = Spacing.small),
        label = { Text(text = stringResource(R.string.event_log_filter_label)) },
        leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(R.string.action_clear_filter),
                    )
                }
            }
        },
        singleLine = true,
    )
}

@Composable
private fun EventLogContent(uiState: EventLogUiState) {
    when {
        uiState.isLoading -> LoadingState()
        uiState.totalCount == 0 -> EmptyState(
            title = stringResource(R.string.event_log_empty_title),
            message = stringResource(R.string.event_log_empty_message),
        )
        uiState.events.isEmpty() -> EmptyState(
            title = stringResource(R.string.event_log_no_match_title),
            message = stringResource(R.string.event_log_no_match_message, uiState.query.trim()),
        )
        else -> EventList(uiState = uiState)
    }
}

@Composable
private fun EventList(uiState: EventLogUiState) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.screenMargin,
            end = Spacing.screenMargin,
            bottom = Spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        item(key = COUNT_KEY) {
            Text(
                text = eventCountText(uiState),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(items = uiState.events, key = { it.id }) { event -> EventLogItem(event = event) }
    }
}

@Composable
private fun eventCountText(uiState: EventLogUiState): String =
    if (uiState.isFiltered) {
        pluralStringResource(
            R.plurals.event_log_filtered_count,
            uiState.totalCount,
            uiState.events.size,
            uiState.totalCount,
        )
    } else {
        pluralStringResource(R.plurals.event_log_count, uiState.totalCount, uiState.totalCount)
    }

@Composable
private fun ClearEventLogDialog(eventCount: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_log_clear_dialog_title)) },
        text = { Text(text = pluralStringResource(R.plurals.event_log_clear_dialog_message, eventCount, eventCount)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = stringResource(R.string.action_clear)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel)) }
        },
    )
}

private const val COUNT_KEY: String = "count"
