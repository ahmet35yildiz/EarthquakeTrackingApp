package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertSettingsStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ObserveAlertSettingsUseCaseTest {

    @Test
    fun `settings come with the last background check time`() = runTest {
        val lastCheckedAt: Instant = Instant.parse("2026-09-26T10:00:00Z")
        val repository = FakeUserPreferencesRepository()
        repository.setLastCheckedAt(lastCheckedAt)
        val status: AlertSettingsStatus = ObserveAlertSettingsUseCase(repository)().first()
        assertEquals(AlertSettingsStatus(AlertSettings.DEFAULT, lastCheckedAt), status)
    }
}
