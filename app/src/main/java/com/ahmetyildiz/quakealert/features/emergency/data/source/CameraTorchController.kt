package com.ahmetyildiz.quakealert.features.emergency.data.source

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.ahmetyildiz.quakealert.features.emergency.domain.TorchController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CameraTorchController @Inject constructor(
    @ApplicationContext context: Context,
) : TorchController {

    private val cameraManager: CameraManager = context.getSystemService(CameraManager::class.java)
    private val torchCameraId: String? by lazy { findTorchCameraId() }

    override val isAvailable: Boolean
        get() = torchCameraId != null

    override fun setTorch(isOn: Boolean): Boolean {
        val cameraId: String = torchCameraId ?: return false
        return try {
            cameraManager.setTorchMode(cameraId, isOn)
            true
        } catch (exception: CameraAccessException) {
            false
        }
    }

    private fun findTorchCameraId(): String? =
        try {
            cameraManager.cameraIdList.firstOrNull(::hasFlashUnit)
        } catch (exception: CameraAccessException) {
            null
        }

    private fun hasFlashUnit(cameraId: String): Boolean =
        cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
}
