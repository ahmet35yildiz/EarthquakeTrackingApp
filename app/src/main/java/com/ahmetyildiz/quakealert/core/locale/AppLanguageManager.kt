package com.ahmetyildiz.quakealert.core.locale

/** Reads and changes the app's own UI language, independently of the device language. */
interface AppLanguageManager {

    /** All languages the app is translated into, default language first. */
    fun getSupportedLanguages(): List<AppLanguage>

    /** The language chosen in the app, or null when the app follows the device language. */
    fun getSelectedLanguage(): AppLanguage?

    /** Applies [language] immediately (recreates visible activities); null follows the device language again. */
    fun setSelectedLanguage(language: AppLanguage?)
}
