package com.ahmetyildiz.quakealert.core.locale

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class CountryNamesTest {

    private val turkish: Locale = Locale.forLanguageTag("tr")

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        delimiter = '|',
        value = ["Japan|Japonya", "Turkey|Türkiye", "New Zealand|Yeni Zelanda", "Burma (Myanmar)|Myanmar (Burma)"],
    )
    fun `a country name is shown in the requested language`(englishName: String, expected: String) {
        assertEquals(expected, CountryNames.translate(englishName, turkish))
    }

    @ParameterizedTest
    @ValueSource(strings = ["Nevada", "CA", "Japan region", "Fiji Islands", "Tokyo, Japan"])
    fun `other names are not countries`(name: String) {
        assertNull(CountryNames.translate(name, turkish))
    }
}
