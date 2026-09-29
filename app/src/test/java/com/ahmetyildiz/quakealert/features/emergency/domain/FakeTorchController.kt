package com.ahmetyildiz.quakealert.features.emergency.domain

class FakeTorchController(
    override val isAvailable: Boolean = true,
    var workingSwitchCount: Int = Int.MAX_VALUE,
) : TorchController {

    val switches: MutableList<Boolean> = mutableListOf()

    override fun setTorch(isOn: Boolean): Boolean {
        switches += isOn
        return switches.size <= workingSwitchCount
    }
}
