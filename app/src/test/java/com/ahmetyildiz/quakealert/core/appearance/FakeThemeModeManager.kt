package com.ahmetyildiz.quakealert.core.appearance

class FakeThemeModeManager(var currentMode: ThemeMode = ThemeMode.SYSTEM) : ThemeModeManager {

    var setCount: Int = 0
        private set

    override fun getSelectedMode(): ThemeMode = currentMode

    override fun setSelectedMode(mode: ThemeMode) {
        setCount++
        currentMode = mode
    }

    override fun applySelectedMode() = Unit
}
