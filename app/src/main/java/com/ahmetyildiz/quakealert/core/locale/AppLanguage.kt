package com.ahmetyildiz.quakealert.core.locale

import java.util.Locale

/** A language the app ships translations for, identified by its BCP 47 tag (e.g. "en", "tr", "pt-BR"). */
data class AppLanguage(val tag: String) {

    private val locale: Locale
        get() = Locale.forLanguageTag(tag)

    /** Name of the language written in that language, e.g. "Türkçe" for "tr". */
    val nativeName: String
        get() = locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
}
