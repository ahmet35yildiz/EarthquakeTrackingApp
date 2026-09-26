package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.FakeAlertCheckScheduler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class SyncAlertScheduleUseCaseTest {

    private val preferencesRepository = FakeUserPreferencesRepository()
    private val scheduler = FakeAlertCheckScheduler().apply { isScheduled = true }
    private val useCase = SyncAlertScheduleUseCase(preferencesRepository, scheduler)

    @Test
    fun `nothing is scheduled before alert settings were ever saved`() = runTest {
        useCase()
        assertFalse(scheduler.isScheduled)
    }

    @Test
    fun `saved and enabled alerts are scheduled`() = runTest {
        preferencesRepository.saveAlertSettings(AlertSettings.DEFAULT, BASELINE)
        useCase()
        assertTrue(scheduler.isScheduled)
    }

    @Test
    fun `disabled alerts are cancelled`() = runTest {
        preferencesRepository.saveAlertSettings(AlertSettings.DEFAULT.copy(isEnabled = false), BASELINE)
        useCase()
        assertFalse(scheduler.isScheduled)
    }

    private companion object {
        val BASELINE: Instant = Instant.parse("2026-09-26T10:00:00Z")
    }
}
