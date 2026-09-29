package com.ahmetyildiz.quakealert.features.alerts.presentation.component

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
class AlertFeedbackCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val answers: MutableList<Boolean> = mutableListOf()

    @Test
    fun yesAndNoReportTheAnswer() {
        setContent(isAnswered = false)
        composeRule.onNodeWithText(string(R.string.alert_feedback_question)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_yes)).performClick()
        composeRule.onNodeWithText(string(R.string.action_no)).performClick()
        assertEquals(listOf(true, false), answers)
    }

    @Test
    fun answeredCardThanksInsteadOfAsking() {
        setContent(isAnswered = true)
        composeRule.onNodeWithText(string(R.string.alert_feedback_thanks)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_yes)).assertDoesNotExist()
    }

    private fun string(id: Int): String = composeRule.activity.getString(id)

    private fun setContent(isAnswered: Boolean) {
        composeRule.setContent {
            QuakeAlertTheme { AlertFeedbackCard(isAnswered = isAnswered, onAnswer = { answers += it }) }
        }
    }
}
