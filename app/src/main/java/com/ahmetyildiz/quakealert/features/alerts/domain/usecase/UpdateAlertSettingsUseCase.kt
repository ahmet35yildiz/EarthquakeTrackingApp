package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferencesRepository
import com.ahmetyildiz.quakealert.core.time.Clock
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class UpdateAlertSettingsUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) {

    private val mutex = Mutex()

    suspend operator fun invoke(transform: (AlertSettings) -> AlertSettings): AlertSettingsUpdate =
        mutex.withLock {
            val previous: AlertSettings = userPreferencesRepository.userPreferences.first().alertSettings
            val update = AlertSettingsUpdate(previous = previous, updated = transform(previous))
            if (update.isChanged) userPreferencesRepository.saveAlertSettings(update.updated, clock.now())
            update
        }
}
