package com.ahmetyildiz.quakealert.core.appearance

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppCompatThemeModeManager @Inject constructor(
    @ApplicationContext context: Context,
) : ThemeModeManager {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getSelectedMode(): ThemeMode {
        val storedName: String? = preferences.getString(KEY_THEME_MODE, null)
        return ThemeMode.entries.firstOrNull { it.name == storedName } ?: ThemeMode.SYSTEM
    }

    override fun setSelectedMode(mode: ThemeMode) {
        preferences.edit { putString(KEY_THEME_MODE, mode.name) }
        applySelectedMode()
    }

    override fun applySelectedMode() {
        AppCompatDelegate.setDefaultNightMode(getSelectedMode().toNightMode())
    }

    private fun ThemeMode.toNightMode(): Int = when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

    private companion object {
        const val PREFERENCES_NAME: String = "app_theme"
        const val KEY_THEME_MODE: String = "theme_mode"
    }
}
