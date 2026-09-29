package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.analytics.EmergencyToolValue
import com.ahmetyildiz.quakealert.features.emergency.domain.TorchController
import com.ahmetyildiz.quakealert.features.emergency.domain.WhistlePlayer
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
    private val whistlePlayer: WhistlePlayer,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(EmergencyUiState(isStrobeAvailable = torchController.isAvailable))
    val uiState: StateFlow<EmergencyUiState> = mutableUiState.asStateFlow()
    private var strobeJob: Job? = null

    fun onStrobeToggled() {
        val isTurningOn: Boolean = !uiState.value.strobe.isOn
        if (isTurningOn) startStrobe() else stopStrobe()
        trackToggled(EmergencyToolValue.STROBE, isTurningOn)
    }

    fun onWhistleToggled() {
        val isTurningOn: Boolean = !uiState.value.whistle.isOn
        if (isTurningOn) startWhistle() else stopWhistle()
        trackToggled(EmergencyToolValue.WHISTLE, isTurningOn)
    }

    fun onScreenStopped(isConfigurationChange: Boolean) {
        if (isConfigurationChange) return
        stopAllTools()
    }

    override fun onCleared() {
        stopAllTools()
    }

    private fun startStrobe() {
        mutableUiState.update { it.copy(strobe = ToolState(isOn = true)) }
        strobeJob = viewModelScope.launch {
            runStrobe()
            mutableUiState.update { it.copy(strobe = ToolState(hasFailed = true)) }
        }
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        mutableUiState.update { it.copy(strobe = it.strobe.copy(isOn = false)) }
    }

    private fun startWhistle() {
        val isPlaying: Boolean = whistlePlayer.start()
        mutableUiState.update { it.copy(whistle = ToolState(isOn = isPlaying, hasFailed = !isPlaying)) }
    }

    private fun stopWhistle() {
        whistlePlayer.stop()
        mutableUiState.update { it.copy(whistle = it.whistle.copy(isOn = false)) }
    }

    private fun stopAllTools() {
        stopStrobe()
        stopWhistle()
    }

    private fun trackToggled(tool: EmergencyToolValue, isEnabled: Boolean) {
        analyticsTracker.track(AnalyticsEvent.EmergencyToolToggled(tool, isEnabled))
    }
}
