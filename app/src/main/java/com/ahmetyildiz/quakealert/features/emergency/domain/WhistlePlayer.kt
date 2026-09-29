package com.ahmetyildiz.quakealert.features.emergency.domain

interface WhistlePlayer {

    fun start(): Boolean

    fun stop()
}
