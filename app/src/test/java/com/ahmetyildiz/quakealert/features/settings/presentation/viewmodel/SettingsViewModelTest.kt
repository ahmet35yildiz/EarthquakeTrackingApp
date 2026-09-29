package com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel

import com.ahmetyildiz.quakealert.BuildConfig
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.appearance.FakeThemeModeManager
import com.ahmetyildiz.quakealert.core.appearance.ThemeMode
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.locale.FakeAppLanguageManager
import com.ahmetyildiz.quakealert.core.notification.NotificationAccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsViewModelTest {

    private val english = AppLanguage("en")
    private val turkish = AppLanguage("tr")
    private val languageManager = FakeAppLanguageManager()
    private val themeModeManager = FakeThemeModeManager()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var notificationAccess: NotificationAccess = NotificationAccess.ALLOWED
    private val viewModel: SettingsViewModel by lazy {
        SettingsViewModel(
            appLanguageManager = languageManager,
            themeModeManager = themeModeManager,
            notificationAccessChecker = { notificationAccess },
            analyticsTracker = analyticsTracker,
        )
    }

    @Test
    fun `state lists the supported languages, the selection, permission and version`() {
        languageManager.currentLanguage = turkish
        notificationAccess = NotificationAccess.APP_BLOCKED
        val state: SettingsUiState = viewModel.uiState.value
        assertEquals(listOf(english, turkish), state.languages)
        assertEquals(turkish, state.selectedLanguage)
        assertEquals(NotificationAccess.APP_BLOCKED, state.notificationAccess)
        assertEquals(AppVersion(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), state.appVersion)
    }

    @Test
    fun `picking a language applies it and tracks the change from system default`() {
        viewModel.onLanguageSelected(turkish)
        assertEquals(turkish, languageManager.currentLanguage)
        assertEquals(turkish, viewModel.uiState.value.selectedLanguage)
        assertEquals(listOf(AnalyticsEvent.LanguageChanged(from = null, to = turkish)), analyticsTracker.events)
    }

    @Test
    fun `picking system default clears the app language`() {
        languageManager.currentLanguage = english
        viewModel.onLanguageSelected(null)
        assertNull(languageManager.currentLanguage)
        assertNull(viewModel.uiState.value.selectedLanguage)
        assertEquals(listOf(AnalyticsEvent.LanguageChanged(from = english, to = null)), analyticsTracker.events)
    }

    @Test
    fun `picking the current language changes and tracks nothing`() {
        languageManager.currentLanguage = turkish
        viewModel.onLanguageSelected(turkish)
        assertEquals(0, languageManager.setCount)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `system theme is selected by default`() {
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.selectedThemeMode)
    }

    @Test
    fun `picking a theme applies it and tracks the change`() {
        viewModel.onThemeModeSelected(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, themeModeManager.currentMode)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.selectedThemeMode)
        val expected = AnalyticsEvent.ThemeChanged(from = ThemeMode.SYSTEM, to = ThemeMode.DARK)
        assertEquals(listOf(expected), analyticsTracker.events)
    }

    @Test
    fun `picking the current theme changes and tracks nothing`() {
        themeModeManager.currentMode = ThemeMode.LIGHT
        viewModel.onThemeModeSelected(ThemeMode.LIGHT)
        assertEquals(0, themeModeManager.setCount)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `resuming re-reads permission and a language changed in the system settings`() {
        assertEquals(NotificationAccess.ALLOWED, viewModel.uiState.value.notificationAccess)
        notificationAccess = NotificationAccess.APP_BLOCKED
        languageManager.currentLanguage = turkish
        viewModel.onScreenResumed()
        assertEquals(NotificationAccess.APP_BLOCKED, viewModel.uiState.value.notificationAccess)
        assertEquals(turkish, viewModel.uiState.value.selectedLanguage)
    }
}
