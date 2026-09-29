package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.DetailAction
import com.ahmetyildiz.quakealert.core.analytics.DetailSource
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.error.AppResult
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.GetEarthquakeUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EarthquakeDetailViewModel.Factory::class)
class EarthquakeDetailViewModel @AssistedInject constructor(
    @Assisted private val earthquakeId: String,
    @Assisted private val source: DetailSource,
    private val getEarthquake: GetEarthquakeUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(EarthquakeDetailUiState())
    val uiState: StateFlow<EarthquakeDetailUiState> = mutableUiState.asStateFlow()
    private var isViewTracked: Boolean = false

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    fun onActionClicked(action: DetailAction) {
        analyticsTracker.track(AnalyticsEvent.DetailActionClicked(action))
    }

    private fun load() {
        mutableUiState.value = EarthquakeDetailUiState(content = EarthquakeDetailContent.LOADING)
        viewModelScope.launch {
            val result: AppResult<EarthquakeDetails> =
                getEarthquake(earthquakeId, shouldRevalidate = source == DetailSource.NOTIFICATION)
            mutableUiState.value = when (result) {
                is AppResult.Success -> loadedState(result.data)
                is AppResult.Failure -> failedState(result.error)
            }
        }
    }

    private fun loadedState(details: EarthquakeDetails): EarthquakeDetailUiState {
        trackViewedOnce(details)
        return EarthquakeDetailUiState(content = EarthquakeDetailContent.LOADED, details = details)
    }

    private fun failedState(error: AppError): EarthquakeDetailUiState {
        val content: EarthquakeDetailContent =
            if (error == AppError.NotFound) EarthquakeDetailContent.NOT_FOUND else EarthquakeDetailContent.ERROR
        return EarthquakeDetailUiState(content = content, error = error)
    }

    private fun trackViewedOnce(details: EarthquakeDetails) {
        if (isViewTracked) return
        isViewTracked = true
        val magnitude: Double? = details.earthquake.magnitude?.value
        val event = AnalyticsEvent.EarthquakeDetailViewed(source = source, magnitude = magnitude)
        analyticsTracker.track(event)
    }

    @AssistedFactory
    interface Factory {
        fun create(earthquakeId: String, source: DetailSource): EarthquakeDetailViewModel
    }
}
