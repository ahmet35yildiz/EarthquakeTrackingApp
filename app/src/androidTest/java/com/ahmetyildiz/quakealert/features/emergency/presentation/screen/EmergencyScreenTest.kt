package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmergencyScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun safetyGuideCardOpensTheGuide() {
        var guideOpens = 0
        composeRule.setContent {
            QuakeAlertTheme {
                EmergencyScreen(onOpenSafetyGuide = { guideOpens++ })
            }
        }
        composeRule.onNodeWithText(string(R.string.emergency_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.safety_guide_card_description)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.safety_guide_title)).performClick()
        assertEquals(1, guideOpens)
    }

    private fun string(id: Int): String = composeRule.activity.getString(id)
}
