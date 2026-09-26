package com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel

import com.ahmetyildiz.quakealert.BuildConfig
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.FakeAnalyticsTracker
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.locale.FakeAppLanguageManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsViewModelTest {

    private val english = AppLanguage("en")
    private val turkish = AppLanguage("tr")
    private val languageManager = FakeAppLanguageManager()
    private val analyticsTracker = FakeAnalyticsTracker()
    private var areNotificationsAllowed: Boolean = true
    private val viewModel: SettingsViewModel by lazy {
        SettingsViewModel(
            appLanguageManager = languageManager,
            notificationPermissionChecker = { areNotificationsAllowed },
            analyticsTracker = analyticsTracker,
        )
    }

    @Test
    fun `state lists the supported languages, the selection, permission and version`() {
        languageManager.currentLanguage = turkish
        areNotificationsAllowed = false
        val state: SettingsUiState = viewModel.uiState.value
        assertEquals(listOf(english, turkish), state.languages)
        assertEquals(turkish, state.selectedLanguage)
        assertFalse(state.areNotificationsAllowed)
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
    fun `resuming re-reads permission and a language changed in the system settings`() {
        assertTrue(viewModel.uiState.value.areNotificationsAllowed)
        areNotificationsAllowed = false
        languageManager.currentLanguage = turkish
        viewModel.onScreenResumed()
        assertFalse(viewModel.uiState.value.areNotificationsAllowed)
        assertEquals(turkish, viewModel.uiState.value.selectedLanguage)
    }
}
