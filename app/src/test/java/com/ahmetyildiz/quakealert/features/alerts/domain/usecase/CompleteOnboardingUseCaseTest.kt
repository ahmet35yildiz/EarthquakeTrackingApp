package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertCheckScheduler
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsUpdate
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompleteOnboardingUseCaseTest {

    private val clock = FakeClock()
    private val repository = FakeUserPreferencesRepository()
    private val scheduler = FakeAlertCheckScheduler()
    private val useCase = CompleteOnboardingUseCase(repository, clock, SyncAlertScheduleUseCase(repository, scheduler))

    private val preferences: UserPreferences
        get() = repository.userPreferences.value

    @Test
    fun `default settings are still saved with a baseline and scheduled`() = runTest {
        useCase(AlertSettings.DEFAULT)
        assertEquals(AlertSettings.DEFAULT, preferences.alertSettings)
        assertEquals(clock.now(), preferences.alertBaselineAt)
        assertTrue(scheduler.isScheduled)
    }

    @Test
    fun `onboarding is marked as completed`() = runTest {
        useCase(AlertSettings.DEFAULT)
        assertTrue(preferences.isOnboardingCompleted)
    }

    @Test
    fun `result compares the stored and the chosen settings`() = runTest {
        val chosen: AlertSettings = AlertSettings.DEFAULT.copy(magnitudeThreshold = 6.0)
        assertEquals(AlertSettingsUpdate(previous = AlertSettings.DEFAULT, updated = chosen), useCase(chosen))
    }
}
