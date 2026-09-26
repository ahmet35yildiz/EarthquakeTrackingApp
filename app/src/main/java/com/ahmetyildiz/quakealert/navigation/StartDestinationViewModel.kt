package com.ahmetyildiz.quakealert.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val startDestination: StateFlow<GraphRoute?> = flow {
        val isOnboardingCompleted: Boolean = userPreferencesRepository.userPreferences.first().isOnboardingCompleted
        emit(if (isOnboardingCompleted) MainGraphRoute else OnboardingGraphRoute)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
