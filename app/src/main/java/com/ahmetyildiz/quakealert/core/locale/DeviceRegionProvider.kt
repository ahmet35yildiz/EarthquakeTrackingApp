package com.ahmetyildiz.quakealert.core.locale

fun interface DeviceRegionProvider {

    fun getDeviceRegionCode(): String?
}
