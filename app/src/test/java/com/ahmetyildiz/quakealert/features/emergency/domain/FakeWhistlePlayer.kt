package com.ahmetyildiz.quakealert.features.emergency.domain

class FakeWhistlePlayer(var canPlay: Boolean = true) : WhistlePlayer {

    var isPlaying: Boolean = false
        private set

    override fun start(): Boolean {
        isPlaying = canPlay
        return canPlay
    }

    override fun stop() {
        isPlaying = false
    }
}
