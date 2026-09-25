package com.ahmetyildiz.quakealert.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ahmetyildiz.quakealert.BuildConfig
import java.util.Locale
import javax.inject.Inject

/**
 * [AppLanguageManager] backed by the AppCompat per-app language API: the system stores the choice on Android 13+,
 * AppCompat stores it below (see `AppLocalesMetadataHolderService` in the manifest).
 */
class AppCompatLanguageManager @Inject constructor() : AppLanguageManager {

    override fun getSupportedLanguages(): List<AppLanguage> =
        BuildConfig.SUPPORTED_LANGUAGE_TAGS.map(::AppLanguage)

    override fun getSelectedLanguage(): AppLanguage? {
        val selectedLocale: Locale = AppCompatDelegate.getApplicationLocales()[0] ?: return null
        return getSupportedLanguages().firstOrNull { it.tag == selectedLocale.toLanguageTag() }
            ?: getSupportedLanguages().firstOrNull { Locale.forLanguageTag(it.tag).language == selectedLocale.language }
    }

    override fun setSelectedLanguage(language: AppLanguage?) {
        val locales: LocaleListCompat = language
            ?.let { LocaleListCompat.forLanguageTags(it.tag) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
