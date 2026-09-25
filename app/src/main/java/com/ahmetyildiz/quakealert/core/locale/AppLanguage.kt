package com.ahmetyildiz.quakealert.core.locale

import java.util.Locale

data class AppLanguage(val tag: String) {

    private val locale: Locale
        get() = Locale.forLanguageTag(tag)

    val nativeName: String
        get() = locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
}
