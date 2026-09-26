package com.ahmetyildiz.quakealert.core.locale

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalizedContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun createLocalizedContext(): Context {
        val appLocales: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        if (appLocales.isEmpty) return context
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(appLocales.toLanguageTags()))
        return context.createConfigurationContext(configuration)
    }
}
