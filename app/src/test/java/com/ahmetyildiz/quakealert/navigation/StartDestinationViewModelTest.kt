package com.ahmetyildiz.quakealert.navigation

import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class StartDestinationViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Test
    fun `first run starts with onboarding`() {
        val viewModel = StartDestinationViewModel(FakeUserPreferencesRepository())
        assertEquals(OnboardingGraphRoute, viewModel.startDestination.value)
    }

    @Test
    fun `completed onboarding starts with the main graph`() {
        val preferences: UserPreferences = UserPreferences.DEFAULT.copy(isOnboardingCompleted = true)
        val viewModel = StartDestinationViewModel(FakeUserPreferencesRepository(preferences))
        assertEquals(MainGraphRoute, viewModel.startDestination.value)
    }

    @Test
    fun `start destination does not change when onboarding completes during the session`() {
        val repository = FakeUserPreferencesRepository()
        val viewModel = StartDestinationViewModel(repository)
        repository.update { it.copy(isOnboardingCompleted = true) }
        assertEquals(OnboardingGraphRoute, viewModel.startDestination.value)
    }
}
