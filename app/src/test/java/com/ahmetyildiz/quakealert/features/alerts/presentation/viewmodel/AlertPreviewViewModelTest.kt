package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.model.AlertArea
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.core.time.FakeClock
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertConfig
import com.ahmetyildiz.quakealert.features.alerts.domain.AlertMatcher
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertChoice
import com.ahmetyildiz.quakealert.features.alerts.domain.model.AlertPreview
import com.ahmetyildiz.quakealert.features.alerts.domain.usecase.PreviewRecentAlertMatchesUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.EarthquakesConfig
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.CheckCacheFreshnessUseCase
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.RefreshEarthquakesUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration

class AlertPreviewViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val clock = FakeClock()
    private val preferencesRepository = FakeUserPreferencesRepository()
    private val earthquakeRepository = FakeEarthquakeRepository()
    private val worldAtM45 = AlertChoice(magnitudeThreshold = 4.5, area = AlertArea.WholeWorld)
    private val viewModel: AlertPreviewViewModel by lazy {
        AlertPreviewViewModel(
            previewRecentAlertMatches = PreviewRecentAlertMatchesUseCase(earthquakeRepository, AlertMatcher(), clock),
            checkCacheFreshness = CheckCacheFreshnessUseCase(preferencesRepository, clock),
            refreshEarthquakes = RefreshEarthquakesUseCase(earthquakeRepository, preferencesRepository, clock),
        )
    }

    @Test
    fun `nothing is shown without a complete choice`() = runTest {
        givenFreshCache(quake("q1", magnitude = 5.0))
        viewModel.onChoiceChanged(null)
        assertEquals(AlertPreviewUiState.Hidden, collectState())
    }

    @Test
    fun `fresh cache is counted without a refresh`() = runTest {
        givenFreshCache(quake("q1", magnitude = 5.0), quake("q2", magnitude = 3.0))
        viewModel.onChoiceChanged(worldAtM45)
        assertEquals(ready(matchCount = 1), collectState())
        assertTrue(earthquakeRepository.receivedQueries.isEmpty())
    }

    @Test
    fun `missing cache is loaded first and then counted`() = runTest {
        earthquakeRepository.remoteEarthquakes = listOf(quake("q1", magnitude = 5.0))
        viewModel.onChoiceChanged(worldAtM45)
        assertEquals(ready(matchCount = 1), collectState())
    }

    @Test
    fun `missing cache that cannot be loaded says so`() = runTest {
        earthquakeRepository.failure = AppError.Network
        viewModel.onChoiceChanged(worldAtM45)
        assertEquals(AlertPreviewUiState.Unavailable, collectState())
    }

    @Test
    fun `stale cache is counted right away and refreshed`() = runTest {
        givenFreshCache(quake("q1", magnitude = 5.0))
        clock.advanceBy(EarthquakesConfig.CACHE_STALE_AFTER + Duration.ofMinutes(1))
        earthquakeRepository.remoteEarthquakes = listOf(quake("q1", magnitude = 5.0), quake("q2", magnitude = 6.0))
        viewModel.onChoiceChanged(worldAtM45)
        assertEquals(ready(matchCount = 2), collectState())
        assertEquals(1, earthquakeRepository.receivedQueries.size)
    }

    @Test
    fun `changing the choice recounts at once`() = runTest {
        givenFreshCache(quake("q1", magnitude = 5.0), quake("q2", magnitude = 3.0))
        viewModel.onChoiceChanged(worldAtM45)
        collectState()
        viewModel.onChoiceChanged(worldAtM45.copy(magnitudeThreshold = 2.5))
        assertEquals(ready(matchCount = 2), viewModel.uiState.value)
    }

    private fun givenFreshCache(vararg earthquakes: Earthquake) {
        earthquakeRepository.cachedEarthquakes.value = earthquakes.toList()
        preferencesRepository.update { it.copy(lastRefreshedAt = clock.now()) }
    }

    private fun quake(id: String, magnitude: Double): Earthquake =
        EarthquakeFixtures.earthquake(id = id, magnitude = magnitude, time = clock.now() - Duration.ofHours(1))

    private fun ready(matchCount: Int): AlertPreviewUiState =
        AlertPreviewUiState.Ready(AlertPreview(matchCount = matchCount, period = AlertConfig.PREVIEW_PERIOD))

    private fun TestScope.collectState(): AlertPreviewUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState.value
    }
}
