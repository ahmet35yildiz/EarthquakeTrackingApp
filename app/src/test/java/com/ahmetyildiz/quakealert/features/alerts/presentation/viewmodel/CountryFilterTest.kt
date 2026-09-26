package com.ahmetyildiz.quakealert.features.alerts.presentation.viewmodel

import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class CountryFilterTest {

    private val countries: List<Country> = listOf(
        Country("DE", "Almanya"),
        Country("KG", "Kırgızistan"),
        Country("TR", "Türkiye"),
        Country("IS", "İzlanda"),
    )

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource(
        "turkiye, TR",
        "TÜRK, TR",
        "kirgiz, KG",
        "izlanda, IS",
        "İZL, IS",
        "de, DE",
        "' almanya ', DE",
    )
    fun `matches names without accents or case and exact codes`(query: String, expectedCode: String) {
        assertEquals(listOf(expectedCode), countries.filterByName(query).map(Country::code))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   "])
    fun `blank query keeps every country`(query: String) {
        assertEquals(countries, countries.filterByName(query))
    }

    @ParameterizedTest
    @ValueSource(strings = ["atlantis", "zz"])
    fun `unknown query matches nothing`(query: String) {
        assertEquals(emptyList<Country>(), countries.filterByName(query))
    }
}
