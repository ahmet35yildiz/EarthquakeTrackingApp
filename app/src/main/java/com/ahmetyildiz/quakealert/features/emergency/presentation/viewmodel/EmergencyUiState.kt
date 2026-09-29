package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

data class ToolState(
    val isOn: Boolean = false,
    val hasFailed: Boolean = false,
)

data class EmergencyUiState(
    val isStrobeAvailable: Boolean = false,
    val strobe: ToolState = ToolState(),
    val whistle: ToolState = ToolState(),
) {
    val isAnyToolOn: Boolean
        get() = strobe.isOn || whistle.isOn
}
