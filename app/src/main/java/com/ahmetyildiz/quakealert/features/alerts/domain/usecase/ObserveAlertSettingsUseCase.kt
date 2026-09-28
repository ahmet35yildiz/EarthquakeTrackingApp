package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAlertSettingsUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    operator fun invoke(): Flow<AlertSettings> =
        userPreferencesRepository.userPreferences
            .map { it.alertSettings }
            .distinctUntilChanged()
}
