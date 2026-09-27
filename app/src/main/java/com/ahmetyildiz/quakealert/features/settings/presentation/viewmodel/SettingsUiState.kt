package com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.appearance.ThemeMode
import com.ahmetyildiz.quakealert.core.locale.AppLanguage

data class SettingsUiState(
    val languages: List<AppLanguage> = emptyList(),
    val selectedLanguage: AppLanguage? = null,
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val areNotificationsAllowed: Boolean = true,
    val appVersion: AppVersion = AppVersion(name = "", code = 0),
)

data class AppVersion(val name: String, val code: Int)
