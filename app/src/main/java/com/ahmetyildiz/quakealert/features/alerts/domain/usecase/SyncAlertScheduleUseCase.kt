package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertCheckScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncAlertScheduleUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alertCheckScheduler: AlertCheckScheduler,
) {

    suspend operator fun invoke() {
        val preferences: UserPreferences = userPreferencesRepository.userPreferences.first()
        val isSetUp: Boolean = preferences.alertBaselineAt != null
        if (isSetUp && preferences.alertSettings.isEnabled) {
            alertCheckScheduler.schedulePeriodicCheck()
        } else {
            alertCheckScheduler.cancelPeriodicCheck()
        }
    }
}
