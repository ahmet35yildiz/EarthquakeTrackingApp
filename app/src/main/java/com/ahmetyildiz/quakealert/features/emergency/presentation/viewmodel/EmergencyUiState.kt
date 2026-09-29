package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

data class EmergencyUiState(
    val isStrobeAvailable: Boolean = false,
    val isStrobeOn: Boolean = false,
    val hasStrobeFailed: Boolean = false,
)
