package com.ahmetyildiz.quakealert.core.ui.format

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class PlaceFormatsTest {

    @Test
    fun englishShowsTheOriginalText() {
        val resources: Resources = resourcesFor("en")
        listOf("28 km NW of Preston, Nevada", "12 km SW of Seferihisar, Turkey", "south of the Fiji Islands")
            .forEach { assertEquals(it, formatPlace(it, resources)) }
    }

    @Test
    fun turkishTranslatesDirectionAndCountryAndKeepsPlaceNames() {
        val resources: Resources = resourcesFor("tr")
        assertEquals(
            "Preston, Nevada · 28\u00A0km\u00A0Kuzey-Kuzeybatı",
            formatPlace("28 km NNW of Preston, Nevada", resources),
        )
        assertEquals(
            "Tokyo, Japonya · 5\u00A0km\u00A0Doğu-Kuzeydoğu",
            formatPlace("5 km ENE of Tokyo, Japan", resources),
        )
        assertEquals(
            "Hualien City, Tayvan · 2,5\u00A0km\u00A0Güney",
            formatPlace("2.5 km S of Hualien City, Taiwan", resources),
        )
    }

    @Test
    fun turkishTranslatesRegionPhrasesAndKnownNames() {
        val resources: Resources = resourcesFor("tr")
        mapOf(
            "south of the Fiji Islands" to "Fiji Adaları · Güneyi",
            "Iceland region" to "İzlanda bölgesi",
            "Izu Islands, Japan region" to "Izu Adaları, Japonya bölgesi",
            "northern Mid-Atlantic Ridge" to "Orta Atlantik Sırtı · Kuzeyi",
            "off the coast of Oregon" to "Oregon açıkları",
            "off the east coast of Honshu, Japan" to "Honshu · Doğu açıkları, Japonya",
            "Kermadec Islands, New Zealand" to "Kermadec Adaları, Yeni Zelanda",
            "Easter Island region" to "Paskalya Adası bölgesi",
            "Mid-Indian Ridge" to "Orta Hint Sırtı",
            "Utah" to "Utah",
        ).forEach { (text, expected) -> assertEquals(expected, formatPlace(text, resources)) }
    }

    private fun resourcesFor(languageTag: String): Resources {
        val context: Context = ApplicationProvider.getApplicationContext()
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(languageTag))
        return context.createConfigurationContext(configuration).resources
    }
}
