package com.ahmetyildiz.quakealert.core.locale

interface AppLanguageManager {

    fun getSupportedLanguages(): List<AppLanguage>

    fun getSelectedLanguage(): AppLanguage?

    fun setSelectedLanguage(language: AppLanguage?)
}
