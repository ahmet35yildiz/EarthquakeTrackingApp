package com.ahmetyildiz.quakealert.core.locale

import java.util.Locale

object CountryNames {

    private val DATA_SOURCE_ALIASES: Map<String, String> = mapOf(
        "Turkey" to "TR",
        "Timor Leste" to "TL",
        "Burma (Myanmar)" to "MM",
        "Russia" to "RU",
        "Vietnam" to "VN",
        "Micronesia" to "FM",
        "Federated States of Micronesia" to "FM",
        "Saint Helena" to "SH",
        "Svalbard and Jan Mayen" to "SJ",
        "Wallis and Futuna" to "WF",
    )

    private val codesByEnglishName: Map<String, String> by lazy {
        Locale.getISOCountries().associateBy { code -> regionLocale(code).getDisplayCountry(Locale.ENGLISH) } +
            DATA_SOURCE_ALIASES
    }

    fun translate(englishName: String, locale: Locale): String? =
        codesByEnglishName[englishName]?.let { code -> regionLocale(code).getDisplayCountry(locale) }

    private fun regionLocale(code: String): Locale = Locale.Builder().setRegion(code).build()
}
