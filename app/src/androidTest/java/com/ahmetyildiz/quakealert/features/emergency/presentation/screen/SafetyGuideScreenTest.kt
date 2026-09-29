package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.SafetyGuideSection
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SafetyGuideScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val selectedSections: MutableList<SafetyGuideSection> = mutableListOf()
    private var backClicks: Int = 0

    @Test
    fun selectedSectionShowsAllItsItemsAndTheSource() {
        setContent(SafetyGuideSection.DURING)
        composeRule.onNodeWithText(string(R.string.safety_guide_section_during)).assertIsSelected()
        items(R.array.safety_guide_during_items).forEach { item ->
            composeRule.onNodeWithText(item).performScrollTo().assertIsDisplayed()
        }
        composeRule.onNodeWithText(string(R.string.safety_guide_source)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(items(R.array.safety_guide_before_items).first()).assertDoesNotExist()
    }

    @Test
    fun tappingATabShowsThatSection() {
        var shownSection: SafetyGuideSection by mutableStateOf(SafetyGuideSection.BEFORE)
        composeRule.setContent {
            QuakeAlertTheme {
                SafetyGuideScreen(
                    selectedSection = shownSection,
                    onSectionSelected = { section ->
                        selectedSections += section
                        shownSection = section
                    },
                    onBack = { backClicks++ },
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.safety_guide_section_after)).performClick()
        composeRule.onNodeWithText(items(R.array.safety_guide_after_items).first()).assertIsDisplayed()
        assertEquals(listOf(SafetyGuideSection.AFTER), selectedSections)
    }

    @Test
    fun backIsReported() {
        setContent(SafetyGuideSection.BEFORE)
        composeRule.onNodeWithContentDescription(string(R.string.action_back)).performClick()
        assertEquals(1, backClicks)
    }

    @Test
    fun everySectionHasFiveToSevenItemsAndTheSameCountInEnglishAndTurkish() {
        val arrays: List<Int> = listOf(
            R.array.safety_guide_before_items,
            R.array.safety_guide_during_items,
            R.array.safety_guide_after_items,
        )
        arrays.forEach { id ->
            val englishCount: Int = localizedResources(ENGLISH).getStringArray(id).size
            val turkishCount: Int = localizedResources(TURKISH).getStringArray(id).size
            assertTrue(englishCount in MIN_ITEMS..MAX_ITEMS)
            assertEquals(englishCount, turkishCount)
        }
    }

    private fun setContent(section: SafetyGuideSection) {
        composeRule.setContent {
            QuakeAlertTheme {
                SafetyGuideScreen(
                    selectedSection = section,
                    onSectionSelected = { selectedSections += it },
                    onBack = { backClicks++ },
                )
            }
        }
    }

    private fun localizedResources(languageTag: String): Resources {
        val configuration = Configuration(composeRule.activity.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(languageTag))
        return composeRule.activity.createConfigurationContext(configuration).resources
    }

    private fun items(id: Int): List<String> = composeRule.activity.resources.getStringArray(id).toList()

    private fun string(id: Int): String = composeRule.activity.getString(id)

    private companion object {
        const val MIN_ITEMS: Int = 5
        const val MAX_ITEMS: Int = 7
        const val ENGLISH: String = "en"
        const val TURKISH: String = "tr"
    }
}
