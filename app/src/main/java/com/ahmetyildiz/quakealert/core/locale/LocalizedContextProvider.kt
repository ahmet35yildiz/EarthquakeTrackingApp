package com.ahmetyildiz.quakealert.core.locale

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class LocalizedContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLanguageManager: AppLanguageManager,
) {

    fun createLocalizedContext(): Context {
        val language: AppLanguage = appLanguageManager.getSelectedLanguage() ?: return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(language.tag))
        return context.createConfigurationContext(configuration)
    }
}
