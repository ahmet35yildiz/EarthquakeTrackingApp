package com.ahmetyildiz.quakealert.core.preferences

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface UserPreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun setOnboardingCompleted(isCompleted: Boolean)

    suspend fun saveAlertSettings(settings: AlertSettings, baselineAt: Instant)

    suspend fun setLastCheckedAt(time: Instant)

    suspend fun setLastRefreshedAt(time: Instant)
}
