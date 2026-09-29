package com.ahmetyildiz.quakealert.features.emergency.domain.usecase

import com.ahmetyildiz.quakealert.features.emergency.domain.EmergencyConfig
import com.ahmetyildiz.quakealert.features.emergency.domain.TorchController
import kotlinx.coroutines.delay
import javax.inject.Inject

class RunStrobeUseCase @Inject constructor(
    private val torchController: TorchController,
) {

    suspend operator fun invoke() {
        try {
            var isTorchWorking = true
            while (isTorchWorking) isTorchWorking = flashOnce()
        } finally {
            torchController.setTorch(isOn = false)
        }
    }

    private suspend fun flashOnce(): Boolean {
        if (!torchController.setTorch(isOn = true)) return false
        delay(EmergencyConfig.STROBE_FLASH_DURATION.toMillis())
        if (!torchController.setTorch(isOn = false)) return false
        delay(EmergencyConfig.STROBE_PAUSE_DURATION.toMillis())
        return true
    }
}
