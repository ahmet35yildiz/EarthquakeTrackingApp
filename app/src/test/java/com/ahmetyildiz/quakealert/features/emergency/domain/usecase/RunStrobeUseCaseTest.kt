package com.ahmetyildiz.quakealert.features.emergency.domain.usecase

import com.ahmetyildiz.quakealert.features.emergency.domain.EmergencyConfig
import com.ahmetyildiz.quakealert.features.emergency.domain.FakeTorchController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunStrobeUseCaseTest {

    private val flashMillis: Long = EmergencyConfig.STROBE_FLASH_DURATION.toMillis()
    private val pauseMillis: Long = EmergencyConfig.STROBE_PAUSE_DURATION.toMillis()

    @Test
    fun `torch flashes on and off at the configured rhythm`() = runTest {
        val torch = FakeTorchController()
        val job: Job = launch { RunStrobeUseCase(torch)() }
        runCurrent()
        assertEquals(listOf(true), torch.switches)
        advanceTimeBy(flashMillis)
        runCurrent()
        assertEquals(listOf(true, false), torch.switches)
        advanceTimeBy(pauseMillis)
        runCurrent()
        assertEquals(listOf(true, false, true), torch.switches)
        job.cancel()
    }

    @Test
    fun `stopping the strobe turns the torch off`() = runTest {
        val torch = FakeTorchController()
        val job: Job = launch { RunStrobeUseCase(torch)() }
        runCurrent()
        job.cancel()
        runCurrent()
        assertEquals(listOf(true, false), torch.switches)
    }

    @Test
    fun `strobe ends and turns the torch off when the torch stops responding`() = runTest {
        val torch = FakeTorchController(workingSwitchCount = 3)
        RunStrobeUseCase(torch)()
        assertEquals(listOf(true, false, true, false, false), torch.switches)
    }
}
