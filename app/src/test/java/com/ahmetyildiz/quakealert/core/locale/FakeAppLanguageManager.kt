package com.ahmetyildiz.quakealert.core.locale

class FakeAppLanguageManager(
    private val supportedLanguages: List<AppLanguage> = listOf(AppLanguage("en"), AppLanguage("tr")),
    var currentLanguage: AppLanguage? = null,
) : AppLanguageManager {

    var setCount: Int = 0
        private set

    override fun getSupportedLanguages(): List<AppLanguage> = supportedLanguages

    override fun getSelectedLanguage(): AppLanguage? = currentLanguage

    override fun setSelectedLanguage(language: AppLanguage?) {
        setCount++
        currentLanguage = language
    }
}
