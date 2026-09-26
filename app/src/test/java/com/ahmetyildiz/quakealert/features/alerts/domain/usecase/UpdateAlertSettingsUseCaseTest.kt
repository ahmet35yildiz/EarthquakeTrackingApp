package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateAlertSettingsUseCaseTest {

    private val clock = FakeClock()
    private val repository = FakeUserPreferencesRepository()
    private val useCase = UpdateAlertSettingsUseCase(repository, clock)

    private val preferences: UserPreferences
        get() = repository.userPreferences.value

    @Test
    fun `change is saved with a new baseline`() = runTest {
        val update: AlertSettingsUpdate = useCase { it.copy(magnitudeThreshold = 6.0) }
        assertTrue(update.isChanged)
        assertEquals(AlertSettings.DEFAULT, update.previous)
        assertEquals(6.0, preferences.alertSettings.magnitudeThreshold)
        assertEquals(clock.now(), preferences.alertBaselineAt)
    }

    @Test
    fun `unchanged settings are not saved and keep the baseline`() = runTest {
        val update: AlertSettingsUpdate = useCase { it.copy(magnitudeThreshold = AlertSettings.DEFAULT_MAGNITUDE_THRESHOLD) }
        assertFalse(update.isChanged)
        assertNull(preferences.alertBaselineAt)
    }

    @Test
    fun `consecutive updates build on each other`() = runTest {
        useCase { it.copy(isEnabled = false) }
        useCase { it.copy(magnitudeThreshold = 5.0) }
        assertEquals(AlertSettings.DEFAULT.copy(isEnabled = false, magnitudeThreshold = 5.0), preferences.alertSettings)
    }
}
