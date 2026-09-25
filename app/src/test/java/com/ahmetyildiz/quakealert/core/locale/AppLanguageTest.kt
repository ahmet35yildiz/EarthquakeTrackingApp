package com.ahmetyildiz.quakealert.core.locale

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AppLanguageTest {

    @ParameterizedTest
    @CsvSource("en, English", "tr, Türkçe", "es, Español", "pt-BR, Português (Brasil)")
    fun `native name is written in the language itself and capitalized`(tag: String, expected: String) {
        val language = AppLanguage(tag)
        val nativeName: String = language.nativeName
        assertEquals(expected, nativeName)
    }
}
