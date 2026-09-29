package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.EmergencyToolValue
import com.ahmetyildiz.quakealert.features.emergency.domain.TorchController
import com.ahmetyildiz.quakealert.features.emergency.domain.usecase.RunStrobeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    torchController: TorchController,
    private val runStrobe: RunStrobeUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(EmergencyUiState(isStrobeAvailable = torchController.isAvailable))
    val uiState: StateFlow<EmergencyUiState> = mutableUiState.asStateFlow()
    private var strobeJob: Job? = null

    fun onStrobeToggled() {
        val isTurningOn: Boolean = !uiState.value.isStrobeOn
        if (isTurningOn) startStrobe() else stopStrobe()
        analyticsTracker.track(AnalyticsEvent.EmergencyToolToggled(EmergencyToolValue.STROBE, isEnabled = isTurningOn))
    }

    fun onScreenStopped(isConfigurationChange: Boolean) {
        if (isConfigurationChange) return
        stopStrobe()
    }

    private fun startStrobe() {
        mutableUiState.update { it.copy(isStrobeOn = true, hasStrobeFailed = false) }
        strobeJob = viewModelScope.launch {
            runStrobe()
            mutableUiState.update { it.copy(isStrobeOn = false, hasStrobeFailed = true) }
        }
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        mutableUiState.update { it.copy(isStrobeOn = false) }
    }
}
