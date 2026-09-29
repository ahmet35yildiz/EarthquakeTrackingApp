package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.EmergencyToolValue
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.features.emergency.domain.EmergencyConfig
import com.ahmetyildiz.quakealert.features.emergency.domain.FakeTorchController
import com.ahmetyildiz.quakealert.features.emergency.domain.usecase.RunStrobeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(dispatcher)

    private val analyticsTracker = FakeAnalyticsTracker()
    private val strobeCycleMillis: Long =
        EmergencyConfig.STROBE_FLASH_DURATION.plus(EmergencyConfig.STROBE_PAUSE_DURATION).toMillis()

    @Test
    fun `device without a flashlight shows the strobe as unavailable`() {
        val viewModel: EmergencyViewModel = createViewModel(FakeTorchController(isAvailable = false))
        assertFalse(viewModel.uiState.value.isStrobeAvailable)
    }

    @Test
    fun `strobe starts and stops with the toggle and both are recorded`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        assertTrue(viewModel.uiState.value.isStrobeOn)
        assertEquals(listOf(true), torch.switches)
        viewModel.onStrobeToggled()
        assertFalse(viewModel.uiState.value.isStrobeOn)
        assertEquals(false, torch.switches.last())
        assertEquals(listOf(toggled(isEnabled = true), toggled(isEnabled = false)), analyticsTracker.events)
    }

    @Test
    fun `leaving the screen stops the strobe without recording a toggle`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        viewModel.onScreenStopped(isConfigurationChange = false)
        assertFalse(viewModel.uiState.value.isStrobeOn)
        assertEquals(false, torch.switches.last())
        assertEquals(listOf(toggled(isEnabled = true)), analyticsTracker.events)
    }

    @Test
    fun `rotation keeps the strobe running`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        viewModel.onScreenStopped(isConfigurationChange = true)
        assertTrue(viewModel.uiState.value.isStrobeOn)
        dispatcher.scheduler.advanceTimeBy(strobeCycleMillis)
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(true, false, true), torch.switches)
    }

    @Test
    fun `a torch that cannot be switched stops the strobe and says so`() {
        val viewModel: EmergencyViewModel = createViewModel(FakeTorchController(workingSwitchCount = 0))
        viewModel.onStrobeToggled()
        assertFalse(viewModel.uiState.value.isStrobeOn)
        assertTrue(viewModel.uiState.value.hasStrobeFailed)
    }

    @Test
    fun `starting again after a failure clears the failure message`() {
        val torch = FakeTorchController(workingSwitchCount = 0)
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        torch.workingSwitchCount = Int.MAX_VALUE
        viewModel.onStrobeToggled()
        assertTrue(viewModel.uiState.value.isStrobeOn)
        assertFalse(viewModel.uiState.value.hasStrobeFailed)
    }

    private fun createViewModel(torch: FakeTorchController): EmergencyViewModel =
        EmergencyViewModel(torch, RunStrobeUseCase(torch), analyticsTracker)

    private fun toggled(isEnabled: Boolean): AnalyticsEvent =
        AnalyticsEvent.EmergencyToolToggled(EmergencyToolValue.STROBE, isEnabled)
}
