package com.ahmetyildiz.quakealert.features.emergency.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.EmergencyUiState
import com.ahmetyildiz.quakealert.features.emergency.presentation.viewmodel.ToolState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmergencyScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val clickedActions: MutableList<String> = mutableListOf()
    private val actions = EmergencyActions(
        onWhistleToggled = { clickedActions += "whistle" },
        onStrobeToggled = { clickedActions += "strobe" },
        onOpenSafetyGuide = { clickedActions += "guide" },
    )

    @Test
    fun safetyGuideCardOpensTheGuide() {
        setContent(EmergencyUiState(isStrobeAvailable = true))
        composeRule.onNodeWithText(string(R.string.emergency_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.safety_guide_card_description)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.safety_guide_title)).performClick()
        assertEquals(listOf("guide"), clickedActions)
    }

    @Test
    fun eachToolStartsFromItsOwnCard() {
        setContent(EmergencyUiState(isStrobeAvailable = true))
        composeRule.onNodeWithText(string(R.string.whistle_description)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.strobe_description)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(R.string.action_start))[0].performClick()
        composeRule.onAllNodesWithText(string(R.string.action_start))[1].performClick()
        assertEquals(listOf("whistle", "strobe"), clickedActions)
    }

    @Test
    fun runningStrobeOffersStop() {
        setContent(EmergencyUiState(isStrobeAvailable = true, strobe = ToolState(isOn = true)))
        composeRule.onNodeWithText(string(R.string.action_stop)).performClick()
        assertEquals(listOf("strobe"), clickedActions)
    }

    @Test
    fun deviceWithoutFlashlightSaysSoAndHasNoButton() {
        setContent(EmergencyUiState(isStrobeAvailable = false))
        composeRule.onNodeWithText(string(R.string.strobe_unavailable)).assertIsDisplayed()
        composeRule.onAllNodesWithText(string(R.string.action_start)).assertCountEquals(1)
    }

    @Test
    fun failedStrobeExplainsWhy() {
        setContent(EmergencyUiState(isStrobeAvailable = true, strobe = ToolState(hasFailed = true)))
        composeRule.onNodeWithText(string(R.string.strobe_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.whistle_failed)).assertDoesNotExist()
    }

    @Test
    fun failedWhistleExplainsWhy() {
        setContent(EmergencyUiState(isStrobeAvailable = true, whistle = ToolState(hasFailed = true)))
        composeRule.onNodeWithText(string(R.string.whistle_failed)).assertIsDisplayed()
    }

    private fun setContent(uiState: EmergencyUiState) {
        composeRule.setContent {
            QuakeAlertTheme {
                EmergencyScreen(uiState = uiState, actions = actions)
            }
        }
    }

    private fun string(id: Int): String = composeRule.activity.getString(id)
}
