package com.ahmetyildiz.quakealert.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ahmetyildiz.quakealert.BuildConfig
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsEvent
import com.ahmetyildiz.quakealert.core.analytics.AnalyticsTracker
import com.ahmetyildiz.quakealert.core.locale.AppLanguage
import com.ahmetyildiz.quakealert.core.locale.AppLanguageManager
import com.ahmetyildiz.quakealert.core.notification.NotificationPermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appLanguageManager: AppLanguageManager,
    private val notificationPermissionChecker: NotificationPermissionChecker,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(readState())

    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    fun onScreenResumed() {
        mutableUiState.value = readState()
    }

    fun onLanguageSelected(language: AppLanguage?) {
        val previousLanguage: AppLanguage? = appLanguageManager.getSelectedLanguage()
        if (language == previousLanguage) return
        analyticsTracker.track(AnalyticsEvent.LanguageChanged(from = previousLanguage, to = language))
        appLanguageManager.setSelectedLanguage(language)
        mutableUiState.value = readState()
    }

    private fun readState(): SettingsUiState =
        SettingsUiState(
            languages = appLanguageManager.getSupportedLanguages(),
            selectedLanguage = appLanguageManager.getSelectedLanguage(),
            areNotificationsAllowed = notificationPermissionChecker.areNotificationsAllowed(),
            appVersion = AppVersion(name = BuildConfig.VERSION_NAME, code = BuildConfig.VERSION_CODE),
        )
}
