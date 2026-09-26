package com.ahmetyildiz.quakealert.core.locale

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.ahmetyildiz.quakealert.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class AppCompatLanguageManager @Inject constructor(
    @ApplicationContext context: Context,
) : AppLanguageManager {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getSupportedLanguages(): List<AppLanguage> =
        BuildConfig.SUPPORTED_LANGUAGE_TAGS.map(::AppLanguage)

    override fun getSelectedLanguage(): AppLanguage? {
        val selectedLocale: Locale = AppCompatDelegate.getApplicationLocales()[0] ?: findStoredLocale() ?: return null
        return getSupportedLanguages().firstOrNull { it.tag == selectedLocale.toLanguageTag() }
            ?: getSupportedLanguages().firstOrNull { Locale.forLanguageTag(it.tag).language == selectedLocale.language }
    }

    override fun setSelectedLanguage(language: AppLanguage?) {
        preferences.edit { if (language == null) remove(KEY_LANGUAGE_TAG) else putString(KEY_LANGUAGE_TAG, language.tag) }
        val locales: LocaleListCompat = language
            ?.let { LocaleListCompat.forLanguageTags(it.tag) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun findStoredLocale(): Locale? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return null
        return preferences.getString(KEY_LANGUAGE_TAG, null)?.let(Locale::forLanguageTag)
    }

    private companion object {
        const val PREFERENCES_NAME: String = "app_language"
        const val KEY_LANGUAGE_TAG: String = "language_tag"
    }
}
