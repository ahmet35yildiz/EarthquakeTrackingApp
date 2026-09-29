package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.PreviewRecentAlertMatchesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.CacheFreshness
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CheckCacheFreshnessUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.RefreshEarthquakesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertPreviewViewModel @Inject constructor(
    private val previewRecentAlertMatches: PreviewRecentAlertMatchesUseCase,
    private val checkCacheFreshness: CheckCacheFreshnessUseCase,
    private val refreshEarthquakes: RefreshEarthquakesUseCase,
) : ViewModel() {

    private val choice = MutableStateFlow<AlertChoice?>(null)
    private val dataStatus = MutableStateFlow(RecentDataStatus.LOADING)

    val uiState: StateFlow<AlertPreviewUiState> = combine(
        previewRecentAlertMatches.observeRecentEarthquakes(),
        choice,
        dataStatus,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AlertPreviewUiState.Hidden)

    init {
        viewModelScope.launch { loadRecentEarthquakes() }
    }

    fun onChoiceChanged(choice: AlertChoice?) {
        this.choice.value = choice
    }

    private suspend fun loadRecentEarthquakes() {
        when (checkCacheFreshness()) {
            CacheFreshness.FRESH -> dataStatus.value = RecentDataStatus.READY
            CacheFreshness.STALE -> {
                dataStatus.value = RecentDataStatus.READY
                refreshEarthquakes()
            }
            CacheFreshness.MISSING -> dataStatus.value = if (refreshEarthquakes() is AppResult.Success) {
                RecentDataStatus.READY
            } else {
                RecentDataStatus.UNAVAILABLE
            }
        }
    }

    private fun toUiState(
        recentEarthquakes: List<Earthquake>,
        choice: AlertChoice?,
        dataStatus: RecentDataStatus,
    ): AlertPreviewUiState =
        when {
            choice == null -> AlertPreviewUiState.Hidden
            dataStatus == RecentDataStatus.LOADING -> AlertPreviewUiState.Loading
            dataStatus == RecentDataStatus.UNAVAILABLE -> AlertPreviewUiState.Unavailable
            else -> AlertPreviewUiState.Ready(previewRecentAlertMatches.preview(recentEarthquakes, choice))
        }

    private enum class RecentDataStatus { LOADING, READY, UNAVAILABLE }

    private companion object {
        const val STOP_TIMEOUT_MILLIS: Long = 5_000
    }
}
