package com.ahmetyildiz.quakealert.features.eventlog.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ahmetyildiz.quakealert.core.database.AnalyticsEventEntity
import com.ahmetyildiz.quakealert.core.database.FakeAnalyticsEventDao
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.features.eventlog.data.repository.EventLogRepositoryImpl
import com.ahmetyildiz.quakealert.features.eventlog.domain.usecase.ClearEventLogUseCase
import com.ahmetyildiz.quakealert.features.eventlog.domain.usecase.ObserveEventLogUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.ZoneOffset

class EventLogViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val dao = FakeAnalyticsEventDao()
    private val savedStateHandle = SavedStateHandle()
    private val viewModel: EventLogViewModel by lazy {
        val repository = EventLogRepositoryImpl(dao)
        EventLogViewModel(savedStateHandle, ObserveEventLogUseCase(repository), ClearEventLogUseCase(repository))
    }

    @Test
    fun `state lists every event newest first`() = runTest {
        storeEvents("app_opened", "earthquake_list_viewed")
        val state: EventLogUiState = collectState()
        assertFalse(state.isLoading)
        assertEquals(listOf("earthquake_list_viewed", "app_opened"), state.events.map { it.name })
        assertEquals(2, state.totalCount)
        assertFalse(state.isFiltered)
    }

    @Test
    fun `query filters by name and keeps the total`() = runTest {
        storeEvents("app_opened", "alerts_toggled", "alert_area_set")
        collectState()
        viewModel.onQueryChanged("alert")
        val state: EventLogUiState = viewModel.uiState.value
        assertEquals(listOf("alert_area_set", "alerts_toggled"), state.events.map { it.name })
        assertEquals(3, state.totalCount)
        assertTrue(state.isFiltered)
    }

    @Test
    fun `query survives in the saved state`() = runTest {
        viewModel.onQueryChanged("app")
        assertEquals("app", savedStateHandle.get<String>("query"))
    }

    @Test
    fun `clearing empties the log`() = runTest {
        storeEvents("app_opened")
        collectState()
        viewModel.onClearConfirmed()
        assertEquals(0, viewModel.uiState.value.totalCount)
        assertTrue(dao.storedEvents.isEmpty())
    }

    @Test
    fun `share text contains only the visible events`() = runTest {
        storeEvents("app_opened", "alerts_toggled")
        collectState()
        viewModel.onQueryChanged("alerts")
        val text: String = viewModel.createShareText(ZoneOffset.UTC)
        assertEquals("1970-01-01T00:00:02Z alerts_toggled", text)
    }

    private suspend fun storeEvents(vararg names: String) {
        names.forEachIndexed { index, name ->
            dao.insert(AnalyticsEventEntity(name = name, params = emptyMap(), timestampEpochMs = (index + 1) * 1_000L))
        }
    }

    private fun TestScope.collectState(): EventLogUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState.value
    }
}
