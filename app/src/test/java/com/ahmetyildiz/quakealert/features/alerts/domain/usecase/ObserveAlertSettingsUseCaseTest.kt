package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import app.cash.turbine.test
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ObserveAlertSettingsUseCaseTest {

    @Test
    fun `emits the saved settings and ignores background check updates`() = runTest {
        val repository = FakeUserPreferencesRepository()
        ObserveAlertSettingsUseCase(repository)().test {
            assertEquals(AlertSettings.DEFAULT, awaitItem())
            repository.setLastCheckedAt(Instant.parse("2026-09-26T10:00:00Z"))
            expectNoEvents()
        }
    }
}
