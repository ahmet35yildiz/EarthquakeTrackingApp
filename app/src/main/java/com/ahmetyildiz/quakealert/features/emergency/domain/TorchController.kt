package com.ahmetyildiz.quakealert.features.emergency.domain

interface TorchController {

    val isAvailable: Boolean

    fun setTorch(isOn: Boolean): Boolean
}
