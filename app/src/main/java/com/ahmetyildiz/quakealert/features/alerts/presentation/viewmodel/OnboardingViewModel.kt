package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val mutableIsCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = mutableIsCompleted.asStateFlow()

    fun onGetStartedClicked() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(isCompleted = true)
            mutableIsCompleted.value = true
        }
    }
}
