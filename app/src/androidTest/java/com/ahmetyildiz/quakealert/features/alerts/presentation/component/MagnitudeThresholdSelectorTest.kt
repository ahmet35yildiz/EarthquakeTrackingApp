package com.ahmetyildiz.quakealert.features.alerts.presentation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmetyildiz.quakealert.R
import com.ahmetyildiz.quakealert.core.ui.theme.QuakeAlertTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MagnitudeThresholdSelectorTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val changes: MutableList<Double> = mutableListOf()

    @Test
    fun sliderReportsTheSnappedThresholdAndUpdatesTheBadge() {
        composeRule.setContent {
            QuakeAlertTheme {
                MagnitudeThresholdSelector(threshold = 4.5, onThresholdChange = { changes += it })
            }
        }
        composeRule.onNodeWithContentDescription(string(R.string.alert_threshold_title))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5.1f) }
        assertEquals(listOf(5.0), changes)
        val magnitude: String = string(R.string.magnitude_value, 5.0)
        composeRule.onNodeWithText(string(R.string.alert_threshold_badge, magnitude)).assertIsDisplayed()
    }

    private fun string(id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)
}
