package com.ahmetyildiz.quakealert.features.settings.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class DeveloperToolsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var backCount: Int = 0
    private var openEventLogCount: Int = 0

    @Test
    fun screenShowsTheAlertToolsAndOpensTheEventLog() {
        setContent()
        composeRule.onNodeWithText(ALERT_TOOLS_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.event_log_title)).performClick()
        assertEquals(1, openEventLogCount)
    }

    @Test
    fun backArrowReportsBack() {
        setContent()
        composeRule.onNodeWithContentDescription(string(R.string.action_back)).performClick()
        assertEquals(1, backCount)
    }

    private fun setContent() {
        composeRule.setContent {
            QuakeAlertTheme {
                DeveloperToolsScreen(onBack = { backCount++ }, onOpenEventLog = { openEventLogCount++ }) {
                    Text(text = ALERT_TOOLS_TEXT)
                }
            }
        }
    }

    private fun string(resId: Int): String = composeRule.activity.getString(resId)

    private companion object {
        const val ALERT_TOOLS_TEXT: String = "alert tools"
    }
}
