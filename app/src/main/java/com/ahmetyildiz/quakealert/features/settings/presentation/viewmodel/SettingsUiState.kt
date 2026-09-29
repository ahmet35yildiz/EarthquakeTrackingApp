package com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel

import com.ahmetyildiz.quakealert.core.appearance.ThemeMode
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess

data class SettingsUiState(
    val languages: List<AppLanguage> = emptyList(),
    val selectedLanguage: AppLanguage? = null,
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationAccess: NotificationAccess = NotificationAccess.ALLOWED,
    val appVersion: AppVersion = AppVersion(name = "", code = 0),
)

data class AppVersion(val name: String, val code: Int)
