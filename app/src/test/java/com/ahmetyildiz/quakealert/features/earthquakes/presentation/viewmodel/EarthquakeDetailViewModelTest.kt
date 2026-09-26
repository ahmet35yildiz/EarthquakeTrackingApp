package com.ahmetyildiz.quakealert.features.earthquakes.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.DetailAction
import com.ahmetyildiz.quakealert.core.analytics.DetailSource
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.error.AppError
import com.ahmetyildiz.quakealert.core.preferences.FakeUserPreferencesRepository
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.Earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeDetails
import com.ahmetyildiz.quakealert.features.earthquakes.domain.model.EarthquakeFixtures.earthquake
import com.ahmetyildiz.quakealert.features.earthquakes.domain.repository.FakeEarthquakeRepository
import com.ahmetyildiz.quakealert.features.earthquakes.domain.usecase.GetEarthquakeUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class EarthquakeDetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val cachedEarthquake: Earthquake = earthquake(id = "us1", magnitude = 5.3)
    private val earthquakeRepository = FakeEarthquakeRepository(cached = listOf(cachedEarthquake))
    private val analyticsTracker = FakeAnalyticsTracker()

    @Test
    fun `cached earthquake is shown with its details`() {
        val viewModel: EarthquakeDetailViewModel = createViewModel(id = "us1")
        val expected = EarthquakeDetailUiState(
            content = EarthquakeDetailContent.LOADED,
            details = EarthquakeDetails(cachedEarthquake, distanceFromCity = null),
        )
        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `view is logged once with its source and magnitude`() {
        val viewModel: EarthquakeDetailViewModel = createViewModel(id = "us1", source = DetailSource.NOTIFICATION)
        viewModel.onRetry()
        val expected = AnalyticsEvent.EarthquakeDetailViewed(source = DetailSource.NOTIFICATION, magnitude = 5.3)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `unknown id shows the not found state and logs no view`() {
        val viewModel: EarthquakeDetailViewModel = createViewModel(id = "unknown")
        assertEquals(EarthquakeDetailContent.NOT_FOUND, viewModel.uiState.value.content)
        assertEquals(emptyList<AnalyticsEvent>(), analyticsTracker.events)
    }

    @Test
    fun `network failure shows the error state and retry loads the earthquake`() {
        earthquakeRepository.failure = AppError.Network
        val viewModel: EarthquakeDetailViewModel = createViewModel(id = "us1")
        assertEquals(EarthquakeDetailContent.ERROR, viewModel.uiState.value.content)
        assertEquals(AppError.Network, viewModel.uiState.value.error)
        earthquakeRepository.failure = null
        viewModel.onRetry()
        assertEquals(EarthquakeDetailContent.LOADED, viewModel.uiState.value.content)
    }

    @Test
    fun `actions are logged`() {
        val viewModel: EarthquakeDetailViewModel = createViewModel(id = "us1")
        analyticsTracker.events.clear()
        DetailAction.entries.forEach(viewModel::onActionClicked)
        val expected: List<AnalyticsEvent> = DetailAction.entries.map { AnalyticsEvent.DetailActionClicked(it) }
        assertEquals(expected, analyticsTracker.events)
    }

    private fun createViewModel(id: String, source: DetailSource = DetailSource.LIST): EarthquakeDetailViewModel =
        EarthquakeDetailViewModel(
            earthquakeId = id,
            source = source,
            getEarthquake = GetEarthquakeUseCase(earthquakeRepository, FakeUserPreferencesRepository()),
            analyticsTracker = analyticsTracker,
        )
}
