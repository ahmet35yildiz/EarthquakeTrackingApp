package com.ahmetyildiz.quakealert.core.preferences

import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** Persisted user preferences, shared by all features (see ADR-006). */
interface UserPreferencesRepository {

    /** Current preferences; emits again after every change. Missing values fall back to [UserPreferences.DEFAULT]. */
    val userPreferences: Flow<UserPreferences>

    suspend fun setOnboardingCompleted(isCompleted: Boolean)

    /**
     * Saves [settings] together with a new alert baseline, so earthquakes older than [baselineAt] can never trigger
     * an alert for the new settings.
     */
    suspend fun saveAlertSettings(settings: AlertSettings, baselineAt: Instant)

    suspend fun setLastCheckedAt(time: Instant)

    suspend fun setLastRefreshedAt(time: Instant)
}
