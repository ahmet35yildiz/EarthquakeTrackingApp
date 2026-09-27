package com.ahmetyildiz.quakealert.core.appearance

interface ThemeModeManager {

    fun getSelectedMode(): ThemeMode

    fun setSelectedMode(mode: ThemeMode)

    fun applySelectedMode()
}
