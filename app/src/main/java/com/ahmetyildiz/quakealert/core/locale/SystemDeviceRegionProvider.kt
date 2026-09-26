package com.ahmetyildiz.quakealert.core.locale

import android.content.Context
import androidx.core.app.LocaleManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class SystemDeviceRegionProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceRegionProvider {

    override fun getDeviceRegionCode(): String? {
        val systemLocale: Locale = LocaleManagerCompat.getSystemLocales(context)[0] ?: return null
        return systemLocale.country.takeIf { it.isNotBlank() }
    }
}
