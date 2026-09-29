package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview

sealed interface AlertPreviewUiState {

    data object Hidden : AlertPreviewUiState

    data object Loading : AlertPreviewUiState

    data object Unavailable : AlertPreviewUiState

    data class Ready(val preview: AlertPreview) : AlertPreviewUiState
}
