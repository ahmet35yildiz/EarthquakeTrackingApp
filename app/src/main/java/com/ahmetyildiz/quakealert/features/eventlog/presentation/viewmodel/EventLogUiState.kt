package com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.eventlog.domain.model.LoggedEvent

data class EventLogUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val events: List<LoggedEvent> = emptyList(),
    val totalCount: Int = 0,
) {
    val isFiltered: Boolean
        get() = query.isNotBlank()
}
