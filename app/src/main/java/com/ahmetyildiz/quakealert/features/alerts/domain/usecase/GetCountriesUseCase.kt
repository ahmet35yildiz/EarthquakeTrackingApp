package com.ahmetyildiz.quakealert.features.alerts.domain.usecase

import com.ahmetyildiz.quakealert.features.alerts.domain.model.Country
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

class GetCountriesUseCase @Inject constructor() {

    operator fun invoke(displayLocale: Locale): List<Country> {
        val collator: Collator = Collator.getInstance(displayLocale)
        return Locale.getISOCountries()
            .map { code -> createCountry(code, displayLocale) }
            .sortedWith(compareBy(collator, Country::name))
    }

    private fun createCountry(code: String, displayLocale: Locale): Country {
        val regionLocale: Locale = Locale.Builder().setRegion(code).build()
        return Country(code = code, name = regionLocale.getDisplayCountry(displayLocale))
    }
}
