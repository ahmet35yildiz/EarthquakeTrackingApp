package com.ahmetyildiz.quakealert.core.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences.DEFAULT,
) : UserPreferencesRepository {

    private val preferences = MutableStateFlow(initial)

    override val userPreferences: StateFlow<UserPreferences> = preferences

    fun update(transform: (UserPreferences) -> UserPreferences) {
        preferences.update(transform)
    }

    override suspend fun setOnboardingCompleted(isCompleted: Boolean) {
        preferences.update { it.copy(isOnboardingCompleted = isCompleted) }
    }

    override suspend fun saveAlertSettings(settings: AlertSettings, baselineAt: Instant) {
        preferences.update { it.copy(alertSettings = settings, alertBaselineAt = baselineAt) }
    }

    override suspend fun setLastCheckedAt(time: Instant) {
        preferences.update { it.copy(lastCheckedAt = time) }
    }

    override suspend fun setLastRefreshedAt(time: Instant) {
        preferences.update { it.copy(lastRefreshedAt = time) }
    }
}
