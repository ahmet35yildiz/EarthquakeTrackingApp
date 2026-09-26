package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class GetCountriesUseCaseTest {

    private val useCase = GetCountriesUseCase()

    @Test
    fun `every ISO country appears exactly once`() {
        val codes: List<String> = useCase(Locale.ENGLISH).map(Country::code)
        assertEquals(Locale.getISOCountries().toSet(), codes.toSet())
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `names are in the display language`() {
        assertEquals("Germany", findName(code = "DE", locale = Locale.ENGLISH))
        assertEquals("Almanya", findName(code = "DE", locale = TURKISH))
    }

    @Test
    fun `countries are sorted by the rules of the display language`() {
        val names: List<String> = useCase(TURKISH).map(Country::name)
        assertTrue(names.indexOf("Cezayir") < names.indexOf("Çin"))
        assertTrue(names.indexOf("Çin") < names.indexOf("Danimarka"))
    }

    private fun findName(code: String, locale: Locale): String = useCase(locale).first { it.code == code }.name

    private companion object {
        val TURKISH: Locale = Locale.forLanguageTag("tr")
    }
}
