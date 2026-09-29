package com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.EventLogText
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent
import com.ahmetyildiz.quakealert.features.eventlog.domain.model.filterByName
import com.ahmetyildiz.quakealert.features.eventlog.domain.usecase.CalculateAlertMetricsUseCase
import com.ahmetyildiz.quakealert.features.eventlog.domain.usecase.ClearEventLogUseCase
import com.ahmetyildiz.quakealert.features.eventlog.domain.usecase.ObserveEventLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EventLogViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeEventLog: ObserveEventLogUseCase,
    private val clearEventLog: ClearEventLogUseCase,
    private val calculateAlertMetrics: CalculateAlertMetricsUseCase,
) : ViewModel() {

    val uiState: StateFlow<EventLogUiState> = combine(
        observeEventLog(),
        savedStateHandle.getStateFlow(KEY_QUERY, ""),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EventLogUiState())

    fun onQueryChanged(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }

    fun onClearConfirmed() {
        viewModelScope.launch { clearEventLog() }
    }

    fun createShareText(zoneId: ZoneId): String = EventLogText.format(uiState.value.events, zoneId)

    private fun toUiState(events: List<LoggedEvent>, query: String): EventLogUiState =
        EventLogUiState(
            isLoading = false,
            query = query,
            events = events.filterByName(query),
            totalCount = events.size,
            metrics = calculateAlertMetrics(events),
        )

    private companion object {
        const val KEY_QUERY: String = "query"
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
