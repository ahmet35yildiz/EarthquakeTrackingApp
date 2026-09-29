package com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.EmergencyToolValue
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.testing.MainDispatcherExtension
import com.ahmetyildiz.quakealert.features.emergency.domain.EmergencyConfig
import com.ahmetyildiz.quakealert.features.emergency.domain.FakeTorchController
import com.ahmetyildiz.quakealert.features.emergency.domain.FakeWhistlePlayer
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
    private val whistlePlayer = FakeWhistlePlayer()
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
        assertTrue(viewModel.uiState.value.strobe.isOn)
        assertEquals(listOf(true), torch.switches)
        viewModel.onStrobeToggled()
        assertFalse(viewModel.uiState.value.strobe.isOn)
        assertEquals(false, torch.switches.last())
        assertEquals(listOf(toggled(isEnabled = true), toggled(isEnabled = false)), analyticsTracker.events)
    }

    @Test
    fun `leaving the screen stops the strobe without recording a toggle`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        viewModel.onScreenStopped(isConfigurationChange = false)
        assertFalse(viewModel.uiState.value.strobe.isOn)
        assertEquals(false, torch.switches.last())
        assertEquals(listOf(toggled(isEnabled = true)), analyticsTracker.events)
    }

    @Test
    fun `rotation keeps the strobe running`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        viewModel.onScreenStopped(isConfigurationChange = true)
        assertTrue(viewModel.uiState.value.strobe.isOn)
        dispatcher.scheduler.advanceTimeBy(strobeCycleMillis)
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(true, false, true), torch.switches)
    }

    @Test
    fun `a torch that cannot be switched stops the strobe and says so`() {
        val viewModel: EmergencyViewModel = createViewModel(FakeTorchController(workingSwitchCount = 0))
        viewModel.onStrobeToggled()
        assertFalse(viewModel.uiState.value.strobe.isOn)
        assertTrue(viewModel.uiState.value.strobe.hasFailed)
    }

    @Test
    fun `starting again after a failure clears the failure message`() {
        val torch = FakeTorchController(workingSwitchCount = 0)
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onStrobeToggled()
        torch.workingSwitchCount = Int.MAX_VALUE
        viewModel.onStrobeToggled()
        assertTrue(viewModel.uiState.value.strobe.isOn)
        assertFalse(viewModel.uiState.value.strobe.hasFailed)
    }

    @Test
    fun `whistle starts and stops with the toggle and both are recorded`() {
        val viewModel: EmergencyViewModel = createViewModel()
        viewModel.onWhistleToggled()
        assertTrue(whistlePlayer.isPlaying)
        assertTrue(viewModel.uiState.value.whistle.isOn)
        assertTrue(viewModel.uiState.value.isAnyToolOn)
        viewModel.onWhistleToggled()
        assertFalse(whistlePlayer.isPlaying)
        assertFalse(viewModel.uiState.value.isAnyToolOn)
        val expected: List<AnalyticsEvent> = listOf(
            toggled(isEnabled = true, tool = EmergencyToolValue.WHISTLE),
            toggled(isEnabled = false, tool = EmergencyToolValue.WHISTLE),
        )
        assertEquals(expected, analyticsTracker.events)
    }

    @Test
    fun `a whistle that cannot play says so and stays off`() {
        whistlePlayer.canPlay = false
        val viewModel: EmergencyViewModel = createViewModel()
        viewModel.onWhistleToggled()
        assertEquals(ToolState(isOn = false, hasFailed = true), viewModel.uiState.value.whistle)
    }

    @Test
    fun `leaving the screen stops both tools`() {
        val torch = FakeTorchController()
        val viewModel: EmergencyViewModel = createViewModel(torch)
        viewModel.onWhistleToggled()
        viewModel.onStrobeToggled()
        viewModel.onScreenStopped(isConfigurationChange = false)
        assertFalse(whistlePlayer.isPlaying)
        assertEquals(false, torch.switches.last())
        assertFalse(viewModel.uiState.value.isAnyToolOn)
    }

    @Test
    fun `rotation keeps the whistle playing`() {
        val viewModel: EmergencyViewModel = createViewModel()
        viewModel.onWhistleToggled()
        viewModel.onScreenStopped(isConfigurationChange = true)
        assertTrue(whistlePlayer.isPlaying)
    }

    private fun createViewModel(torch: FakeTorchController = FakeTorchController()): EmergencyViewModel =
        EmergencyViewModel(torch, RunStrobeUseCase(torch), whistlePlayer, analyticsTracker)

    private fun toggled(isEnabled: Boolean, tool: EmergencyToolValue = EmergencyToolValue.STROBE): AnalyticsEvent =
        AnalyticsEvent.EmergencyToolToggled(tool, isEnabled)
}
