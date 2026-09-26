package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
    private val syncAlertSchedule: SyncAlertScheduleUseCase,
) {

    suspend operator fun invoke(settings: AlertSettings): AlertSettingsUpdate {
        val previous: AlertSettings = userPreferencesRepository.userPreferences.first().alertSettings
        userPreferencesRepository.saveAlertSettings(settings, baselineAt = clock.now())
        userPreferencesRepository.setOnboardingCompleted(isCompleted = true)
        syncAlertSchedule()
        return AlertSettingsUpdate(previous = previous, updated = settings)
    }
}
