package com.ahmetyildiz.quakealert.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.model.City
import com.ahmetyildiz.quakealert.core.model.GeoPoint
import com.ahmetyildiz.quakealert.core.preferences.AlertSettings
import com.ahmetyildiz.quakealert.core.preferences.UserPreferences
import com.ahmetyildiz.quakealert.core.time.FakeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration

/** Round-trips through a real DataStore file, so key names and value mapping are covered together. */
class DataStoreUserPreferencesRepositoryTest {

    @TempDir
    lateinit var tempDirectory: File

    private val clock = FakeClock()

    private val izmir = City(
        name = "İzmir",
        adminArea = "İzmir",
        countryCode = "TR",
        location = GeoPoint(38.4237, 27.1428),
    )

    @Test
    fun `empty store reads as defaults`() = runRepositoryTest { repository ->
        val preferences: UserPreferences = repository.userPreferences.first()
        assertEquals(UserPreferences.DEFAULT, preferences)
    }

    @Test
    fun `alert settings with a city area are read back with their baseline`() = runRepositoryTest { repository ->
        val settings = AlertSettings(
            isEnabled = false,
            magnitudeThreshold = 5.5,
            area = AlertArea.AroundCity(city = izmir, radiusKm = 100),
        )
        repository.saveAlertSettings(settings, baselineAt = clock.now())
        val preferences: UserPreferences = repository.userPreferences.first()
        assertEquals(settings, preferences.alertSettings)
        assertEquals(clock.now(), preferences.alertBaselineAt)
    }

    @Test
    fun `city without admin area is read back without one`() = runRepositoryTest { repository ->
        val area = AlertArea.AroundCity(city = izmir, radiusKm = 250)
        repository.saveAlertSettings(AlertSettings.DEFAULT.copy(area = area), baselineAt = clock.now())
        val areaWithoutAdmin = AlertArea.AroundCity(city = izmir.copy(adminArea = null), radiusKm = 250)
        repository.saveAlertSettings(AlertSettings.DEFAULT.copy(area = areaWithoutAdmin), baselineAt = clock.now())
        assertEquals(areaWithoutAdmin, repository.userPreferences.first().alertSettings.area)
    }

    @Test
    fun `switching to whole world clears the stored area`() = runRepositoryTest { repository ->
        val cityArea = AlertArea.AroundCity(city = izmir, radiusKm = 500)
        repository.saveAlertSettings(AlertSettings.DEFAULT.copy(area = cityArea), baselineAt = clock.now())
        clock.advanceBy(Duration.ofMinutes(5))
        repository.saveAlertSettings(AlertSettings.DEFAULT, baselineAt = clock.now())
        val preferences: UserPreferences = repository.userPreferences.first()
        assertEquals(AlertArea.WholeWorld, preferences.alertSettings.area)
        assertEquals(clock.now(), preferences.alertBaselineAt)
    }

    @Test
    fun `onboarding flag and background timestamps are read back`() = runRepositoryTest { repository ->
        val checkedAt = clock.now()
        val refreshedAt = clock.now().plusSeconds(30)
        repository.setOnboardingCompleted(isCompleted = true)
        repository.setLastCheckedAt(checkedAt)
        repository.setLastRefreshedAt(refreshedAt)
        val preferences: UserPreferences = repository.userPreferences.first()
        val expected: UserPreferences = UserPreferences.DEFAULT.copy(
            isOnboardingCompleted = true,
            lastCheckedAt = checkedAt,
            lastRefreshedAt = refreshedAt,
        )
        assertEquals(expected, preferences)
    }

    private fun runRepositoryTest(block: suspend TestScope.(DataStoreUserPreferencesRepository) -> Unit) = runTest {
        val repository = DataStoreUserPreferencesRepository(createDataStore(backgroundScope))
        block(repository)
    }

    private fun createDataStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { File(tempDirectory, "test.preferences_pb") },
    )
}
